package client;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import shared.model.Card;

/**
 * 
 * Player lưu tên và danh sách lá bài (List<Card>).
 */
public class Player {
	private String name;
	private final List<Card> hand = new ArrayList<>();

	public Player() {
		this.name = "Anonymous";
	}

	public Player(String name) {
		this.name = name;
	}

	public synchronized String getName() {
		return name;
	}

	public synchronized void setName(String name) {
		this.name = name;
	}

	public synchronized void setHand(List<Card> cards) {
		hand.clear();
		if (cards != null)
			hand.addAll(cards);
	}

	public synchronized List<Card> getHand() {
		return new ArrayList<>(hand);
	}

	public synchronized void addCards(List<Card> cards) {
		if (cards != null)
			hand.addAll(cards);
	}

	public synchronized void removeCards(List<Card> cards) {
		if (cards != null)
			hand.removeAll(cards);
	}

	public synchronized boolean hasCard(Card c) {
		return hand.contains(c);
	}

	// In bài trên tay ra console, có index để dễ chọn
	public synchronized void showHand() {
		if (hand.isEmpty()) {
			System.out.println("[Hand] Bạn hiện không có lá bài nào.");
			return;
		}
		System.out.println("===== Your hand (" + hand.size() + " cards) =====");
		int i = 1;
		StringBuilder sb = new StringBuilder();
		for (Card c : hand) {
			sb.append(String.format("%2d:%s  ", i, c.toString()));
			if (i % 8 == 0)
				sb.append("\n");
			i++;
		}
		System.out.println(sb.toString());
		System.out.println("=================================");
	}

	/**
	 * 
	 * Cho phép người dùng chọn bài từ console. Hỗ trợ nhập dạng: * bằng mã lá: 3S
	 * 4C 10H * hoặc bằng chỉ số: 1 3 5 * hoặc PASS để bỏ lượt (trả về empty list)
	 *
	 * Trả về danh sách Card đã chọn (không xoá khỏi hand ở đây).
	 */
//	public List<Card> chooseCardsFromConsole(Scanner scanner) {
//		showHand();
//		System.out.println("Nhập các lá muốn đánh (ví dụ: 3S 4C 5D) hoặc chỉ số (1 3 5). Gõ PASS để bỏ lượt.");
//		System.out.print("> ");
//		String line = scanner.nextLine().trim();
//		if (line.equalsIgnoreCase("PASS") || line.isEmpty()) {
//			return new ArrayList<>();
//		}
//		List<Card> chosen = new ArrayList<>();
//		String[] tokens = line.split("\s+");
//		for (String t : tokens) {
//			if (t.matches("\\d+")) {
//				int idx = Integer.parseInt(t);
//				synchronized (hand) {
//					if (idx >= 1 && idx <= hand.size()) {
//						chosen.add(hand.get(idx - 1));
//					} else {
//						System.out.println("Chỉ số không hợp lệ: " + t);
//					}
//				}
//			} else {
//				Card parsed = CardUtils.parseCard(t);
//				if (parsed == null) {
//					System.out.println("Không nhận diện được lá bài: " + t);
//				} else {
//					synchronized (hand) {
//						if (hand.contains(parsed))
//							chosen.add(parsed);
//						else
//							System.out.println("Bạn không có lá bài: " + parsed);
//					}
//				}
//			}
//		}
//		return chosen;
//	}
}
