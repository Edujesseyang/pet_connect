package com.pet_connect.backend_service.entity;

import java.time.LocalDateTime;

public class Message {
    private int senderId;
    private String content;
    private LocalDateTime timestemp;
    private String senderName;
    private int conversationId;

    public Message() {
    }

    public Message(int senderId, String content, LocalDateTime timestamp) {
        this.senderId = senderId;
        this.content = content;
        this.timestemp = timestamp;
    }

    // getters and setters

    public int getSenderId() {
        return senderId;
    }

    public void setSenderId(int senderId) {
        this.senderId = senderId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public LocalDateTime getTimestamp() {
        return timestemp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestemp = timestamp;
    }

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public int getConversationId() {
        return conversationId;
    }

    public void setConversationId(int conversationId) {
        this.conversationId = conversationId;
    }
    
}
