package Pokergames;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.*;

public class PokerClientGUI extends JFrame {
    private JTextArea txtLog;       // 遊戲訊息日誌區
    private JPanel panelCards;      // 未來放撲克牌圖片的區域
    private JTextField txtBet;      // 輸入下注金額的框框
    private JButton btnBet, btnFold, btnCheck; // 動作按鈕

    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private final String SERVER_IP = "localhost";
    private final int SERVER_PORT = 12345;

    public PokerClientGUI() {
        super("Java 線上撲克牌遊戲 - 玩家視窗");
        setLayout(new BorderLayout());

        // 1. 中間區域：上方放牌、下方放文字日誌
        JPanel centerPanel = new JPanel(new GridLayout(2, 1));
        
        panelCards = new JPanel(new FlowLayout());
        panelCards.setBackground(new Color(0, 100, 0)); // 經典撲克牌綠底桌布
        panelCards.add(new JLabel(new ImageIcon())); // 預留給撲克牌圖片的位置
        
        txtLog = new JTextArea();
        txtLog.setEditable(false);
        JScrollPane scrollLog = new JScrollPane(txtLog);

        centerPanel.add(panelCards);
        centerPanel.add(scrollLog);
        add(centerPanel, BorderLayout.CENTER);

        // 2. 下方區域：控制操作面板（按鈕與下注框）
        JPanel bottomPanel = new JPanel(new FlowLayout());
        bottomPanel.add(new JLabel("下注金額:"));
        txtBet = new JTextField("100", 5);
        bottomPanel.add(txtBet);

        btnBet = new JButton("下注 (Bet)");
        btnCheck = new JButton("過牌 (Check)");
        btnFold = new JButton("蓋牌 (Fold)");

        bottomPanel.add(btnBet);
        bottomPanel.add(btnCheck);
        bottomPanel.add(btnFold);
        add(bottomPanel, BorderLayout.SOUTH);

        // 按鈕事件綁定：把動作傳給伺服器
        btnBet.addActionListener(e -> sendAction("BET " + txtBet.getText().trim()));
        btnCheck.addActionListener(e -> sendAction("CHECK"));
        btnFold.addActionListener(e -> sendAction("FOLD"));

        // 視窗基本設定
        setSize(600, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // 畫面做完後，開始連線
        connectToServer();
    }

    // 連線至伺服器，並開闢「背景執行緒」專門聽伺服器的廣播
    private void connectToServer() {
        try {
            socket = new Socket(SERVER_IP, SERVER_PORT);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
            out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"), true);
            
            txtLog.append("成功連線至伺服器！\n");

            // 【進階技巧】開闢背景執行緒（Thread），這樣讀取伺服器訊息時才不會讓 GUI 畫面卡死
            new Thread(() -> {
                try {
                    String serverMessage;
                    while ((serverMessage = in.readLine()) != null) {
                        // 收到伺服器傳來的話，就把它加進文字區
                        String msg = serverMessage;
                        SwingUtilities.invokeLater(() -> txtLog.append(msg + "\n"));
                    }
                } catch (IOException e) {
                    SwingUtilities.invokeLater(() -> txtLog.append("與伺服器斷開連線。\n"));
                }
            }).start();

        } catch (IOException e) {
            txtLog.append("無法連線至伺服器: " + e.getMessage() + "\n");
        }
    }

    // 送出動作給伺服器
    private void sendAction(String action) {
        if (out != null) {
            out.println(action);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new PokerClientGUI().setVisible(true);
        });
    }
}