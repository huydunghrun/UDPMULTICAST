package utils; // <--- BẠN ĐANG BỊ THIẾU DÒNG NÀY

import java.awt.Color;

public class ThemeManager {
    // Trạng thái hiện tại: mặc định là Tối (true)
    private static boolean isDarkMode = true;

    // --- BẢNG MÀU CHẾ ĐỘ TỐI (DARK MODE) ---
    public static final Color DARK_MAIN_BG = new Color(31, 31, 31);
    public static final Color DARK_TEXT = new Color(220, 220, 220);
    public static final Color DARK_BUBBLE_OTHER = new Color(55, 55, 55);
    public static final Color DARK_INPUT_BG = new Color(45, 45, 45);
    public static final Color DARK_BORDER = new Color(80, 80, 80);

    // --- BẢNG MÀU CHẾ ĐỘ SÁNG (LIGHT MODE) ---
    public static final Color LIGHT_MAIN_BG = Color.WHITE;
    public static final Color LIGHT_TEXT = Color.BLACK;
    public static final Color LIGHT_BUBBLE_OTHER = new Color(240, 240, 240);
    public static final Color LIGHT_INPUT_BG = Color.WHITE;
    public static final Color LIGHT_BORDER = new Color(200, 200, 200);

    // Màu chung
    public static final Color MY_BUBBLE_BG = new Color(0, 150, 136);
    public static final Color MY_TEXT = Color.WHITE;

    // --- HÀM CHUYỂN ĐỔI VÀ LẤY MÀU ---
    public static void toggle() {
        isDarkMode = !isDarkMode;
    }
    
    public static boolean isDark() {
        return isDarkMode;
    }

    public static Color getBackgroundColor() {
        return isDarkMode ? DARK_MAIN_BG : LIGHT_MAIN_BG;
    }

    public static Color getTextColor() {
        return isDarkMode ? DARK_TEXT : LIGHT_TEXT;
    }

    public static Color getOtherBubbleColor() {
        return isDarkMode ? DARK_BUBBLE_OTHER : LIGHT_BUBBLE_OTHER;
    }
    
    public static Color getInputBgColor() {
        return isDarkMode ? DARK_INPUT_BG : LIGHT_INPUT_BG;
    }

    public static Color getBorderColor() {
        return isDarkMode ? DARK_BORDER : LIGHT_BORDER;
    }

    public static Color getSendButtonTextColor() {
        // Nếu là Dark Mode -> Trắng, nếu Light Mode -> Đen
        return isDarkMode ? Color.WHITE : Color.BLACK;
    }
}