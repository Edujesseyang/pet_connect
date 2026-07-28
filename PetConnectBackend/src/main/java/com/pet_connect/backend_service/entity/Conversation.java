package com.pet_connect.backend_service.entity;

import java.util.ArrayList;
import java.util.List;

public class Conversation {
    private int conversationId;
    private int fromUser;
    private int toUser;
    private int petId;
    private List<Message> messages;

    public Conversation(int fromUser, int toUser, int petId) {
        this.fromUser = fromUser;
        this.toUser = toUser;
        this.petId = petId;
    }

    public int getFromUser() {
        return fromUser;
    }

    public void setFromUser(int fromUser) {
        this.fromUser = fromUser;
    }

    public int getToUser() {
        return toUser;
    }

    public void setToUser(int toUser) {
        this.toUser = toUser;
    }

    public int getPetId() {
        return petId;
    }

    public void setPetId(int petId) {
        this.petId = petId;
    }

    public List<Message> getMessages() {
        return messages;
    }

    public void setMessages(List<Message> messages) {
        this.messages = messages;
    }

    public Conversation() {
        this.messages = new ArrayList<>();
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

}
