package client;

import java.util.ArrayList;
import java.util.List;

import shared.model.Card;
import shared.model.Rank;
import shared.model.Suit;

/**
 * 
 * Các hàm tiện ích chuyển đổi String <-> Card Chuỗi lá bài dùng format của
 * server: e.g. "3S", "10H", "AD", "KC"
 */
public class CardUtils {
	/**
	 * 
	 * Parse một token như "3S" hoặc "10H" thành Card. Trả về null nếu không parse
	 * được.
	 */
	public static Card parseCard(String token) {
		if (token == null)
			return null;
		token = token.trim().toUpperCase();
		if (token.length() < 2)
			return null;
		char suitChar = token.charAt(token.length() - 1);
		String rankStr = token.substring(0, token.length() - 1);

		Suit suit;
		switch (suitChar) {
		case 'S':
			suit = Suit.SPADES;
			break;
		case 'C':
			suit = Suit.CLUBS;
			break;
		case 'D':
			suit = Suit.DIAMONDS;
			break;
		case 'H':
			suit = Suit.HEARTS;
			break;
		default:
			return null;
		}

		for (Rank r : Rank.values()) {
			if (r.getDisplayName().equals(rankStr)) {
				return new Card(r, suit);
			}
		}
		return null;
	}

	/**
	 * 
	 * Parse danh sách lá bài dạng "3S,4C,10H" hoặc "3S 4C 10H" -> List<Card> (bỏ
	 * những token không hợp lệ)
	 */
	public static List<Card> parseCardList(String raw) {
		List<Card> out = new ArrayList<>();
		if (raw == null)
			return out;
		// Có thể server dùng "," làm list-sep
		String cleaned = raw.trim();
		if (cleaned.isEmpty())
			return out;

		String[] tokens;
		if (cleaned.contains(","))
			tokens = cleaned.split(",");
		else
			tokens = cleaned.split("\s+");

		for (String t : tokens) {
			t = t.trim();
			if (t.isEmpty())
				continue;
			Card c = parseCard(t);
			if (c != null)
				out.add(c);
		}
		return out;
	}

	public static String joinCardList(List<Card> cards) {
		if (cards == null || cards.isEmpty())
			return "";
		StringBuilder sb = new StringBuilder();
		boolean first = true;
		for (Card c : cards) {
			if (!first)
				sb.append(",");
			sb.append(c.toString());
			first = false;
		}
		return sb.toString();
	}
}
