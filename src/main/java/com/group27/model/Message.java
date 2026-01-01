package com.group27.model;

import java.time.LocalDateTime;

public class Message {
    private int id;
    private int senderId;
    private int receiverId; // 0 for Owner (if owner is a single entity or specific user)
    private String content;
    private LocalDateTime timestamp;
    private String reply;

    public Message(int id, int senderId, int receiverId, String content, LocalDateTime timestamp, String reply) {
        this.id = id;
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.content = content;
        this.timestamp = timestamp;
        this.reply = reply;
    }

    public int getId() { return id; }
    public int getSenderId() { return senderId; }
    public int getReceiverId() { return receiverId; }
    public String getContent() { return content; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public String getReply() { return reply; }
}
