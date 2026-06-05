package Pokergames;

public class Card {
    // 每一張牌都有點數和花色
    private final Face face;
    private final Suit suit;

    // 建庫子：初始化這張牌的點數與花色 [cite: 20]
    public Card(Face face, Suit suit) {
        this.face = face;
        this.suit = suit;
    }

    // 取得點數的方法 (後面判斷牌型會用到) [cite: 7]
    public Face getFace() {
        return face;
    }

    // 取得花色的方法 (後面判斷同花會用到) [cite: 7]
    public Suit getSuit() {
        return suit;
    }

    // 讓這張牌印出來時好看（例如：顯示 "SPADES_ACE" 或 "HEARTS_SEVEN"） [cite: 23]
    @Override
    public String toString() {
        return suit + "_" + face;
    }
}