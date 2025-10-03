package client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Scanner;

import shared.model.Card;
import shared.protocol.Protocol;

/**
 * 
 * Client console để kết nối GameServer.
 * 
 * Cách dùng:
 * 
 * setname <name>
 * 
 * create
 * 
 * join <roomId>
 * 
 * play 3S 4C 5D (hoặc play 3S,4C,5D)
 * 
 * pass
 * 
 * chat Hello
 * 
 * exit
 */
public class ClientApp {
	private final String host;
	private final int port;

	private Socket socket;
	private BufferedReader in;
	private PrintWriter out;
	private final Player player = new Player();
	private volatile boolean running = false;

	public ClientApp(String host, int port) {
		this.host = host;
		this.port = port;
	}

	public void connect() throws IOException {
		socket = new Socket(host, port);
		in = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
		out = new PrintWriter(socket.getOutputStream(), true);
		running = true;
		System.out.println("Connected to server " + host + ":" + port);
	}

	public void close() {
		running = false;
		try {
			if (socket != null && !socket.isClosed())
				socket.close();
		} catch (IOException e) {
		}
	}

	public void sendMessage(String msg) {
		if (out != null) {
			out.println(msg);
			out.flush();
		}
	}

	public void startListener() {
		Thread t = new Thread(() -> {
			try {
				String line;
				while (running && (line = in.readLine()) != null) {
					handleServerMessage(line);
				}
			} catch (IOException e) {
				if (running)
					System.out.println("[Error] Listener lỗi: " + e.getMessage());
			} finally {
				running = false;
			}
		}, "Server-Listener");
		t.setDaemon(true);
		t.start();
	}

	private void handleServerMessage(String raw) {
// In thô để debug
		System.out.println("\n[Server] " + raw);

// Tách command và payload một cách mềm dẻo
		String cmd = null;
		String payload = "";
		int sepIndex = raw.indexOf(Protocol.SEPARATOR);
		if (sepIndex >= 0) {
			cmd = raw.substring(0, sepIndex);
			payload = raw.substring(sepIndex + 1);
		} else {
			// nếu không có separator, thử detect theo prefix (một số message do server tạo
			// có thể dính tên vào ngay sau cmd)
			String[] known = { Protocol.S_GAME_START, Protocol.S_YOUR_TURN, Protocol.S_UPDATE_GAME, Protocol.S_MSG,
					Protocol.S_ERROR, Protocol.S_INVALID_MOVE, Protocol.S_GAME_OVER, Protocol.S_CHAT,
					Protocol.S_ROOM_LIST };
			for (String k : known) {
				if (raw.startsWith(k)) {
					cmd = k;
					payload = raw.substring(k.length());
					if (payload.startsWith(Protocol.SEPARATOR))
						payload = payload.substring(1);
					break;
				}
			}
		}

		if (cmd == null) {
			System.out.println("[Unknown message] " + raw);
			return;
		}

		switch (cmd) {
		case Protocol.S_MSG:
			System.out.println("[Server MSG] " + payload);
			break;
		case Protocol.S_ERROR:
			System.out.println("[Server ERROR] " + payload);
			break;
		case Protocol.S_GAME_START:
			// payload: danh sách bài cho client hiện tại, ví dụ "3S,4C,5D,..."
			List<Card> hand = CardUtils.parseCardList(payload);
			player.setHand(hand);
			System.out.println("[GAME_START] Bài đã được chia cho bạn:");
			player.showHand();
			break;
		case Protocol.S_YOUR_TURN:
			// payload có thể chứa last move
			System.out.println("[TURN] Đến lượt bạn!");
			if (payload != null && !payload.isBlank()) {
				System.out.println("Last move: " + payload);
			}
			player.showHand();
			System.out.println("Nhập: play <cards>  hoặc pass");
			break;
		case Protocol.S_UPDATE_GAME:
			// cố gắng parse payload: playerName|ACTION|cards
			// Lưu ý server có thể không luôn chèn separator chuẩn giữa cmd và playerName,
			// mình xử lý mềm dẻo phía trên
			String[] parts = payload.split("\\" + Protocol.SEPARATOR);
			if (parts.length >= 2) {
				String playerName = parts[0];
				String action = parts[1];
				String cards = parts.length >= 3 ? parts[2] : "";
				if (action.equals(Protocol.C_PLAY)) {
					System.out.println("[UPDATE] " + playerName + " played: " + cards);
					if (playerName.equals(player.getName())) {
						// xóa các lá vừa đánh khỏi hand của mình
						List<Card> played = CardUtils.parseCardList(cards);
						player.removeCards(played);
						System.out.println("Bạn đã đánh: " + cards);
						player.showHand();
					}
				} else if (action.equals(Protocol.C_PASS)) {
					System.out.println("[UPDATE] " + playerName + " passed.");
				} else {
					System.out.println("[UPDATE] " + payload);
				}
			} else {
				System.out.println("[UPDATE] " + payload);
			}
			break;
		case Protocol.S_INVALID_MOVE:
			System.out.println("[INVALID MOVE] " + payload);
			break;
		case Protocol.S_GAME_OVER:
			System.out.println("[GAME OVER] " + payload);
			break;
		case Protocol.S_CHAT:
			System.out.println("[CHAT] " + payload);
			break;
		case Protocol.S_ROOM_LIST:
			System.out.println("[ROOM LIST] " + payload);
			break;

		default:
			System.out.println("[Unhandled] " + cmd + " -> " + payload);
		}

	}

