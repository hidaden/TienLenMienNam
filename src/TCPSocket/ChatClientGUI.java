package TCPSocket;

import java.awt.BorderLayout;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

public class ChatClientGUI extends JFrame{
	private Socket socket;
	private PrintWriter out;
	private BufferedReader in;
	private String clientName;
	
	private JTextArea messageArea;
	private JTextField textField;
	private JButton sendButton;
	
	public ChatClientGUI() {
		super("Chat Room");
		
		setSize(500, 400);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		messageArea = new JTextArea();
		messageArea.setEditable(false);
		add (new JScrollPane(messageArea), BorderLayout.CENTER);
		
		textField = new JTextField();
		sendButton = new JButton("Send");
		
		JPanel bottomPanel = new JPanel(new BorderLayout());
		bottomPanel.add(textField, BorderLayout.CENTER);
		bottomPanel.add(sendButton, BorderLayout.EAST);
		add(bottomPanel, BorderLayout.SOUTH);
		
		ActionListener senMessageAction = e -> sendMessage();
		sendButton.addActionListener(senMessageAction);
		textField.addActionListener(senMessageAction);
		
		clientName = JOptionPane.showInputDialog(this, "Enter your name:", "Name entry", JOptionPane.PLAIN_MESSAGE);
		if (clientName == null || clientName.trim().isEmpty()) {
			System.exit(0);
		}
		setTitle("Chat Room - " + clientName);
		
		try {
			// tạo kết nối TCP tới server 
			socket = new Socket("localhost", 6789);
			// lấy luồng ghi/đọc từ socket 
			out = new PrintWriter(socket.getOutputStream(), true);
			in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			
			out.println(clientName);
			
			new Thread(new ServerListener()).start();
		} catch (IOException e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(this, "Could not connect to the server.", "Connection Error", JOptionPane.ERROR_MESSAGE);
			System.exit(1);
		}
	}
	
	private void sendMessage() {
		String message = textField.getText();
		if (!message.trim().isEmpty()) {
			out.println(message);
			textField.setText("");
		}
	}
	
	private class ServerListener implements Runnable {

		@Override
		public void run() {
			// TODO Auto-generated method stub
			try {
				String serverMessage;
				while ((serverMessage = in.readLine()) != null) {
					messageArea.append(serverMessage + "\n");
				}
			} catch (IOException e) {
				messageArea.append("Connection to server lost.\n");
			}
		}
	}
	
	public static void main(String[] args) {
		SwingUtilities.invokeLater(() -> {
			new ChatClientGUI().setVisible(true);
		});
	}
}