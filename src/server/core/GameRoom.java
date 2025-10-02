package server.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import server.network.ClientHandler;
import server.network.GameServer;
import shared.model.Card;
import shared.model.Rank;
import shared.model.Suit;
import shared.protocol.Protocol;

public class GameRoom {
	private final String roomId;
	private final List<ClientHandler> players;
	private static final int MAX_PLAYERS = 4;
	
	private GameState state;
	private Deck deck;
	private int currentPlayerIndex;
	private Combination lastMove;
	private int passCount;
	private int lastPlayerToPlayIndex;
	
	public GameRoom() {
		this.roomId = UUID.randomUUID().toString().substring(0, 5).toUpperCase();
		this.players = new ArrayList<>();
		this.state = GameState.WAITING;
		this.deck = new Deck();
		this.currentPlayerIndex = -1;
		this.lastMove = null;
		this.passCount = 0;
		this.lastPlayerToPlayIndex = -1;
	}
	
	public synchronized void addPlayer(ClientHandler player) {
		if (players.size() < MAX_PLAYERS && state == GameState.WAITING) {
			players.add(player);
			player.setCurrentRoom(this);
			System.out.println("Player " + player.getPlayerName() + " joined room " + roomId);
			
			broadcastMessage(Protocol.S_MSG + Protocol.SEPARATOR + player.getPlayerName() + " has joined the room. Players: " + players.size() + "/" + MAX_PLAYERS);
		} else {
			// TODO: Gửi lại thông báo lỗi cho người chơi (phòng đầy hoặc game đã bắt đầu)
		}
	}
	
	public synchronized void removePlayer(ClientHandler player) {
		players.remove(player);
		broadcastMessage(Protocol.S_MSG + Protocol.SEPARATOR + player.getPlayerName() + " has left the room.");
	}
	
	public void startGame() {
		if (state != GameState.WAITING || players.size() < 2) {
			broadcastMessage(Protocol.S_MSG + Protocol.SEPARATOR + "Not enough players to start the game.");
			return;
		}
		
		this.state = GameState.PLAYING;
		broadcastMessage(Protocol.S_MSG + Protocol.SEPARATOR + "The game is starting! Dealing cards...");
		
		deck = new Deck();
		deck.shuffle();
		List<List<Card>> hands = deck.deal(players.size(), 13);
		
		Card threeOfSpades = new Card(Rank.THREE, Suit.SPADES);
		int firstPlayerIndex = -1;
		
		for (int i = 0; i < players.size(); i++) {
			ClientHandler player = players.get(i);
			List<Card> playerHand = hands.get(i);
			
			player.setHand(playerHand);
			
			String handString = formatHandToString(playerHand);
			player.sendMessage(Protocol.S_GAME_START + Protocol.SEPARATOR + handString);
			
			if (playerHand.contains(threeOfSpades)) {
				firstPlayerIndex = i;
			}
		}
		
		currentPlayerIndex = (firstPlayerIndex != -1) ? firstPlayerIndex : 0;
		
		ClientHandler firstPlayer = players.get(currentPlayerIndex);
		broadcastMessage(Protocol.S_MSG + Protocol.SEPARATOR + firstPlayer.getPlayerName() + " has the 3 of Spades and will go first.");
		
		firstPlayer.sendMessage(Protocol.S_YOUR_TURN + Protocol.SEPARATOR);
		
	}
	
	private String formatHandToString(List<Card> hand) {
		return hand.stream().map(Card::toString).collect(Collectors.joining(","));
	}
	
	public synchronized void processPlayerMove(ClientHandler player, String message) {
		if (players.indexOf(player) != currentPlayerIndex) {
			player.sendMessage(Protocol.S_ERROR + Protocol.SEPARATOR + "It's not your turn.");
			return;
		}
		
		String[] parts = message.split("\\" + Protocol.SEPARATOR, 2);
		String command = parts[0];
		
		if (command.equals(Protocol.C_PASS)) {
			passCount++;
			broadcastMessage(Protocol.S_UPDATE_GAME + player.getPlayerName() + Protocol.SEPARATOR + Protocol.C_PASS + Protocol.SEPARATOR);
			
			if (passCount >= players.size() - 1) {
				startNewRound();
			} else {
				advanceToNextPlayer();
			}
			return;
		}
		
		if (command.equals(Protocol.C_PLAY)) {
			String cardPayLoad = parts.length > 1 ? parts[1] : "";
			List<Card> cardsToPlay = parseCardsFromString(cardPayLoad);
			
			if (cardsToPlay.isEmpty()) {
				player.sendMessage(Protocol.S_INVALID_MOVE + Protocol.SEPARATOR + "You must select cards to play.");
				return;
			}
			
			Combination currentMove = Combination.fromCards(cardsToPlay);
			
			if (GameLogic.isValidMove(lastMove, currentMove)) {
				lastMove = currentMove;
				lastPlayerToPlayIndex = currentPlayerIndex;
				passCount = 0;
				
				player.removeCardsFromHand(cardsToPlay);
				broadcastMessage(Protocol.S_UPDATE_GAME + Protocol.SEPARATOR + player.getPlayerName() + Protocol.SEPARATOR + Protocol.C_PLAY + Protocol.SEPARATOR + cardPayLoad);
				
				if (player.getHand().isEmpty()) {
					state = GameState.FINISHED;
					broadcastMessage(Protocol.S_GAME_OVER + Protocol.SEPARATOR + player.getPlayerName());
					return;
				}
				
				advanceToNextPlayer();
				
			} else {
				player.sendMessage(Protocol.S_INVALID_MOVE + Protocol.SEPARATOR + "Yout move is not valid.");
			}
		}
	}
	
	public Combination getLastMove() {
		return lastMove;
	}

	public void setLastMove(Combination lastMove) {
		this.lastMove = lastMove;
	}

	private void startNewRound() {
		broadcastMessage(Protocol.S_MSG + Protocol.SEPARATOR + "Everyone passed. New round starts.");
		lastMove = null;
		passCount = 0;
		currentPlayerIndex = lastPlayerToPlayIndex;
		players.get(currentPlayerIndex).sendMessage(Protocol.S_YOUR_TURN + Protocol.SEPARATOR);
	}
	
	private void advanceToNextPlayer() {
		currentPlayerIndex = (currentPlayerIndex + 1) % players.size();
		ClientHandler nextPlayer = players.get(currentPlayerIndex);
		
		String lastMoveString = (lastMove != null) ? formatHandToString(lastMove.getCards()) : "";
		nextPlayer.sendMessage(Protocol.S_YOUR_TURN + Protocol.SEPARATOR + lastMoveString);
	}
	
	private List<Card> parseCardsFromString(String cardStr) {
		if (cardStr == null || cardStr.isEmpty()) {
			return new ArrayList<>();
		}
		
		List<Card> cards = new ArrayList<>();
		String[] cardTokens = cardStr.split(",");
		
		for (String token : cardTokens) {
			Card card = GameServer.getCardFromString(token.trim().toUpperCase());
			
			if (card != null) {
				cards.add(card);
			} else {
				System.err.println("Warning: Received invalid card string from client: " + token);
			}
		}
		return cards;
	}
	
	public void broadcastMessage(String message) {
		for (ClientHandler player : players) {
			player.sendMessage(message);
		}
	}

	public String getRoomId() {
		return roomId;
	}

	public void setState(GameState state) {
		this.state = state;
	}

	public GameState getState() {
		return state;
	}
	
}
