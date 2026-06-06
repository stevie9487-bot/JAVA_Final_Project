package Pokergames;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
    private static final int PORT = 12345;
    // 撲克牌遊戲通常是 2 到 5 人，這裡預設最多 5 個玩家
    private static final int MAX_PLAYERS = 5; 
    // 用來管理所有玩家執行緒的連接池
    private static ExecutorService pool = Executors.newFixedThreadPool(MAX_PLAYERS);
    // 儲存所有線上玩家的處理器，方便之後廣播訊息
    private static List<PlayerHandler> players = new ArrayList<>();
    
    private static DeckOfCards deck;

    public static void main(String[] args) {
        System.out.println("【撲克牌伺服器】正在啟動，監聽連接埠: " + PORT);
        deck = new DeckOfCards();
        // 遊戲開始前先洗牌
        deck.shuffle(); 

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            while (players.size() < MAX_PLAYERS) {
                System.out.println("等待玩家連線中... (目前人數: " + players.size() + "/" + MAX_PLAYERS + ")");
                
                Socket clientSocket = serverSocket.accept();
                System.out.println("新玩家已連線，來自: " + clientSocket.getRemoteSocketAddress());
                
                // 為這個玩家建立一個專屬的獨立執行緒處理器
                PlayerHandler playerHandler = new PlayerHandler(clientSocket);
                players.add(playerHandler);
                
                // 丟進執行緒池開始執行，讓 accept() 可以立刻去等下一個人
                pool.execute(playerHandler);
            }
            
            System.out.println("人數已滿，遊戲即將開始！");
            
        } catch (IOException e) {
            System.err.println("伺服器異常: " + e.getMessage());
        }
    }
}

// ========================================================
// 專屬處理單一玩家通訊的執行緒類別 (一連線一執行緒)
// ========================================================
class PlayerHandler implements Runnable {
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;

    public PlayerHandler(Socket socket) {
        this.socket = socket;
        try {
            this.in = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
            this.out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"), true);
        } catch (IOException e) {
            System.err.println("玩家通訊串流建立失敗: " + e.getMessage());
        }
    }

    @Override
    public void run() {
        try {
            // 向剛連進來的玩家打招呼
            out.println("歡迎來到 Java 線上撲克牌遊戲！等待其他玩家中...");

            String clientMessage;
            // 持續監聽這個玩家傳過來的任何指令（例如：BET, FOLD, CHECK）
            while ((clientMessage = in.readLine()) != null) {
                System.out.println("收到玩家訊息: " + clientMessage);
                
                // TODO: 這裡之後要串接 PokerGameManager 判斷玩家的動作
                out.println("伺服器已收到你的動作: " + clientMessage);
            }
        } catch (IOException e) {
            System.out.println("玩家斷開連線。");
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
