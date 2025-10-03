package shared.model;

public record Card(Rank rank, Suit suit) implements Comparable<Card> {

	@Override
	public int compareTo(Card other) {
		// TODO Auto-generated method stub
		int rankComparison = Integer.compare(this.rank.getValue(), other.rank.getValue());
		if (rankComparison != 0) {
			return rankComparison;
		}
		return this.suit.compareTo(other.suit);
	}

	@Override
	public String toString() {
		// Vi du: "3S" cho 3 Bich', "KC" cho K chuon`
		return rank.getDisplayName() + suit.name().charAt(0);
	}

}
