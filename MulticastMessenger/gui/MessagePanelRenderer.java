package gui;

import model.ChatMessage;
import utils.ThemeManager; // Import file vừa tạo
import utils.UIHelper;

import javax.swing.*;
import javax.swing.border.AbstractBorder;
import java.awt.*;
import java.time.LocalDateTime;

public class MessagePanelRenderer extends JPanel implements ListCellRenderer<ChatMessage> {
    private ChatMessage msg;
    private String currentUser;
    private static String lastSender = "";
    private static LocalDateTime lastTime = null;

    public MessagePanelRenderer() {
        setOpaque(true);
    }

    @Override
    public Component getListCellRendererComponent(JList<? extends ChatMessage> list,
                                                  ChatMessage value,
                                                  int index,
                                                  boolean isSelected,
                                                  boolean cellHasFocus) {
        this.msg = value;
        this.currentUser = UIHelper.getCurrentUser();

        if (msg == null) return this;

        // 1. LẤY MÀU NỀN TỪ THEME MANAGER
        setBackground(ThemeManager.getBackgroundColor());
        
        removeAll();
        setLayout(new BorderLayout(0, 0));
        setOpaque(true);

        // SYSTEM messages
        if (msg.type() == ChatMessage.Type.SYSTEM || 
            msg.type() == ChatMessage.Type.JOIN || 
            msg.type() == ChatMessage.Type.LEAVE) {
            
            setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
            JLabel l = new JLabel(msg.content());
            l.setForeground(new Color(150, 150, 150)); // Màu xám trung tính dùng được cho cả 2
            l.setHorizontalAlignment(SwingConstants.CENTER);
            l.setFont(new Font("Segoe UI", Font.ITALIC, 11));
            add(l, BorderLayout.CENTER);
            setPreferredSize(new Dimension(list.getWidth(), 32));
            return this;
        }

        boolean isMe = msg.sender().equals(currentUser);
        boolean sameSenderAsLast = msg.sender().equals(lastSender);
        boolean showTime = lastTime == null || msg.timestamp().getMinute() != lastTime.getMinute();

        lastSender = msg.sender();
        lastTime = msg.timestamp();

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setOpaque(false);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(2, 12, 2, 12));

        if (showTime) {
            JLabel timeLabel = new JLabel(msg.getFormattedTime());
            timeLabel.setFont(new Font("Segoe UI", Font.PLAIN, 10));
            timeLabel.setForeground(new Color(130, 130, 130));
            timeLabel.setHorizontalAlignment(SwingConstants.CENTER);
            mainPanel.add(timeLabel, BorderLayout.NORTH);
        }

        JPanel messageWrapper = new JPanel(new BorderLayout());
        messageWrapper.setOpaque(false);
        messageWrapper.setBorder(BorderFactory.createEmptyBorder(3, 0, 3, 0));

        if (isMe) {
            JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.TRAILING, 0, 0));
            rightPanel.setOpaque(false);
            rightPanel.add(createBubble(isMe));
            messageWrapper.add(rightPanel, BorderLayout.CENTER);
        } else {
            JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEADING, 8, 0));
            leftPanel.setOpaque(false);

            if (!sameSenderAsLast) {
                JLabel avatar = new JLabel(UIHelper.getInitial(msg.sender()));
                avatar.setFont(new Font("Segoe UI", Font.BOLD, 11));
                avatar.setForeground(Color.WHITE);
                avatar.setHorizontalAlignment(SwingConstants.CENTER);
                avatar.setOpaque(true);
                avatar.setBackground(UIHelper.getColorForUser(msg.sender()));
                avatar.setPreferredSize(new Dimension(32, 32));
                // Nếu chế độ sáng thì ko cần viền trắng, tối thì cần
                if (ThemeManager.isDark()) {
                    avatar.setBorder(BorderFactory.createLineBorder(Color.WHITE, 1));
                } else {
                    avatar.setBorder(null);
                }
                leftPanel.add(avatar);
            } else {
                leftPanel.add(Box.createHorizontalStrut(40));
            }
            leftPanel.add(createBubble(isMe));
            messageWrapper.add(leftPanel, BorderLayout.CENTER);
        }

        mainPanel.add(messageWrapper, BorderLayout.CENTER);
        add(mainPanel, BorderLayout.CENTER);
        setPreferredSize(new Dimension(list.getWidth(), showTime ? 60 : 42));
        return this;
    }

    private JComponent createBubble(boolean isMe) {
        String content = msg.content();
        if (content.length() > 40) content = "<html><body style='width: 220px; word-wrap: break-word'>" + escapeHtml(content) + "</body></html>";
        else content = escapeHtml(content);

        JLabel bubbleLabel = new JLabel(content);
        bubbleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        bubbleLabel.setOpaque(true);

        // 2. LOGIC MÀU BONG BÓNG TỰ ĐỘNG
        if (isMe) {
            bubbleLabel.setBackground(ThemeManager.MY_BUBBLE_BG);
            bubbleLabel.setForeground(ThemeManager.MY_TEXT);
            // Mình gửi thì dùng viền tối hơn nền một chút
            bubbleLabel.setBorder(new RoundBubbleBorder(ThemeManager.MY_BUBBLE_BG.darker(), 16));
        } else {
            bubbleLabel.setBackground(ThemeManager.getOtherBubbleColor());
            bubbleLabel.setForeground(ThemeManager.getTextColor());
            bubbleLabel.setBorder(new RoundBubbleBorder(ThemeManager.getBorderColor(), 16));
        }

        bubbleLabel.setVerticalAlignment(SwingConstants.TOP);
        bubbleLabel.setHorizontalAlignment(SwingConstants.LEFT);
        bubbleLabel.setPreferredSize(new Dimension(240, content.contains("<html>") ? 60 : 35));
        
        return bubbleLabel;
    }
    
    // ... Giữ nguyên hàm escapeHtml và class RoundBubbleBorder ...
    private String escapeHtml(String text) {
        return text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;").replace("'", "&#39;");
    }
}
// ... Copy lại class RoundBubbleBorder ở bài trước vào đây ...
class RoundBubbleBorder extends AbstractBorder {
    private final Color color;
    private final int radius;
    public RoundBubbleBorder(Color color, int radius) { this.color = color; this.radius = radius; }
    @Override
    public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(color);
        g2.setStroke(new BasicStroke(1.0f));
        g2.drawRoundRect(x, y, w - 1, h - 1, radius, radius);
        g2.dispose();
    }
    @Override
    public Insets getBorderInsets(Component c) { return new Insets(8, 12, 8, 12); }
}