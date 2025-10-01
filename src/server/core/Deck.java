package server.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import shared.model.Card;
import shared.model.Rank;
import shared.model.Suit;

public class Deck {
	private final List<Card> cards;
	
	public Deck() {
		this.cards = new ArrayList<>();
		// Tao ra 52 la' bai` chuan
		for (Suit suit : Suit.values()) {
			for (Rank rank : Rank.values()) {
				cards.add(new Card(rank, suit));
			}
		}
	}
	
	public void shuffle() {
		Collections.shuffle(cards);
	}
	
	// Chia bài cho một số lượng người chơi nhất định
	public List<List<Card>> deal(int numPlayers, int cardsPerPlayer) {
		if (numPlayers * cardsPerPlayer > cards.size()) {
			throw new IllegalArgumentException("Không đủ bài để chia!");
		}
		
		List<List<Card>> hands = new ArrayList<>();
		for (int i = 0; i < cardsPerPlayer; i++) {
			hands.add(new ArrayList<>());
		}
		
		for (int i = 0; i < cardsPerPlayer; i++) {
			for (int j = 0; j < numPlayers; j++) {
				hands.get(j).add(cards.remove(0)); // Lấy lá bài trên cùng của bộ bài
			}
		}
		
		// Sắp xếp bài của mỗi người 
		for (List<Card> hand : hands) {
			Collections.sort(hand);
		}
		
		return hands;
	}
	
	public int size() {
		return cards.size();
	}
}

