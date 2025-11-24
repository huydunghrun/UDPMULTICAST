package src;

import gui.ChatFrame;
import javax.swing.UIManager;

public class Main {
    public static void main(String[] args) {
        try {
            // Đặt look and feel cho Swing
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            System.err.println("Không thể set look and feel: " + e.getMessage());
        }

        System.out.println("🚀 Khởi động Messenger LAN...");
        System.out.println("📡 Multicast Group: 239.1.1.1:12345");
        
        javax.swing.SwingUtilities.invokeLater(ChatFrame::new);
    }
}