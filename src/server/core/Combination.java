package server.core;

import java.util.Collections;
import java.util.List;

import shared.model.Card;
import shared.model.Rank;


// Lớp này đại diện cho nước đi.
// Nó phân tích một danh sách các lá bài và xđ loại của chúng.
public class Combination {
	public enum CombinationType {
		INVALID, // Không hợp lệ
		SINGLE, // Rác
		PAIR, // Đôi
		TRIPLE, // Sám
		STRAIGHT, // Sảnh
		FOUR_OF_A_KIND, // Tứ Quý
		STRAIGHT_PAIR // Đôi Thông
	}
	
	private final List<Card> cards;
	private final CombinationType type;
	private final Rank powerRank; // Rank dùng để so sánh sức 
	
	public List<Card> getCards() {
		return cards;
	}
	public CombinationType getType() {
		return type;
	}
	public Rank getPowerRank() {
		return powerRank;
	}
	public int size() {
		return cards.size();
	}
	
	public Combination(List<Card> cards, CombinationType type, Rank powerRank) {
		Collections.sort(cards);
		this.cards = cards;
		this.type = type;
		this.powerRank = powerRank;
	}
	
	// Một phương thức static để phân tích một danh sách lá bài và trả về 1 đối tg Combination
	public static Combination fromCards(List<Card> cards) {
		if (cards == null || cards.isEmpty()) {
			return new Combination(Collections.emptyList(), CombinationType.INVALID, null);
		}
		
		Collections.sort(cards);
		int size = cards.size();
		
		if (isFourOfAKind(cards)) {
			return new Combination(cards, CombinationType.FOUR_OF_A_KIND, cards.get(0).rank());
		}
		if (isStraightPair(cards)) {
			return new Combination(cards, CombinationType.STRAIGHT_PAIR, cards.get(size-1).rank());
		}
		if (isStraight(cards)) {
			return new Combination(cards, CombinationType.STRAIGHT, cards.get(size-1).rank());
		}
		if (isTriple(cards)) {
			return new Combination(cards, CombinationType.TRIPLE, cards.get(0).rank());
		}
		if (isPair(cards)) {
			return new Combination(cards, CombinationType.PAIR, cards.get(0).rank());
		}
		if (isSingle(cards)) {
			return new Combination(cards, CombinationType.SINGLE, cards.get(0).rank());
		}
		
		return new Combination(cards, CombinationType.INVALID, null); 
	}
	
	private static boolean isSingle(List<Card> cards) {
		return cards.size() == 1;
	}
	
	private static boolean isPair(List<Card> cards) {
		return cards.size() == 2 && cards.get(0).rank() == cards.get(1).rank();
	}
	
	private static boolean isTriple(List<Card> cards) {
		return cards.size() == 3 && cards.get(0).rank() == cards.get(1).rank() && cards.get(1).rank() == cards.get(2).rank();
	}
	
	private static boolean isFourOfAKind(List<Card> cards) {
		return cards.size() == 4 && cards.get(0).rank() == cards.get(1).rank() &&cards.get(1).rank() == cards.get(2).rank() && cards.get(2).rank() == cards.get(3).rank();
	}
	
	private static boolean isStraight(List<Card> cards) {
		if (cards.size() < 3) return false;
		if (cards.stream().anyMatch(c -> c.rank() == Rank.TWO)) return false;
		
		for (int i = 0; i < cards.size() - 1; i++) {
			if (cards.get(i+1).rank().getValue() - cards.get(i).rank().getValue() != 1) {
				return false;
			}
		}
		return true;
	}
	
	private static boolean isStraightPair(List<Card> cards) {
		if (cards.size() < 6 || cards.size() % 2 != 0) return false;
		
		// Đôi thông 0 đc chứa lá 2
		if (cards.stream().anyMatch(c -> c.rank() == Rank.TWO)) return false;
		
		// Ktra mỗi 2 lá có phải là 1 đôi 
		for (int i = 0; i < cards.size(); i += 2) {
			if (cards.get(i).rank() != cards.get(i + 1).rank()) {
				return false;
			}
		}
		
		// Ktra các đôi có phải là 1 sảnh kh
		for (int i = 0; i < cards.size() - 2; i += 2) {
			if (cards.get(i + 2).rank().getValue() - cards.get(i).rank().getValue() != 1) {
				return false;
			}
		}
		
		return true;
	}
}
