package com.pet_connect.backend_service.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.pet_connect.backend_service.dto.respond.InnerRespond;
import com.pet_connect.backend_service.entity.Conversation;
import com.pet_connect.backend_service.entity.Message;
import com.pet_connect.backend_service.repository.MessageDAO;

@Service
public class MessageService {
    private final MessageDAO dao;

    public MessageService(MessageDAO dao) {
        this.dao = dao;
    }

    public InnerRespond<?> startConversation(int fromUID, int toUID, int petId) {
        Conversation con = new Conversation(fromUID, toUID, petId);
        con.setConversationId(-1);
        int conversationId = dao.createConversation(con);
        if (conversationId != -1) {
            con.setConversationId(conversationId);
            return new InnerRespond<>(true, "conversation created successful", con);
        }
        return new InnerRespond<>(false, "fail to create conversation");
    }

    public InnerRespond<?> getConversation(int conversationId) {
        Conversation con = dao.getConversation(conversationId);
        if (con == null) {
            return new InnerRespond<>(false, "failed");
        }
        return new InnerRespond<>(true, "success", con);
    }

    public InnerRespond<?> sendMessage(Message msg) {
        Conversation con = dao.sendMessage(msg);
        if (con == null) {
            return new InnerRespond<>(false, "failed");
        }
        return new InnerRespond<>(true, "success", con);
    }

    public InnerRespond<?> getAllConversation(int userId) {
        List<Conversation> cons = dao.getAllConversation(userId);
        if (cons == null) {
            return new InnerRespond<>(false, "failed");
        }
        return new InnerRespond<>(true, "success", cons);
    }

    public InnerRespond<?> endConversation(int conversationId) {
        if (!dao.endConversation(conversationId)) {
            return new InnerRespond<>(false, "failed");
        }
        return new InnerRespond<>(true, "success");
    }

}
