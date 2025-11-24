package gui;

import model.ChatMessage;
import network.MulticastReceiver;
import network.MulticastSender;
import utils.FileTransfer;
import utils.ThemeManager; // Import ThemeManager
import utils.UIHelper;

import javax.swing.*;
import javax.swing.border.TitledBorder; // Cần import để chỉnh màu tiêu đề border
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

public class ChatFrame extends JFrame {
    private JList<ChatMessage> chatList;
    private DefaultListModel<ChatMessage> chatModel;
    private JTextField inputField;
    private JButton sendButton;
    private JList<String> userList;
    private DefaultListModel<String> userModel;
    
    // Khai báo các Panel cần đổi màu làm biến toàn cục để truy cập trong applyTheme
    private JPanel inputPanel;
    private JScrollPane userScroll;
    private JPanel topPanel;

    private MulticastSender sender;
    private MulticastReceiver receiver;
    private ExecutorService receiverExecutor;
    private String currentNickname;
    
    private final Map<String, Long> onlineUsersMap = new ConcurrentHashMap<>();
    private ScheduledFuture<?> timeoutChecker;
    private static final long USER_TIMEOUT_MS = 8_000L;
    
    private List<ChatMessage> chatHistory = new ArrayList<>();
    private JLabel statusLabel;
    private JButton changeNameBtn, historyBtn, fileBtn, emojiBtn, searchBtn, themeBtn;
    private ScheduledExecutorService typingScheduler;
    private ScheduledFuture<?> typingTask;

    public ChatFrame() {
        super("Messenger LAN - Chưa đăng nhập");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1100, 750);
        setLocationRelativeTo(null);
        initializeUI();
        
        // Áp dụng theme mặc định ngay khi mở
        applyTheme(); 
        
