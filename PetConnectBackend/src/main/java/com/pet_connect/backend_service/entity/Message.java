package com.pet_connect.backend_service.entity;

public class Message {
    private int messageId;
    private User sender;
    private String content;
    private long timestamp;

    public Message() {
    }

    public Message(int messageId,  User sender, String content, long timestamp) {
        this.messageId = messageId;
        this.sender = sender;
        this.content = content;
        this.timestamp = timestamp;
    }

    // getters and setters
    public int getMessageId() {
        return messageId;
    }

    public void setMessageId(int messageId) {
        this.messageId = messageId;
    }

    public User getSender() {
        return sender;
    }

    public void setSender(User sender) {
        this.sender = sender;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
    
}
