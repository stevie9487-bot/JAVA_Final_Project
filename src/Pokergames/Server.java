package Pokergames;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
    private static final int PORT = 12345;
    private static final int MAX_PLAYERS = 2; // 測試用 2 人，可自由改動
    private static ExecutorService pool = Executors.newFixedThreadPool(MAX_PLAYERS);
    private static List<PlayerHandler> players = new ArrayList<>();
    private static DeckOfCards deck;
    
    private static int turnIndex = 0; 
    private static int currentMaxBet = 0; 

    public static void main(String[] args) {
        System.out.println("【撲克牌伺服器】正在啟動...");
        deck = new DeckOfCards();
        deck.shuffle();

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            while (players.size() < MAX_PLAYERS) {
                Socket clientSocket = serverSocket.accept();
                int playerId = players.size() + 1;
                
                PlayerHandler playerHandler = new PlayerHandler(clientSocket, playerId);
                players.add(playerHandler);
                pool.execute(playerHandler);
            }
            startGame();
        } catch (IOException e) {
            System.err.println("伺服器異常: " + e.getMessage());
        }
    }

    private static void startGame() {
        currentMaxBet = 0; 
        turnIndex = 0;
        
        broadcast("\n======================================");
        broadcast("🃏✨ 【新對局開始】現已進入官方標準『換牌撲克』規則！ ✨🃏");
        broadcast("【規則說明】請先看您的起手牌。輪到你時，勾選想換掉的牌，並決定下注額。");
        broadcast("======================================");

        for (PlayerHandler player : players) {
            for (int i = 0; i < 5; i++) {
                player.addCard(deck.dealCard());
            }
            player.sendHandAndResult();
        }
        sendTurnNotification();
    }

    public static void sendTurnNotification() {
        for (int i = 0; i < players.size(); i++) {
            if (i == turnIndex) {
                players.get(i).sendMessage("\n【👉 輪到你的回合】場上最高注額為: $" + currentMaxBet + "。\n請[勾選欲換手牌]並點擊確認，或點擊蓋牌！");
            } else {
                players.get(i).sendMessage("【⏳ 請稍候】正在等待 [玩家 " + (turnIndex + 1) + "] 換牌下注...");
            }
        }
    }

    public static synchronized void handlePlayerAction(int playerId, String clientMessage) {
        if (playerId != (turnIndex + 1)) {
            players.get(playerId - 1).sendMessage("⚠️ 目前不是你的回合，請等待！");
            return;
        }

        String[] parts = clientMessage.split(" ");
        String actionType = parts[0];

        if ("ACTION".equals(actionType)) {
            int betAmount = 0;
            try {
                betAmount = Integer.parseInt(parts[1]);
            } catch (Exception e) {
                players.get(playerId - 1).sendMessage("⚠️ 請輸入有效的下注數字！");
                return;
            }

            if (betAmount < currentMaxBet) {
                players.get(playerId - 1).sendMessage("⚠️ 動作失敗！下注金額不能低於最高注額: $" + currentMaxBet);
                return; 
            }
            
            currentMaxBet = betAmount;
            String discardData = parts[2];

            PlayerHandler p = players.get(playerId - 1);
            if (!"none".equals(discardData)) {
                String[] indices = discardData.split(",");
                int replaceCount = indices.length;
                broadcast("[玩家 " + playerId + "] 選擇換掉 " + replaceCount + " 張牌，並下注: $" + betAmount);
                
                for (String idxStr : indices) {
                    int index = Integer.parseInt(idxStr);
                    Card newCard = deck.dealCard();
                    p.replaceCard(index, newCard);
                }
                p.sendMessage("【系統】換牌成功！");
                p.sendHandAndResult();
            } else {
                broadcast("[玩家 " + playerId + "] 決定不換牌，直接下注: $" + betAmount);
            }

        } else {
            broadcast("[玩家 " + playerId + "] 選擇了: 蓋牌 (FOLD)");
        }

        turnIndex++;

        if (turnIndex >= players.size()) {
            checkWinner();
        } else {
            sendTurnNotification();
        }
    }

    private static void checkWinner() {
        broadcast("\n======================================");
        broadcast("【📢 📢 全場最終開牌比牌結果】");
        broadcast("======================================");

        PlayerHandler winner = null;
        int highestScore = -1;

        for (PlayerHandler player : players) {
            int score = HandEvaluator.getHandValue(player.getHand());
            String handName = HandEvaluator.getHandName(score);
            broadcast("[玩家 " + player.getPlayerId() + "] 換牌後的最終手牌: " + player.getHand().toString() + " -> " + handName);

            if (score > highestScore) {
                highestScore = score;
                winner = player;
            }
        }

        if (winner != null) {
            broadcast("\n🏆✨✨ 本局最後大贏家是: [玩家 " + winner.getPlayerId() + "] ！！！ ✨✨🏆");
        }
        broadcast("======================================");
        
        broadcast("【系統】對局結束，5 秒後自動刷新牌堆，開啟下一局對戰...");
        new Thread(() -> {
            try {
                Thread.sleep(5000);
                resetGame();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }

    private static void resetGame() {
        deck = new DeckOfCards();
        deck.shuffle();
        for (PlayerHandler player : players) {
            player.clearHand();
        }
        startGame();
    }

    public static void broadcast(String message) {
        for (PlayerHandler player : players) {
            player.sendMessage(message);
        }
    }
}

class PlayerHandler implements Runnable {
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private int playerId;
    private List<Card> hand = new ArrayList<>();

    public PlayerHandler(Socket socket, int playerId) {
        this.socket = socket;
        this.playerId = playerId;
        try {
            this.in = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
            this.out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"), true);
        } catch (IOException e) {
            System.err.println("串流異常: " + e.getMessage());
        }
    }

    public void addCard(Card card) { if (card != null) hand.add(card); }
    
    public void replaceCard(int index, Card newCard) {
        if (index >= 0 && index < hand.size() && newCard != null) {
            hand.set(index, newCard);
        }
    }

    public void clearHand() { this.hand.clear(); }
    public void sendMessage(String message) { if (out != null) out.println(message); }
    public int getPlayerId() { return this.playerId; }
    public List<Card> getHand() { return this.hand; }

    public void sendHandAndResult() {
        out.println("【你的手牌】: " + hand.toString());
        int score = HandEvaluator.getHandValue(hand);
        out.println("【牌型判定】: 您目前的牌型是 —— " + HandEvaluator.getHandName(score));
    }

    @Override
    public void run() {
        try {
            out.println("歡迎來到 Java 線上撲克牌遊戲！您是 [玩家 " + playerId + "]。");
            out.println("正在等待其他玩家加入以啟動發牌...");

            String clientMessage;
            while ((clientMessage = in.readLine()) != null) {
                Server.handlePlayerAction(playerId, clientMessage);
            }
        } catch (IOException e) {
            System.out.println("玩家 " + playerId + " 斷開連線。");
        } finally {
            try { socket.close(); } catch (IOException e) { e.printStackTrace(); }
        }
    }
}