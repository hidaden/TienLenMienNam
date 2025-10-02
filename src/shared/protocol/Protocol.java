package shared.protocol;

public class Protocol {
	public static final String SEPARATOR = "|";
	public static final String LIST_SEPARATOR = ",";
	
	// Lệnh từ Client gửi lên Server (C2S) 
	public static final String C_SET_NAME = "SET_NAME";
	public static final String C_CREATE_ROOM = "CREATE_ROOM";
	public static final String C_JOIN_ROOM = "JOIN_ROOM";
	public static final String C_PLAY = "PLAY";
	public static final String C_PASS = "PASS";
	public static final String C_CHAT = "CHAT"; 
	public static final String C_LIST_ROOMS = "LIST_ROOMS"; 

	// Lệnh từ Server gửi xuống Client (S2C)
	public static final String S_MSG = "MSG";
	public static final String S_ERROR = "ERROR";
	public static final String S_GAME_START = "GAME_START";
	public static final String S_YOUR_TURN = "YOUR_TURN";
	public static final String S_UPDATE_GAME = "UPDATE_GAME";
	public static final String S_INVALID_MOVE = "INVALID_MOVE";
	public static final String S_GAME_OVER = "GAME_OVER";
	public static final String S_CHAT = "CHAT"; 
	public static final String S_ROOM_LIST = "ROOM_LIST"; 
}
