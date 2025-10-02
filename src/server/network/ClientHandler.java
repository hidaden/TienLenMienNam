package server.network;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import server.core.GameRoom;
import server.core.GameState;
import shared.model.Card;
import shared.protocol.Protocol;

public class ClientHandler implements Runnable {
	private final Socket clientSocket;
	private final GameServer server;
	private PrintWriter write;
	private BufferedReader reader;
	
	private String playerName;
	private GameRoom currentRoom;
	private List<Card> hand;
	
	public ClientHandler(Socket socket, GameServer server) {
		this.clientSocket = socket;
		this.server = server;
		this.hand = new ArrayList<>();
	}

	@Override
	public void run() {
		try {
			this.reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
			this.write = new PrintWriter(clientSocket.getOutputStream(), true);
			
			String clientMessage;
			while (((clientMessage = reader.readLine()) != null)) {
				System.out.println("Received from " + playerName + ": " + clientMessage);
				
				if (currentRoom != null && currentRoom.getState() == GameState.PLAYING) {
					currentRoom.processPlayerMove(this, clientMessage);
				} else {
					handleLobbyCommand(clientMessage);
				}
			}
			
		} catch (IOException e) {
			// TODO: handle exception
		} finally {
			if (currentRoom != null) {
				currentRoom.removePlayer(this);
			}
			server.removeClient(this);
			try {
				clientSocket.close();
			} catch (IOException e2) {}
		}
	}
	
	private void handleLobbyCommand(String message) {
		String [] parts = message.split("\\" + Protocol.SEPARATOR, 2);
		String command = parts[0];
		String payload = parts.length > 1 ? parts[1] : "";
		
		switch (command) {
		case Protocol.C_SET_NAME:
			if (!payload.isEmpty()) {
				this.setPlayerName(payload);
				System.out.println("Client " + clientSocket.getInetAddress().getHostAddress() + " set name to: " + this.playerName);
				sendMessage(Protocol.S_MSG + Protocol.SEPARATOR + "Welcome, " + this.playerName + "!");
			} else {
				sendMessage(Protocol.S_ERROR + Protocol.SEPARATOR + "Name cannot be empty.");
			}
			break;
		
		case Protocol.C_CREATE_ROOM:
			server.createRoom(this);
			break;
			
		case Protocol.C_JOIN_ROOM:
			if (payload.isEmpty()) {
				server.joinRoom(this, payload);
			} else {
				sendMessage(Protocol.S_ERROR + Protocol.SEPARATOR + "Room ID is required");
			}
			break;
			
		default:
			sendMessage(Protocol.S_ERROR + Protocol.SEPARATOR + "Unknown comman in lobby: " + command);
			break;
		}
	}
	
	public void setHand(List<Card> hand) {
		this.hand = new ArrayList<>(hand);
	}
	
	public void removeCardsFromHand(List<Card> cardsToRemove) {
		this.hand.removeAll(cardsToRemove);
	}
	
	public void sendMessage(String message) {
		write.println(message);
	}
	
	public String getPlayerName() {
		return playerName;
	}
	
	public void setPlayerName(String name) {
		this.playerName = name;
	}
	
	public void setCurrentRoom(GameRoom room) {
		this.currentRoom = room;
	}

	public List<Card> getHand() {
		return hand;
	}
	
	
}