        showNicknameDialog();
        setVisible(true);
    }

    private void initializeUI() {
        setLayout(new BorderLayout(5, 5));
        
        // === TOP BAR ===
        topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        topPanel.setBackground(new Color(0, 150, 136)); // Giữ màu xanh thương hiệu
        topPanel.setForeground(Color.WHITE);

        changeNameBtn = createTopButton("Đổi tên");
        historyBtn = createTopButton("Lịch sử");
        fileBtn = createTopButton("Gửi File");
        emojiBtn = createTopButton("Emoji");
        searchBtn = createTopButton("Tìm kiếm");

        changeNameBtn.addActionListener(e -> changeName());
        historyBtn.addActionListener(e -> showHistory());
        fileBtn.addActionListener(e -> sendFile());
        emojiBtn.addActionListener(e -> showEmojiPicker());
        searchBtn.addActionListener(e -> searchMessages());

        topPanel.add(changeNameBtn);
        topPanel.add(historyBtn);
        topPanel.add(fileBtn);
        topPanel.add(emojiBtn);
        topPanel.add(searchBtn);

        statusLabel = new JLabel("● Online");
        statusLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        statusLabel.setForeground(Color.WHITE);
        topPanel.add(Box.createHorizontalGlue());
        
        // Nút đổi theme lấy text dựa trên trạng thái hiện tại
        themeBtn = createTopButton(ThemeManager.isDark() ? "🌙 Tối" : "☀️ Sáng");
        themeBtn.addActionListener(e -> toggleTheme());
        topPanel.add(themeBtn);
        
        topPanel.add(statusLabel);

        add(topPanel, BorderLayout.NORTH);

        // === CHAT LIST ===
        chatModel = new DefaultListModel<>();
        chatList = new JList<>(chatModel);
        chatList.setCellRenderer(new MessagePanelRenderer()); // Renderer tự xử lý màu từng dòng
        chatList.setFixedCellHeight(-1);

        JScrollPane chatScroll = new JScrollPane(chatList);
        chatScroll.setBorder(null);
        add(chatScroll, BorderLayout.CENTER);

        // === USER LIST ===
        userModel = new DefaultListModel<>();
        userList = new JList<>(userModel);
        
        // Border cho user list
        TitledBorder userBorder = BorderFactory.createTitledBorder(
                BorderFactory.createEmptyBorder(10, 10, 10, 10), "👥 Người online");
        userList.setBorder(userBorder);

        userScroll = new JScrollPane(userList);
        userScroll.setPreferredSize(new Dimension(180, 0));
        add(userScroll, BorderLayout.EAST);

        // === INPUT PANEL ===
        inputPanel = new JPanel(new BorderLayout(8, 0));
        inputPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        inputField = new JTextField();
        inputField.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        inputField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() != KeyEvent.VK_ENTER) {
                    scheduleTypingNotification();
                }
            }
        });

        sendButton = new JButton("Gửi");
        sendButton.setBackground(new Color(0, 150, 136));
        sendButton.setForeground(Color.WHITE); // Chữ trắng cho nổi trên nền xanh
        sendButton.setFocusPainted(false);
        sendButton.setFont(new Font("Segoe UI", Font.BOLD, 13));
        sendButton.setPreferredSize(new Dimension(90, 40));
        sendButton.setCursor(new Cursor(Cursor.HAND_CURSOR));

        inputPanel.add(inputField, BorderLayout.CENTER);
        inputPanel.add(sendButton, BorderLayout.EAST);
        add(inputPanel, BorderLayout.SOUTH);

        // === ACTION LISTENERS ===
        Action sendAction = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sendMessage();
            }
        };
        inputField.addActionListener(sendAction);
        sendButton.addActionListener(sendAction);
    }

    // === HÀM XỬ LÝ THEME (QUAN TRỌNG) ===
    private void toggleTheme() {
        ThemeManager.toggle(); // Đổi trạng thái trong Manager
        themeBtn.setText(ThemeManager.isDark() ? "🌙 Tối" : "☀️ Sáng");
        applyTheme(); // Áp dụng màu mới
    }

    private void applyTheme() {
        Color mainBg = ThemeManager.getBackgroundColor();
        Color textCol = ThemeManager.getTextColor();
        Color inputBg = ThemeManager.getInputBgColor();
        
        // 1. Set màu cho các thành phần cơ bản
        getContentPane().setBackground(mainBg);
        
        chatList.setBackground(mainBg);
        chatList.setForeground(textCol);
        
        inputPanel.setBackground(mainBg);
        inputField.setBackground(inputBg);
        inputField.setForeground(textCol);
        inputField.setCaretColor(textCol);
        
        userList.setBackground(inputBg); 
        userList.setForeground(textCol);
        userScroll.getViewport().setBackground(inputBg);
        
        TitledBorder border = (TitledBorder) userList.getBorder();
        border.setTitleColor(textCol);
        
        // 2. Cập nhật giao diện tổng thể (Lệnh này sẽ reset màu nút, nên phải để ở đây)
        SwingUtilities.updateComponentTreeUI(this);
        
        // 3. SET MÀU NÚT GỬI (Quan trọng: Phải làm sau bước 2)
        sendButton.setBackground(new Color(0, 150, 136)); // Luôn giữ màu xanh Teal
        sendButton.setOpaque(true); // Bắt buộc để hiện màu nền trên một số hệ điều hành
        sendButton.setBorderPainted(false); // Bỏ viền lồi lõm cho đẹp phẳng
        
        // Logic đổi màu chữ: Tối -> Trắng, Sáng -> Đen
        if (ThemeManager.isDark()) {
            sendButton.setForeground(Color.WHITE);
        } else {
            sendButton.setForeground(Color.BLACK);
        }

        // 4. Vẽ lại danh sách chat
        chatList.repaint();
    }

    private JButton createTopButton(String text) {
        JButton btn = new JButton(text);
        btn.setForeground(Color.WHITE);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setContentAreaFilled(false);
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private void initializeNetwork(String nickname) {
        this.currentNickname = nickname;
        UIHelper.setCurrentUser(nickname);
        setTitle("Messenger LAN - " + nickname);

        try {
            sender = new MulticastSender(nickname);
            receiver = new MulticastReceiver(this::handleIncomingMessage);
            receiverExecutor = Executors.newSingleThreadExecutor(r -> {
                Thread t = new Thread(r, "Receiver-Thread");
                t.setDaemon(true);
                return t;
            });
            
            typingScheduler = Executors.newSingleThreadScheduledExecutor(r -> {
                Thread t = new Thread(r, "Typing-Scheduler");
                t.setDaemon(true);
                return t;
            });
            
            receiverExecutor.submit(receiver);
            
            // Tải lịch sử
            chatHistory = FileTransfer.loadHistory();
            SwingUtilities.invokeLater(() -> {
                for (ChatMessage msg : chatHistory) {
                    if (msg.type() != ChatMessage.Type.HEARTBEAT && 
                        msg.type() != ChatMessage.Type.TYPING) {
                        chatModel.addElement(msg);
                    }
                }
                scrollToBottom();
            });

            startTimeoutChecker();
            sender.startHeartbeat();
            sender.sendJoin();

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "❌ Không thể khởi tạo mạng: " + ex.getMessage(),
                    "Lỗi mạng", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
    }

    public void handleIncomingMessage(ChatMessage message) {
        SwingUtilities.invokeLater(() -> {
            if (message.type() == ChatMessage.Type.HEARTBEAT) {
                updateOnlineUser(message.sender());
                return;
            }

            if (message.type() == ChatMessage.Type.TYPING) {
                return;
            }

            if (message.type() == ChatMessage.Type.TEXT && 
                message.sender().equals(currentNickname)) {
                return;
            }

            chatModel.addElement(message);
            chatHistory.add(message);
            scrollToBottom();
        });
    }

    private void sendMessage() {
        String text = inputField.getText().trim();
        if (text.isEmpty() || sender == null) return;

        text = UIHelper.replaceEmoji(text);

        ChatMessage myMsg = new ChatMessage(text, currentNickname, ChatMessage.Type.TEXT);
        chatModel.addElement(myMsg);
        chatHistory.add(myMsg);
        scrollToBottom();

        sender.sendText(text);
        inputField.setText("");
        
        if (typingTask != null) {
            typingTask.cancel(true);
        }
        
        FileTransfer.saveHistory(chatHistory);
    }

    private void scheduleTypingNotification() {
        if (typingTask != null && !typingTask.isDone()) {
            return;
        }
        sender.sendTyping();
        typingTask = typingScheduler.schedule(() -> {}, 2, TimeUnit.SECONDS);
    }

    private void scrollToBottom() {
        int size = chatModel.size();
        if (size > 0) {
            chatList.ensureIndexIsVisible(size - 1);
        }
    }

    public void updateOnlineUser(String nickname) {
        onlineUsersMap.put(nickname, System.currentTimeMillis());

        SwingUtilities.invokeLater(() -> {
            boolean exists = false;
            for (int i = 0; i < userModel.size(); i++) {
                if (userModel.get(i).equals(nickname)) {
                    exists = true;
                    break;
                }
            }
            
            if (!exists && !nickname.equals(currentNickname)) {
                userModel.addElement(nickname);
                sortUserList();
            }
        });
    }

    private void sortUserList() {
        List<String> list = new ArrayList<>();
        for (int i = 0; i < userModel.size(); i++) {
            list.add(userModel.get(i));
        }
        list.sort(String::compareToIgnoreCase);
        userModel.clear();
        list.forEach(userModel::addElement);
    }

    private void startTimeoutChecker() {
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "Timeout-Checker");
            t.setDaemon(true);
            return t;
        });
        
        timeoutChecker = scheduler.scheduleAtFixedRate(() -> {
            long now = System.currentTimeMillis();
            java.util.List<String> offlineUsers = new ArrayList<>();
            
            for (Map.Entry<String, Long> entry : onlineUsersMap.entrySet()) {
                if (now - entry.getValue() > USER_TIMEOUT_MS) {
                    if (!entry.getKey().equals(currentNickname)) {
                        offlineUsers.add(entry.getKey());
                    }
                }
            }

            for (String user : offlineUsers) {
                onlineUsersMap.remove(user);
                SwingUtilities.invokeLater(() -> {
                    userModel.removeElement(user);
                    addSystemMessage(user + " ⚠️ đã offline");
                });
            }
        }, 2, 2, TimeUnit.SECONDS);
    }

    private void addSystemMessage(String message) {
        ChatMessage sysMsg = new ChatMessage(message, "SYSTEM", ChatMessage.Type.SYSTEM);
        chatModel.addElement(sysMsg);
        chatHistory.add(sysMsg);
        scrollToBottom();
    }

    private void changeName() {
        String newName = JOptionPane.showInputDialog(this, "Nhập tên mới:", currentNickname);
        if (newName != null && !newName.trim().isEmpty()) {
            sender.sendLeave();
            currentNickname = newName.trim();
            UIHelper.setCurrentUser(currentNickname);
            setTitle("Messenger LAN - " + currentNickname);
            sender.sendJoin();
            addSystemMessage("📝 Bạn đã đổi tên thành " + currentNickname);
        }
    }

    private void showHistory() {
        StringBuilder history = new StringBuilder();
        for (ChatMessage msg : chatHistory) {
            history.append(String.format("[%s] %s: %s\n",
                    msg.getFormattedTime(), msg.sender(), msg.content()));
        }

        JTextArea textArea = new JTextArea(history.toString());
        textArea.setEditable(false);
        textArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JScrollPane scroll = new JScrollPane(textArea);
        scroll.setPreferredSize(new Dimension(500, 400));

        int result = JOptionPane.showConfirmDialog(this, scroll,
                "📋 Lịch sử Chat (" + chatHistory.size() + " tin)",
                JOptionPane.OK_CANCEL_OPTION);
        
        if (result == JOptionPane.OK_OPTION) {
            FileTransfer.exportChatToTxt(chatHistory);
        }
    }

    private void sendFile() {
        JFileChooser chooser = new JFileChooser();
        int result = chooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            FileTransfer.sendFile(file, currentNickname);
            addSystemMessage("📤 Gửi file: " + file.getName());
        }
    }

    private void showEmojiPicker() {
        String[] emojis = {"😊", "😂", "❤️", "👍", "🎉", "😍", "🔥", "👌", "😭", "🤔"};
        String emoji = (String) JOptionPane.showInputDialog(this,
                "Chọn emoji:", "😊 Emoji Picker",
                JOptionPane.PLAIN_MESSAGE, null, emojis, emojis[0]);
        if (emoji != null) {
            inputField.setText(inputField.getText() + emoji);
        }
    }

    private void searchMessages() {
        String keyword = JOptionPane.showInputDialog(this, "Tìm kiếm tin nhắn:");
        if (keyword != null && !keyword.trim().isEmpty()) {
            List<ChatMessage> results = FileTransfer.searchMessages(chatHistory, keyword);
            StringBuilder found = new StringBuilder();
            for (ChatMessage msg : results) {
                found.append(String.format("[%s] %s: %s\n",
                        msg.getFormattedTime(), msg.sender(), msg.content()));
            }

            JTextArea textArea = new JTextArea(found.toString());
            textArea.setEditable(false);
            textArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
            JScrollPane scroll = new JScrollPane(textArea);
            scroll.setPreferredSize(new Dimension(500, 300));

            JOptionPane.showMessageDialog(this, scroll,
                    "🔍 Kết quả tìm kiếm: " + results.size() + " tin",
                    JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void showNicknameDialog() {
        String nick;
        while (true) {
            nick = JOptionPane.showInputDialog(this,
                    "Nhập tên hiển thị của bạn:", "Đăng nhập",
                    JOptionPane.PLAIN_MESSAGE);
            if (nick == null) System.exit(0);
            nick = nick.trim();
            if (!nick.isEmpty()) break;
            JOptionPane.showMessageDialog(this, 
                "Tên không được để trống!", "Lỗi", JOptionPane.WARNING_MESSAGE);
        }
        initializeNetwork(nick);
    }

    @Override
    public void dispose() {
        try {
            if (sender != null) {
                sender.sendLeave();
                sender.stopHeartbeat();
            }
            FileTransfer.saveHistory(chatHistory);
            if (receiverExecutor != null) receiverExecutor.shutdownNow();
            if (typingScheduler != null) typingScheduler.shutdownNow();
            if (timeoutChecker != null) timeoutChecker.cancel(true);
        } catch (Exception e) {
            System.err.println("Lỗi đóng: " + e.getMessage());
        }
        super.dispose();
    }
}