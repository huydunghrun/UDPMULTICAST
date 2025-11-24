package utils;

import model.ChatMessage;
import javax.swing.*;
import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.function.Consumer;

public class FileTransfer {
    private static final String HISTORY_FILE = "chat_history.dat";
    private static final String DOWNLOAD_DIR = "Downloads";

    static {
        new File(DOWNLOAD_DIR).mkdirs();
    }

    // === LƯỚI SỬ CHAT ===
    
    public static void saveHistory(List<ChatMessage> history) {
        try (ObjectOutputStream oos = new ObjectOutputStream(
                new FileOutputStream(HISTORY_FILE))) {
            oos.writeObject(history);
            System.out.println("✓ Lưu lịch sử: " + history.size() + " tin nhắn");
        } catch (IOException e) {
            System.err.println("✗ Lỗi lưu lịch sử: " + e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    public static List<ChatMessage> loadHistory() {
        List<ChatMessage> history = new ArrayList<>();
        File file = new File(HISTORY_FILE);
        
        if (!file.exists()) {
            return history;
        }

        try (ObjectInputStream ois = new ObjectInputStream(
                new FileInputStream(file))) {
            history = (List<ChatMessage>) ois.readObject();
            System.out.println("✓ Tải lịch sử: " + history.size() + " tin nhắn");
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("✗ Lỗi tải lịch sử: " + e.getMessage());
        }
        
        return history;
    }

    public static void clearHistory() {
        File file = new File(HISTORY_FILE);
        if (file.exists() && file.delete()) {
            System.out.println("✓ Xóa lịch sử thành công");
        }
    }

    // === GỬIT FILE ===

    public static void sendFile(File file, String sender) {
        if (file == null || !file.exists()) {
            JOptionPane.showMessageDialog(null, 
                "File không tồn tại!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (file.length() > 100 * 1024 * 1024) { // 100MB limit
            JOptionPane.showMessageDialog(null,
                "File quá lớn (> 100MB)!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }

        new Thread(() -> {
            try {
                String fileName = file.getName();
                long fileSize = file.length();
                
                System.out.println("📤 Gửi file: " + fileName + " (" + fileSize + " bytes)");
                
                // Mô phỏng gửi file (trong thực tế cần TCP server)
                ChatMessage msg = new ChatMessage(
                    fileName + " (" + formatFileSize(fileSize) + ")",
                    sender,
                    ChatMessage.Type.FILE
                );
                
                System.out.println("✓ File đã sẵn sàng gửi");
                
            } catch (Exception e) {
                System.err.println("✗ Lỗi gửi file: " + e.getMessage());
            }
        }).start();
    }

    public static void saveDownloadedFile(String fileName, byte[] content) {
        try {
            Path path = Paths.get(DOWNLOAD_DIR, fileName);
            Files.write(path, content);
            System.out.println("✓ File đã lưu: " + path.toAbsolutePath());
        } catch (IOException e) {
            System.err.println("✗ Lỗi lưu file: " + e.getMessage());
        }
    }

    public static String formatFileSize(long bytes) {
        if (bytes <= 0) return "0 B";
        final String[] units = new String[]{"B", "KB", "MB", "GB"};
        int digitGroups = (int) (Math.log10(bytes) / Math.log10(1024));
        return String.format("%.1f %s", 
            bytes / Math.pow(1024, digitGroups),
            units[digitGroups]);
    }

    // === SEARCH ===

    public static List<ChatMessage> searchMessages(List<ChatMessage> history, String keyword) {
        List<ChatMessage> results = new ArrayList<>();
        String lowerKeyword = keyword.toLowerCase();
        
        for (ChatMessage msg : history) {
            if (msg.content().toLowerCase().contains(lowerKeyword) ||
                msg.sender().toLowerCase().contains(lowerKeyword)) {
                results.add(msg);
            }
        }
        
        return results;
    }

    // === EXPORT CHAT ===

    public static void exportChatToTxt(List<ChatMessage> history) {
        try {
            String fileName = "chat_export_" + System.currentTimeMillis() + ".txt";
            Path path = Paths.get(DOWNLOAD_DIR, fileName);
            
            StringBuilder content = new StringBuilder();
            content.append("=== CHAT HISTORY ===\n");
            content.append("Exported at: ").append(new Date()).append("\n\n");
            
            for (ChatMessage msg : history) {
                content.append(String.format("[%s] %s: %s\n",
                    msg.getFormattedTime(),
                    msg.sender(),
                    msg.content()));
            }
            
            Files.write(path, content.toString().getBytes());
            
            JOptionPane.showMessageDialog(null,
                "Chat đã export: " + path.toAbsolutePath(),
                "Thành công", JOptionPane.INFORMATION_MESSAGE);
                
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null,
                "Lỗi export: " + e.getMessage(),
                "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }
}