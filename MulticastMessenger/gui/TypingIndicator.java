package gui;

import java.util.*;
import java.util.concurrent.*;

public class TypingIndicator {
    private final Map<String, Long> typingUsers = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduler;
    private final Runnable onUpdate;
    private static final long TYPING_TIMEOUT_MS = 2000L;

    public TypingIndicator(Runnable onUpdate) {
        this.onUpdate = onUpdate;
        this.scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "Typing-Checker");
            t.setDaemon(true);
            return t;
        });
        
        startTimeoutChecker();
    }

    public void setTyping(String nickname) {
        typingUsers.put(nickname, System.currentTimeMillis());
        onUpdate.run();
    }

    public void stopTyping(String nickname) {
        typingUsers.remove(nickname);
        onUpdate.run();
    }

    public String getTypingStatus() {
        if (typingUsers.isEmpty()) return "";
        
        List<String> users = new ArrayList<>(typingUsers.keySet());
        if (users.size() == 1) {
            return users.get(0) + " đang gõ...";
        } else if (users.size() <= 3) {
            return String.join(", ", users) + " đang gõ...";
        } else {
            return users.size() + " người đang gõ...";
        }
    }

    private void startTimeoutChecker() {
        scheduler.scheduleAtFixedRate(() -> {
            long now = System.currentTimeMillis();
            boolean changed = false;
            
            for (Iterator<Map.Entry<String, Long>> it = typingUsers.entrySet().iterator(); it.hasNext();) {
                Map.Entry<String, Long> entry = it.next();
                if (now - entry.getValue() > TYPING_TIMEOUT_MS) {
                    it.remove();
                    changed = true;
                }
            }
            
            if (changed) {
                onUpdate.run();
            }
        }, 500, 500, TimeUnit.MILLISECONDS);
    }

    public void shutdown() {
        scheduler.shutdownNow();
    }
}