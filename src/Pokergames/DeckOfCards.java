package Pokergames;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class DeckOfCards {
    private final List<Card> deck; // 用來存 52 張牌的清單
    private int currentCard;       // 記錄目前發到第幾張牌
    private final Random randomObject;

    // 建構子
    public DeckOfCards() {
        deck = new ArrayList<>();
        currentCard = 0;
        randomObject = new Random();

        // 雙重迴圈：自動組合 4 種花色 x 13 種點數 = 52 張牌 [cite: 20, 21, 22]
        for (Suit suit : Suit.values()) {
            for (Face face : Face.values()) {
                deck.add(new Card(face, suit));
            }
        }
    }

    // 實作 Fisher-Yates 洗牌演算法 
    public void shuffle() {
        currentCard = 0; // 洗牌後，發牌位置歸零

        // 從最後一張牌開始往前跑
        for (int first = deck.size() - 1; first > 0; first--) {
            // 隨機選出一個索引值 (0 到 first 之間)
            int second = randomObject.nextInt(first + 1);

            // 將目前的牌 (first) 與隨機選出的牌 (second) 交換位置
            Card temp = deck.get(first);
            deck.set(first, deck.get(second));
            deck.set(second, temp);
        }
    }

    // 發一張牌的方法
    public Card dealCard() {
        // 如果還有牌可以發
        if (currentCard < deck.size()) {
            return deck.get(currentCard++);
        } else {
            return null; // 牌發完了
        }
    }
    
    /**
     * 🌟 這是專門用來在報告中演示 GitHub 團隊協作與功能的工具方法。
     * 功能：在 Console 中依序印出目前牌堆中剩餘的所有撲克牌（方便開發除錯與簡報演示）。
     */
    public void printDeckDebug() {
        System.out.println("\n--- 🛠️ [Git 演示工具] 目前牌堆剩餘卡牌列表 ---");
        if (deck == null || deck.isEmpty()) {
            System.out.println("牌堆是空的！");
            return;
        }
        
        int count = 1;
        // 從目前準備要發的這張牌 (currentCard) 開始，印到最後一張
        for (int i = currentCard; i < deck.size(); i++) {
            System.out.print(deck.get(i) + "\t");
            if (count % 4 == 0) {
                System.out.println(); // 每 4 張牌換一行，畫面比較漂亮
            }
            count++;
        }
        System.out.println("\n-----------------------------------------\n");
    }
}

