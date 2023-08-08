package com.mohaymen.web;

import com.mohaymen.service.ChatService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.util.Map;

@RestController
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @PostMapping("/chat/send-message")
    public String SendMessage(@RequestBody Map<String, Object> request) {
        Long sender, receiver;
        String text, token;
        try {
            token = (String) request.get("token");
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
        try {
            sender = (Long) request.get("sender");
            receiver = (Long) request.get("receiver");
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
        try {
            text = (String) request.get("text");
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        if (chatService.sendMessage(sender, receiver, text)) return "Message is sent.";
        else return "Cannot send message!";
    }

    @GetMapping("/chat/get-messages")
    public void getMessages(@RequestBody Map<String, Object> request) {
        Long chatID, userID;
        String token;
        int direction;
        try {
            token = (String) request.get("token");
        } catch (Exception ignored) {}
        try {
            chatID = (Long) request.get("chat_id");
            userID = (Long) request.get("user_id");
        } catch (Exception ignored) {}

    }
}
