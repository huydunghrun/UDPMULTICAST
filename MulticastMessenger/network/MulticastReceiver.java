package network;

import model.ChatMessage;
import java.net.*;
import java.util.function.Consumer;

public class MulticastReceiver implements Runnable {
    private final Consumer<ChatMessage> callback;

    public MulticastReceiver(Consumer<ChatMessage> callback) {
        this.callback = callback;
    }

    @Override
    public void run() {
        try (MulticastSocket socket = new MulticastSocket(12345)) {
            InetAddress group = InetAddress.getByName("239.1.1.1");
            socket.joinGroup(group);
            
            byte[] buf = new byte[70000];
            
            while (!Thread.currentThread().isInterrupted()) {
                DatagramPacket packet = new DatagramPacket(buf, buf.length);
                socket.receive(packet);
                
                String data = new String(packet.getData(), 0, packet.getLength(), "UTF-8");
                parseAndHandle(data);
            }
        } catch (SocketException e) {
            System.out.println("Receiver dừng.");
        } catch (Exception e) {
            System.err.println("Lỗi receiver: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void parseAndHandle(String data) {
        try {
            String[] parts = data.split(":", 3);
            if (parts.length < 2) return;

            String type = parts[0];
            String sender = parts[1];
            String content = parts.length > 2 ? parts[2] : "";

            ChatMessage msg = null;

            switch (type) {
                case "TEXT":
                    msg = new ChatMessage(content, sender, ChatMessage.Type.TEXT);
                    callback.accept(msg);
                    break;
                case "TYPING":
                    msg = new ChatMessage("đang gõ...", sender, ChatMessage.Type.TYPING);
                    callback.accept(msg);
                    break;
                case "HEARTBEAT":
                    msg = new ChatMessage("", sender, ChatMessage.Type.HEARTBEAT);
                    callback.accept(msg);
                    break;
                case "JOIN":
                    msg = new ChatMessage(sender + " đã vào phòng", "SYSTEM", ChatMessage.Type.JOIN);
                    callback.accept(msg);
                    break;
                case "LEAVE":
                    msg = new ChatMessage(sender + " đã rời phòng", "SYSTEM", ChatMessage.Type.LEAVE);
                    callback.accept(msg);
                    break;
                case "SEEN":
                    msg = new ChatMessage("Đã xem bởi " + sender, sender, ChatMessage.Type.SEEN);
                    callback.accept(msg);
                    break;
                case "FILE":
                    msg = new ChatMessage("File: " + content, sender, ChatMessage.Type.FILE);
                    callback.accept(msg);
                    break;
            }
        } catch (Exception e) {
            System.err.println("Lỗi parse: " + e.getMessage());
        }
    }
}