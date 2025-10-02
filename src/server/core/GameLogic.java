package server.core;

import java.util.Collections;
import java.util.List;
import java.util.zip.CheckedInputStream;

import server.core.Combination.CombinationType;
import shared.model.Card;
import shared.model.Rank;

public class GameLogic {
	
	/**
     * Hàm trọng tâm: Kiểm tra nước đi hiện tại có hợp lệ so với nước đi trước đó.
     * @param lastCombination Nước đi của người chơi trước.
     * @param currentCombination Nước đi của người chơi hiện tại.
     * @return true nếu hợp lệ, false nếu không.
     */
	
	public static boolean isValidMove(Combination lastCombination, Combination currentCombination) {
		// 1. Nước đi hiện tại phải hợp lệ
		if (currentCombination.getType() == Combination.CombinationType.INVALID) {
			return false;
		}
		
		// 2. Nếu kh có nước đi trc đó (bắt đầu round mới).
		if (lastCombination == null) {
			return true;
		}
		
		// 3. Xử lí logic chặt
		boolean canIntercept = checkInterception(lastCombination, currentCombination);
		if (canIntercept) {
			return true;
		}
		
		// 4. Nếu kh phải là chặt, hai bộ phải cùng loại và cùng kích thước
		if (currentCombination.getType() != lastCombination.getType()) {
			return false;
		}
		if (currentCombination.size() != lastCombination.size()) {
			return false;
		}
		
		// 5. Cùng loại, cùng kích thước, thì so sánh rank
		return currentCombination.getPowerRank().getValue() > lastCombination.getPowerRank().getValue();
	}
	
	// Ktra luật chặt
	private static boolean checkInterception(Combination last, Combination current) {
		// Luật 1: Heo
		if (last.getType() == Combination.CombinationType.SINGLE && last.getPowerRank() == Rank.TWO) {
			// 3 đôi thông chặt heo
			if (current.getType() == Combination.CombinationType.STRAIGHT_PAIR && current.size() == 6) {
				return true;
			}
			
			// Tứ Quý chặt heo
			if (current.getType() == Combination.CombinationType.FOUR_OF_A_KIND) {
				return true;
			}
		}
		
		// Luật 2: Đôi heo
		if (last.getType() == Combination.CombinationType.PAIR && last.getPowerRank() == Rank.TWO) {
			// Bốn đôi thông chặt đôi heo
			if (current.getType() == Combination.CombinationType.STRAIGHT_PAIR && current.size() == 8) {
				return true;
			}
		}
		
		// Luật 3: Tứ quý chặt Tứ quý
		if (last.getType() == Combination.CombinationType.FOUR_OF_A_KIND && current.getType() == Combination.CombinationType.FOUR_OF_A_KIND) {
			return current.getPowerRank().getValue() > last.getPowerRank().getValue();
		}
		
		return false;
	}
}
