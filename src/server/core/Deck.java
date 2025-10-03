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

		// Chia bÃ i cho má»™t sá»‘ lÆ°á»£ng ngÆ°á»�i chÆ¡i nháº¥t Ä‘á»‹nh
		public List<List<Card>> deal(int numPlayers, int cardsPerPlayer) {
			if (numPlayers * cardsPerPlayer > cards.size()) {
				throw new IllegalArgumentException("KhÃ´ng Ä‘á»§ bÃ i Ä‘á»ƒ chia!");
			}

			List<List<Card>> hands = new ArrayList<>();
			for (int i = 0; i < cardsPerPlayer; i++) {
				hands.add(new ArrayList<>());
			}

			for (int i = 0; i < cardsPerPlayer; i++) {
				for (int j = 0; j < numPlayers; j++) {
					hands.get(j).add(cards.remove(0)); // Láº¥y lÃ¡ bÃ i trÃªn cÃ¹ng cá»§a bá»™ bÃ i
				}
			}

			// Sáº¯p xáº¿p bÃ i cá»§a má»—i ngÆ°á»�i
			for (List<Card> hand : hands) {
				Collections.sort(hand);
			}

			return hands;
		}

		public int size() {
			return cards.size();
		}
	}

