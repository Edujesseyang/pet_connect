package com.pet_connect.backend_service.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.pet_connect.backend_service.dto.respond.InnerRespond;
import com.pet_connect.backend_service.entity.Message;
import com.pet_connect.backend_service.service.MessageService;



@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/message")
public class MessageController {
    private final MessageService service;

    public MessageController(MessageService service) {
        this.service = service;
    }

    @PostMapping("/begin_conversation/{from_uid}/{to_uid}/{pet_id}")
    public ResponseEntity<?> begingConversation(@RequestParam int from_uid, @RequestParam int to_uid,
            @RequestParam int pet_id) {
        InnerRespond<?> result = service.startConversation(from_uid, to_uid, pet_id);

        if (result.getState()) {
            return ResponseEntity.ok(result);
        }
        return ResponseEntity.badRequest().body(result);
    }

    @PostMapping("/get_conversation/{conversation_id}")
    public ResponseEntity<?> getConversation(@RequestParam int conversation_id) {
        InnerRespond<?> result = service.getConversation(conversation_id);

        if (result.getState()) {
            return ResponseEntity.ok(result);
        }
        return ResponseEntity.badRequest().body(result);
    }

    @PostMapping("/send_message")
    public ResponseEntity<?> sendMessage(@RequestBody Message msg) {
        InnerRespond<?> result = service.sendMessage(msg);
        if (result.getState()) {
            return ResponseEntity.ok(result);
        }
        return ResponseEntity.badRequest().body(result);
    }

    @GetMapping("/get_all/{userId}")
    public ResponseEntity<?> getAllConversation(@RequestParam int userId) {
        InnerRespond<?> result = service.getAllConversation(userId);
        if (result.getState()) {
            return ResponseEntity.ok(result);
        }
        return ResponseEntity.badRequest().body(result);
    }

        @PostMapping("/end_conversation/{conversationId}")
    public ResponseEntity<?> endConversation(@RequestParam int conversationId) {
        InnerRespond<?> result = service.endConversation(conversationId);
        if (result.getState()) {
            return ResponseEntity.ok(result);
        }
        return ResponseEntity.badRequest().body(result);
    }
    
}
