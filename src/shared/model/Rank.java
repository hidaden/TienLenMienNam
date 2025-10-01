package shared.model;

public enum Rank {
	THREE(3, "3"),
	FOUR(4, "4"),
	FIVE(5, "5"),
	SIX(6, "6"),
	SEVEN(7, "7"),
	EIGHT(8, "8"),
	NINE(9, "9"),
	TEN(10, "10"),
	JACK(11, "J"),
	QUEEN(12, "Q"),
	KING(13, "K"),
	ACE(14, "A"),
	TWO(15, "2");
	
	private final int value;
	private final String displayName;
	
	Rank(int value, String displayName) {
		this.value = value;
		this.displayName = displayName;
	}
	
	public int getValue() {
		return value;
	}
	
	public String getDisplayName() {
		return displayName;
	}
}