	/**
	 * 
	 * Vòng lặp console để người dùng nhập lệnh và gửi server.
	 */
	public void runConsole() {
		Scanner scanner = new Scanner(System.in);
		System.out.println("=== TienLen Client Console ===");
		System.out.println(
				"Commands: setname <name> | create | join <roomId> | play <cards> | pass | chat <msg> | list | exit | start");
		while (running) {
			System.out.print("> ");
			String line;
			try {
				line = scanner.nextLine();
			} catch (Exception e) {
				break;
			}
			if (line == null)
				break;
			line = line.trim();
			if (line.isEmpty())
				continue;
			String[] tokens = line.split("\s+", 2);
			String cmd = tokens[0].toLowerCase();
			String arg = tokens.length >= 2 ? tokens[1].trim() : "";

			switch (cmd) {
			case "setname":
			case "name":
				if (arg.isEmpty()) {
					System.out.println("Usage: setname YourName");
				} else {
					player.setName(arg);
					sendMessage(Protocol.C_SET_NAME + Protocol.SEPARATOR + arg);
				}
				break;
			case "create":
				sendMessage(Protocol.C_CREATE_ROOM + Protocol.SEPARATOR + arg);
				break;
			case "join":
				if (arg.isEmpty()) {
					System.out.println("Usage: join <roomId>");
				} else {
					sendMessage(Protocol.C_JOIN_ROOM + Protocol.SEPARATOR + arg);
				}
				break;
			case "play":
				if (arg.isEmpty()) {
					System.out.println("Usage: play 3S 4C 5D  (or play 3S,4C,5D)");
					break;
				}
				// normalise arg to comma separated list
				String cardPayload = arg.contains(",") ? arg.replaceAll("\\s+", "") : arg.replaceAll("\\s+", ",");
				sendMessage(Protocol.C_PLAY + Protocol.SEPARATOR + cardPayload);
				break;
			case "pass":
				sendMessage(Protocol.C_PASS + Protocol.SEPARATOR);
				break;
			case "chat":
				if (arg.isEmpty())
					System.out.println("Usage: chat <message>");
				else
					sendMessage(Protocol.C_CHAT + Protocol.SEPARATOR + arg);
				break;
			case "list":
				sendMessage(Protocol.C_LIST_ROOMS + Protocol.SEPARATOR);
				break;
			case "start":
				sendMessage(Protocol.C_START_GAME + Protocol.SEPARATOR);
				break;

			case "exit":
				System.out.println("Exiting...");
				close();
				break;
			default:
				System.out.println("Unknown command: " + cmd);
				break;
			}

		}
		scanner.close();
	}

	public static void main(String[] args) {
		String host = "localhost";
		int port = 8888;
		if (args.length >= 1)
			host = args[0];
		if (args.length >= 2) {
			try {
				port = Integer.parseInt(args[1]);
			} catch (NumberFormatException e) {
			}
		}

		ClientApp client = new ClientApp(host, port);
		try {
			client.connect();
			client.startListener();
			client.runConsole();
		} catch (IOException e) {
			System.err.println("Không thể kết nối server: " + e.getMessage());
		} finally {
			client.close();
			System.out.println("Client terminated.");
		}

	}
}