package TCPSocket;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Vector;

public class ChatServer {
	private static Vector<ClientHandler> clients = new Vector<>();

	public static void main(String[] args) throws IOException {
		// tạo socket server, lắng nghe cổng 6789
		ServerSocket serverSocket = new ServerSocket(6789);
		System.out.println("Server is running and waiting for clients...");

		while (true) {
			// chấp nhận kết nối client
			Socket clientSocket = serverSocket.accept();
			System.out.println("New client connected: " + clientSocket);

			ClientHandler clientThread = new ClientHandler(clientSocket);
			clients.add(clientThread);
			new Thread(clientThread).start();
		}
	}

	public static void broadcastMessage(String message) {
		synchronized (clients) {
			for (ClientHandler client : clients) {
				client.sendMessage(message);
			}
		}
	}

	public static void removeClient(ClientHandler client) {
		synchronized (clients) {
			clients.remove(client);
		}
		System.out.println("Client disconnected.");
	}
}

class ClientHandler implements Runnable {
	private Socket clientSocket;
	private BufferedReader in;
	private PrintWriter out;
	private String clientName;

	public ClientHandler(Socket socket) throws IOException {
		this.clientSocket = socket;
		// đọc/ghi dựa trên socket của client
		in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
		out = new PrintWriter(clientSocket.getOutputStream(), true);
	}

	@Override
	public void run() {
		try {
			clientName = in.readLine();
			System.out.println(clientName + "has joined.");
			ChatServer.broadcastMessage(clientName + " has joined the chat.");

			String inputLine;
			// vòng lặp đọc tin nhắn client gửi qua TCP
			while ((inputLine = in.readLine()) != null) {
				String messageToSend = clientName + ": " + inputLine;
				System.out.println("Broadcasting: " + messageToSend);
				ChatServer.broadcastMessage(messageToSend);
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (clientName != null) {
				ChatServer.broadcastMessage(clientName + " has left the chat.");
			}
			try {
				clientSocket.close();
			} catch (IOException e2) {
				e2.printStackTrace();
			}
		}
	}

	// gửi tin nhắn tới client
	public void sendMessage(String message) {
		out.println(message);
	}

	public String getClientInfo() {
		return clientSocket.getRemoteSocketAddress().toString();
	}

}