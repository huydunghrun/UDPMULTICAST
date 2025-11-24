package gui;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import model.ChatMessage;
import utils.UIHelper;

public class MessagePanel extends JPanel {
    private final ChatMessage msg;
    private JLabel seenLabel;

    public MessagePanel(ChatMessage msg, String myName) {
        this.msg = msg;
        setLayout(new BorderLayout());
        setBackground(new Color(240, 242, 245));
        setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
        setOpaque(true);

        boolean isMe = msg.sender().equals(myName);

        // SYSTEM, TYPING, JOIN, LEAVE messages
        if (msg.type() == ChatMessage.Type.SYSTEM || 
            msg.type() == ChatMessage.Type.TYPING ||
            msg.type() == ChatMessage.Type.JOIN || 
            msg.type() == ChatMessage.Type.LEAVE) {
            
            JLabel l = new JLabel(msg.content());
            l.setForeground(new Color(100, 100, 100));
            l.setHorizontalAlignment(SwingConstants.CENTER);
            l.setFont(new Font("Segoe UI", Font.ITALIC, 13));
            add(l, BorderLayout.CENTER);
            return;
        }

        JPanel wrapper = new JPanel(new FlowLayout(isMe ? FlowLayout.RIGHT : FlowLayout.LEFT, 10, 0));
        wrapper.setOpaque(false);

        // Avatar cho tin nhắn từ người khác
        if (!isMe) {
            JLabel avatar = new JLabel(UIHelper.getInitial(msg.sender()));
            avatar.setFont(new Font("Segoe UI", Font.BOLD, 15));
            avatar.setForeground(Color.WHITE);
            avatar.setHorizontalAlignment(SwingConstants.CENTER);
            avatar.setOpaque(true);
            avatar.setBackground(UIHelper.getColorForUser(msg.sender()));
            avatar.setPreferredSize(new Dimension(40, 40));
            avatar.setBorder(BorderFactory.createLineBorder(Color.WHITE, 2));
            wrapper.add(avatar);
        }

        // Nội dung tin nhắn
        JTextArea text = new JTextArea(msg.content());
        text.setLineWrap(true);
        text.setWrapStyleWord(true);
        text.setEditable(false);
        text.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        text.setMargin(new Insets(10, 16, 10, 16));
        text.setBackground(isMe ? new Color(0, 122, 255) : Color.WHITE);
        text.setForeground(isMe ? Color.WHITE : Color.BLACK);
        text.setBorder(BorderFactory.createLineBorder(
            isMe ? new Color(0, 100, 220) : new Color(220, 220, 220), 1));

        // Footer với thời gian + seen
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 2));
        footer.setOpaque(false);
        
        JLabel time = new JLabel(msg.getFormattedTime());
        time.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        time.setForeground(isMe ? new Color(200, 230, 255) : new Color(170, 170, 170));

        seenLabel = new JLabel("✓ Seen");
        seenLabel.setFont(new Font("Segoe UI", Font.BOLD, 10));
        seenLabel.setForeground(new Color(80, 200, 255));
        seenLabel.setVisible(msg.isSeen());

        footer.add(time);
        footer.add(seenLabel);

        // Content panel
        JPanel content = new JPanel(new BorderLayout());
        content.setOpaque(false);
        content.add(text, BorderLayout.CENTER);
        content.add(footer, BorderLayout.SOUTH);

        wrapper.add(content);
        add(wrapper, BorderLayout.CENTER);
    }

    public String getSender() {
        return msg.sender();
    }

    public boolean isTypingMessage() {
        return msg.type() == ChatMessage.Type.TYPING;
    }

    public void markAsSeen() {
        msg.setSeen(true);
        seenLabel.setVisible(true);
    }
}