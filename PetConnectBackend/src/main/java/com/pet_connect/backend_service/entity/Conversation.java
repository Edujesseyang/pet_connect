package com.pet_connect.backend_service.entity;

import java.util.ArrayList;
import java.util.List;

public class Conversation {
    private int conversationId;
    private User fromUser;
    private User toUser;
    private List<Message> messages;

    public Conversation() {
        this.messages = new ArrayList<>();
    }

    public Conversation(int conversationId, User fromUser, User toUser) {
        this.conversationId = conversationId;
        this.fromUser = fromUser;
        this.toUser = toUser;
    }

    public void addMessage(Message message) {
        messages.add(message);
    }

    // getters and setters
    public int getConversationId() {
        return conversationId;
    }

    public void setConversationId(int conversationId) {
        this.conversationId = conversationId;
    }

    public User getFromUser() {
        return fromUser;
    }

    public void setFromUser(User fromUser) {
        this.fromUser = fromUser;
    }

    public User getToUser() {
        return toUser;
    }

    public void setToUser(User toUser) {
        this.toUser = toUser;
    }

}
