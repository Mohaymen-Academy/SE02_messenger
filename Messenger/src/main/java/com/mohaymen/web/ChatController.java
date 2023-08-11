package com.mohaymen.web;

import com.fasterxml.jackson.annotation.JsonView;
import com.mohaymen.model.ChatDisplay;
import com.mohaymen.model.Views;
import com.mohaymen.security.JwtHandler;
import com.mohaymen.service.ChatService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;
import java.util.Map;

@RestController
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @JsonView(Views.ChatDisplay.class)
    @GetMapping("/")
    public List<ChatDisplay> getChats(@RequestBody Map<String, Object> request) {
        String token;
        Long userId;
        try {
            token = (String) request.get("jwt");
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
        try {
            userId = JwtHandler.getIdFromAccessToken(token);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE);
        }
        return chatService.getChats(userId);
    }

}
