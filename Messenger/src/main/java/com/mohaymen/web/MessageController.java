package com.mohaymen.web;

import com.mohaymen.service.MessageService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.util.Map;

@RestController
public class MessageController {

    private final MessageService messageService;

    public MessageController(MessageService messageService) {
        this.messageService = messageService;
    }

    @PostMapping("/chat/send-message/{receiver}")
    public String SendMessage(@PathVariable Long receiver,
             @RequestBody Map<String, Object> request) {
        long sender;
        String text, token;
        try {
            token = (String) request.get("token");
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
        try {
            sender = Long.parseLong((String) request.get("sender"));
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
        try {
            text = (String) request.get("text");
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        if (messageService.sendMessage(sender, receiver, text)) return "Message is sent.";
        else return "Cannot send message!";
    }

    @GetMapping("/chat/get-messages/{chatID}")
    public void getMessages(@PathVariable Long chatID,
                             @RequestBody Map<String, Object> request) {
        Long userID;
        String token;
        int direction;
        try {
            token = (String) request.get("token");
        } catch (Exception ignored) {}
        try {
            userID = (Long) request.get("user_id");
        } catch (Exception ignored) {}
    }
}
