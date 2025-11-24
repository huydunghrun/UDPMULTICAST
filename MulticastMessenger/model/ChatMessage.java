package model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ChatMessage implements Serializable, Comparable<ChatMessage> {
    private static final long serialVersionUID = 1L;
    
    private final String content;
    private final String sender;
    private final Type type;
    private final LocalDateTime timestamp;
    private boolean seen;

    public enum Type {
        TEXT, SYSTEM, TYPING, JOIN, LEAVE, IMAGE, SEEN, HEARTBEAT, FILE
    }

    public ChatMessage(String content, String sender, Type type) {
        this.content = content;
        this.sender = sender;
        this.type = type;
        this.timestamp = LocalDateTime.now();
        this.seen = false;
    }

    public ChatMessage(String content, String sender, Type type, LocalDateTime timestamp) {
        this.content = content;
        this.sender = sender;
        this.type = type;
        this.timestamp = timestamp;
        this.seen = false;
    }

    public String content() {
        return content;
    }

    public String sender() {
        return sender;
    }

    public Type type() {
        return type;
    }

    public LocalDateTime timestamp() {
        return timestamp;
    }

    public boolean isSeen() {
        return seen;
    }

    public void setSeen(boolean seen) {
        this.seen = seen;
    }

    public String getFormattedTime() {
        return timestamp.format(DateTimeFormatter.ofPattern("HH:mm"));
    }

    @Override
    public String toString() {
        return String.format("[%s] %s: %s", type, sender, content);
    }

    @Override
    public int compareTo(ChatMessage o) {
        return this.timestamp.compareTo(o.timestamp);
    }
}