package Pokergames;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HandEvaluator {

    // 1. 輔助方法：將手牌依照點數從小到大排序 [cite: 39]
    private static void sortHand(List<Card> hand) {
        // 使用 Lambda 表示式，比較兩張牌的 Face 在 enum 中的順序 (ordinal)
        hand.sort((c1, c2) -> c1.getFace().ordinal() - c2.getFace().ordinal());
    }

    // 2. 輔助方法：建立點數次數統計表 (Frequency Map) 
    private static Map<Face, Integer> getFaceFrequency(List<Card> hand) {
        Map<Face, Integer> frequencyMap = new HashMap<>();
        for (Card card : hand) {
            Face face = card.getFace();
            // 如果點數已存在就次數+1，不存在就從 0 開始+1
            frequencyMap.put(face, frequencyMap.getOrDefault(face, 0) + 1);
        }
        return frequencyMap;
    }

    // a) 判斷是否為「一對」 (A Pair) 
    // 條件：點數統計表中，恰好有一個點數出現了 2 次
    public static boolean isPair(List<Card> hand) {
        Map<Face, Integer> frequencyMap = getFaceFrequency(hand);
        int pairCount = 0;
        for (int count : frequencyMap.values()) {
            if (count == 2) pairCount++;
        }
        return pairCount == 1; // 剛好只有一對
    }

    // b) 判斷是否為「兩對」 (Two Pairs) 
    // 條件：點數統計表中，有兩個不同的點數各出現了 2 次
    public static boolean isTwoPairs(List<Card> hand) {
        Map<Face, Integer> frequencyMap = getFaceFrequency(hand);
        int pairCount = 0;
        for (int count : frequencyMap.values()) {
            if (count == 2) pairCount++;
        }
        return pairCount == 2; // 有兩對
    }

    // c) 判斷是否為「三條」 (Three of a Kind) 
    // 條件：點數統計表中，某個點數出現了 3 次，且其他點數沒有成對（排除葫蘆）
    public static boolean isThreeOfAKind(List<Card> hand) {
        Map<Face, Integer> frequencyMap = getFaceFrequency(hand);
        boolean hasThree = false;
        boolean hasPair = false;
        
        for (int count : frequencyMap.values()) {
            if (count == 3) hasThree = true;
            if (count == 2) hasPair = true;
        }
        return hasThree && !hasPair; // 有三張相同的，但不能有配對（否則就是葫蘆了）
    }

    // d) 判斷是否為「四條/鐵支」 (Four of a Kind) 
    // 條件：點數統計表中，某個點數出現了 4 次
    public static boolean isFourOfAKind(List<Card> hand) {
        Map<Face, Integer> frequencyMap = getFaceFrequency(hand);
        return frequencyMap.containsValue(4);
    }

    // e) 判斷是否為「同花」 (A Flush) 
    // 條件：五張牌的花色（Suit）全部一模一樣
    public static boolean isFlush(List<Card> hand) {
        Suit firstSuit = hand.get(0).getSuit();
        for (Card card : hand) {
            if (card.getSuit() != firstSuit) {
                return false; // 只要有一張花色不同就不是同花
            }
        }
        return true;
    }

    // f) 判斷是否為「順子」 (A Straight) 
    // 條件：先排序，接著後面每一張牌的點數必須比前一張恰好大 1
    public static boolean isStraight(List<Card> hand) {
        sortHand(hand); // 先排序 [cite: 39]
        
        // 檢查一般順子 (例如：2, 3, 4, 5, 6)
        for (int i = 0; i < hand.size() - 1; i++) {
            int currentOrdinal = hand.get(i).getFace().ordinal();
            int nextOrdinal = hand.get(i + 1).getFace().ordinal();
            if (nextOrdinal != currentOrdinal + 1) {
                return false; // 沒有連續就不是順子
            }
        }
        return true;
    }

    // g) 判斷是否為「葫蘆」 (A Full House) 
    // 條件：點數統計表中，一個點數出現 3 次，另一個點數出現 2 次 
    public static boolean isFullHouse(List<Card> hand) {
        Map<Face, Integer> frequencyMap = getFaceFrequency(hand);
        return frequencyMap.containsValue(3) && frequencyMap.containsValue(2);
    }
    
    // h) 新增：計算牌型的權重分數 (分數越高牌型越大)
    public static int getHandValue(List<Card> hand) {
        if (isFourOfAKind(hand)) return 7;
        if (isFullHouse(hand))   return 6;
        if (isFlush(hand))       return 5;
        if (isStraight(hand))    return 4;
        if (isThreeOfAKind(hand))return 3;
        if (isTwoPairs(hand))    return 2;
        if (isPair(hand))        return 1;
        return 0; // 高牌
    }

    // i) 新增：將分數轉換為中文名稱
    public static String getHandName(int value) {
        switch (value) {
            case 7: return "四條 / 鐵支 (Four of a Kind) 💥";
            case 6: return "葫蘆 (Full House) 🏠";
            case 5: return "同花 (Flush) 🌊";
            case 4: return "順子 (Straight) 🏃";
            case 3: return "三條 (Three of a Kind) 👌";
            case 2: return "兩對 (Two Pairs) ✌️";
            case 1: return "一對 (One Pair) 👍";
            default: return "高牌 (High Card)";
        }
    }
}