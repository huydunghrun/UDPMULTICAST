package network;

import java.net.*;
import java.util.concurrent.*;

public class MulticastSender {
    private static final String GROUP = "239.1.1.1";
    private static final int PORT = 12345;
    private final String nickname;
    private ScheduledExecutorService heartbeatExecutor;
    private ScheduledFuture<?> heartbeatTask;

    public MulticastSender(String nickname) {
        this.nickname = nickname;
    }

    public void sendText(String text) {
        send("TEXT:" + nickname + ":" + text);
    }

    public void sendTyping() {
        send("TYPING:" + nickname);
    }

    public void sendSeen(String targetNickname) {
        send("SEEN:" + nickname + ":" + targetNickname);
    }

    public void sendJoin() {
        send("JOIN:" + nickname);
        System.out.println("📢 Sent JOIN: " + nickname);
    }

    public void sendLeave() {
        send("LEAVE:" + nickname);
        System.out.println("📢 Sent LEAVE: " + nickname);
    }

    public void sendFile(String fileName, String filePath) {
        send("FILE:" + nickname + ":" + fileName + ":" + filePath);
    }

    public void startHeartbeat() {
        heartbeatExecutor = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "Heartbeat-Thread");
            t.setDaemon(true);
            return t;
        });

        // Gửi heartbeat ngay lập tức, sau đó lặp lại mỗi 2 giây
        heartbeatTask = heartbeatExecutor.scheduleAtFixedRate(
                () -> send("HEARTBEAT:" + nickname),
                0,
                2,
                TimeUnit.SECONDS
        );
    }

    public void stopHeartbeat() {
        if (heartbeatTask != null) {
            heartbeatTask.cancel(true);
        }
        if (heartbeatExecutor != null) {
            heartbeatExecutor.shutdownNow();
        }
    }

    private void send(String data) {
        try (DatagramSocket socket = new DatagramSocket()) {
            byte[] buf = data.getBytes("UTF-8");
            InetAddress group = InetAddress.getByName(GROUP);
            socket.send(new DatagramPacket(buf, buf.length, group, PORT));
        } catch (Exception e) {
            System.err.println("Lỗi gửi: " + e.getMessage());
        }
    }
}