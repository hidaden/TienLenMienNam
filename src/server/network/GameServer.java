package server.network;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import server.core.GameRoom;
import shared.model.Card;
import shared.model.Rank;
import shared.model.Suit;
import shared.protocol.Protocol;

public class GameServer {

	private static final int PORT = 8888;
	
	private final Set<ClientHandler> clients = Collections.synchronizedSet(new HashSet<>());
	
	private final Map<String, GameRoom> gameRooms = new ConcurrentHashMap<>();
	
	private static final Map<String, Card> CARD_CACHE = new HashMap<>();
	
	static {
		System.out.println("Initializing card cache...");
		for (Suit suit: Suit.values()) {
			for (Rank rank : Rank.values()) {
				Card card = new Card(rank, suit);
				CARD_CACHE.put(card.toString(), card);
			}
		}
		System.out.println("Card cache initialized with " + CARD_CACHE.size() + " cards.");
	}
	
	public static Card getCardFromString(String cardString) {
		return CARD_CACHE.get(cardString);
	}
	
	public void start() {
		System.out.println("Tien Len Mien Nam Server is starting...");
		
		try (ServerSocket serverSocket = new ServerSocket(PORT)) {
			System.out.println("Server is listening on port: " + PORT);
			
			// Vòng lặp vô tận để luôn chấp nhận kết nối mới
			while (true) {
				Socket clientSocket = serverSocket.accept();
				
				ClientHandler clientHandler = new ClientHandler(clientSocket, this);
				clients.add(clientHandler);
				
				
				// Khi có client kết nối, tạo một Virtual Thread để xử lý riêng client đó
                // Điều này giúp server không bị block và sẵn sàng nhận client tiếp theo ngay lập tức
				Thread.ofVirtual().start(clientHandler);
			}
		} catch (IOException e) {
			System.err.println("Server exception: " + e.getMessage());
			e.printStackTrace();
		}
	}
	
	public void removeClient(ClientHandler clientHandler) {
		clients.remove(clientHandler);
		System.out.println("Client: " + clientHandler.getPlayerName() + " removed from server.");
	}
	
	public void createRoom(ClientHandler player) {
		GameRoom newRoom = new GameRoom();
		gameRooms.put(newRoom.getRoomId(), newRoom);
		System.out.println("New room created: " + newRoom.getRoomId());
		newRoom.addPlayer(player);
	}
	
	public void joinRoom(ClientHandler player, String roomId) {
		GameRoom room = gameRooms.get(roomId.toUpperCase());
		if (room != null) {
			room.addPlayer(player);
		} else {
			player.sendMessage(Protocol.S_ERROR + "Room not found.");
		}
	}
	
	public static void main(String[] args) {
		GameServer server = new GameServer();
		server.start();
	}
}
