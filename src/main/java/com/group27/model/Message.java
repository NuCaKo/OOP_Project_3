package com.group27.model;

import java.sql.Timestamp;
import java.time.LocalDateTime;

/**
 * Represents a message exchanged between users.
 */
public class Message {
    private int id;
    private int senderId;
    private int receiverId;
    private String content;
    private String reply;
    private LocalDateTime timestamp;

    /**
     * Constructs a new Message.
     *
     * @param id         The message ID.
     * @param senderId   The ID of the sender.
     * @param receiverId The ID of the receiver.
     * @param content    The message content.
     * @param reply      The reply content (if any).
     * @param timestamp  The time the message was sent.
     */
    public Message(int id, int senderId, int receiverId, String content, LocalDateTime timestamp, String reply) {
        this.id = id;
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.content = content;
        this.reply = reply;
        this.timestamp = timestamp;
    }

    // Getters and Setters

    public int getId() { return id; }
    public int getSenderId() { return senderId; }
    public int getReceiverId() { return receiverId; }
    public String getContent() { return content; }
    public String getReply() { return reply; }
    public void setReply(String reply) { this.reply = reply; }
    public LocalDateTime getTimestamp() { return timestamp; }
}
