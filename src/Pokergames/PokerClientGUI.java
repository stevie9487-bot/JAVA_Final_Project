package Pokergames;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.*;

public class PokerClientGUI extends JFrame {
    private JTextArea txtLog;       
    private JPanel panelCards;      
    private JTextField txtBet;      
    private JButton btnAction, btnFold; 
    
    // 🌟 新增：5 個勾選框，讓玩家對應自己的 5 張手牌
    private JCheckBox[] chkDiscard = new JCheckBox[5];

    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private final String SERVER_IP = "localhost";
    private final int SERVER_PORT = 12345;
    private boolean myTurn = false;

    public PokerClientGUI() {
        super("Java 線上撲克牌遊戲 - 最終完全體");
        setLayout(new BorderLayout());

        // 1. 中間牌桌區
        JPanel centerPanel = new JPanel(new GridLayout(2, 1));
        
        // 牌桌內部分為：手牌文字區 與 勾選框區
        panelCards = new JPanel(new BorderLayout());
        panelCards.setBackground(new Color(0, 100, 0));
        
        JPanel checkPanel = new JPanel(new FlowLayout());
        checkPanel.setBackground(new Color(0, 100, 0));
        for (int i = 0; i < 5; i++) {
            chkDiscard[i] = new JCheckBox("換第 " + (i + 1) + " 張");
            chkDiscard[i].setForeground(Color.WHITE);
            chkDiscard[i].setBackground(new Color(0, 100, 0));
            checkPanel.add(chkDiscard[i]);
        }
        panelCards.add(checkPanel, BorderLayout.SOUTH);
        
        txtLog = new JTextArea();
        txtLog.setEditable(false);
        JScrollPane scrollLog = new JScrollPane(txtLog);

        centerPanel.add(panelCards);
        centerPanel.add(scrollLog);
        add(centerPanel, BorderLayout.CENTER);

        // 2. 下方控制面板
        JPanel bottomPanel = new JPanel(new FlowLayout());
        bottomPanel.add(new JLabel("下注金額:"));
        txtBet = new JTextField("100", 5);
        bottomPanel.add(txtBet);

        // 將動作簡化為：確認執行（換牌+下注）與 蓋牌
        btnAction = new JButton("確認換牌並下注 (Submit Action)");
        btnFold = new JButton("蓋牌 (Fold)");

        bottomPanel.add(btnAction);
        bottomPanel.add(btnFold);
        add(bottomPanel, BorderLayout.SOUTH);

        // 事件綁定
        btnAction.addActionListener(e -> submitTurnAction());
        btnFold.addActionListener(e -> sendAction("FOLD"));

        setSize(650, 550);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        connectToServer();
    }

    // 🌟 打包換牌與下注的資訊送給伺服器
    private void submitTurnAction() {
        StringBuilder discardIndices = new StringBuilder();
        for (int i = 0; i < 5; i++) {
            if (chkDiscard[i].isSelected()) {
                discardIndices.append(i).append(",");
            }
        }
        // 指令格式: ACTION [下注額] [要換的索引，用逗號隔開，若不換就寫 none]
        String discards = discardIndices.length() > 0 ? discardIndices.toString() : "none";
        String betStr = txtBet.getText().trim();
        
        sendAction("ACTION " + betStr + " " + discards);
        
        // 送出後重置勾選框
        for (JCheckBox chk : chkDiscard) chk.setSelected(false);
    }

    private void connectToServer() {
        try {
            socket = new Socket(SERVER_IP, SERVER_PORT);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
            out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"), true);
            
            txtLog.append("成功連線至伺服器！\n");

            new Thread(() -> {
                try {
                    String serverMessage;
                    while ((serverMessage = in.readLine()) != null) {
                        String msg = serverMessage;
                        SwingUtilities.invokeLater(() -> {
                            txtLog.append(msg + "\n");
                            // 自動滾動到最下方
                            txtLog.setCaretPosition(txtLog.getDocument().getLength());
                        });
                    }
                } catch (IOException e) {
                    SwingUtilities.invokeLater(() -> txtLog.append("與伺服器斷開連線。\n"));
                }
            }).start();

        } catch (IOException e) {
            txtLog.append("無法連線至伺服器: " + e.getMessage() + "\n");
        }
    }

    private void sendAction(String action) {
        if (out != null) {
            out.println(action);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new PokerClientGUI().setVisible(true));
    }
}