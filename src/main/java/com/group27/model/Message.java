package com.group27.model;

import java.time.LocalDateTime;

/**
 * Represents a message in the GreenGrocer system.
 * Used for communication between users and the owner.
 * Contains message content, sender/receiver IDs, timestamp, and optional reply.
 * 
 * @author Group27
 * @version 1.0
 */
public class Message {
    private int id;
    private int senderId;
    private int receiverId; // 0 for Owner (if owner is a single entity or specific user)
    private String content;
    private LocalDateTime timestamp;
    private String reply;

    /**
     * Constructs a Message with all parameters.
     * 
     * @param id The unique identifier for the message
     * @param senderId The ID of the user who sent the message
     * @param receiverId The ID of the receiver (0 for Owner)
     * @param content The message content
     * @param timestamp The timestamp when the message was sent
     * @param reply The reply to the message (null if no reply yet)
     */
    public Message(int id, int senderId, int receiverId, String content, LocalDateTime timestamp, String reply) {
        this.id = id;
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.content = content;
        this.timestamp = timestamp;
        this.reply = reply;
    }

    /**
     * Gets the message's unique identifier.
     * 
     * @return The message ID
     */
    public int getId() { return id; }
    
    /**
     * Gets the ID of the sender.
     * 
     * @return The sender ID
     */
    public int getSenderId() { return senderId; }
    
    /**
     * Gets the ID of the receiver.
     * 
     * @return The receiver ID (0 for Owner)
     */
    public int getReceiverId() { return receiverId; }
    
    /**
     * Gets the message content.
     * 
     * @return The message content
     */
    public String getContent() { return content; }
    
    /**
     * Gets the timestamp when the message was sent.
     * 
     * @return The timestamp
     */
    public LocalDateTime getTimestamp() { return timestamp; }
    
    /**
     * Gets the reply to the message.
     * 
     * @return The reply, or null if no reply exists
     */
    public String getReply() { return reply; }
}
