package utils;

import java.awt.Color;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class UIHelper {
    private static final String[] COLORS = {
        "#1877f2", "#42b72a", "#e91e63", "#9c27b0",
        "#ff9800", "#00bcd4", "#f44336", "#607d8b"
    };
    
    private static String currentUser = "Unknown";

    public static void setCurrentUser(String user) {
        currentUser = user;
    }

    public static String getCurrentUser() {
        return currentUser;
    }

    public static Color getColorForUser(String name) {
        if (name == null || name.isEmpty()) return Color.GRAY;
        return Color.decode(COLORS[Math.abs(name.hashCode() % COLORS.length)]);
    }

    public static String getInitial(String name) {
        if (name == null || name.isEmpty()) return "?";
        String[] parts = name.trim().split("\\s+");
        
        if (parts.length > 1) {
            return (parts[0].charAt(0) + "" + parts[parts.length - 1].charAt(0))
                    .toUpperCase();
        }
        return name.substring(0, Math.min(2, name.length())).toUpperCase();
    }

    public static String replaceEmoji(String text) {
        return text.replace(":))", "😄")
                   .replace(":)", "😊")
                   .replace(":D", "😁")
                   .replace(":(", "😢")
                   .replace("<3", "❤️")
                   .replace(":*", "😘")
                   .replace(":P", "😜")
                   .replace(":O", "😮");
    }

    public static String getTime() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm"));
    }

    public static String formatTime(LocalDateTime dateTime) {
        return dateTime.format(DateTimeFormatter.ofPattern("HH:mm:ss"));
    }
}