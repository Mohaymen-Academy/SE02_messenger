package com.mohaymen.web;

import com.fasterxml.jackson.annotation.JsonView;
import com.mohaymen.model.ChatDisplay;
import com.mohaymen.model.Views;
import com.mohaymen.security.JwtHandler;
import com.mohaymen.service.ChatService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
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
    public List<ChatDisplay> getChats(@RequestHeader(name = "Authorization") String token) {
        Long userId;
        try {
            userId = JwtHandler.getIdFromAccessToken(token);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE);
        }
        return chatService.getChats(userId);
    }

    @DeleteMapping("/delete-chat")
    public ResponseEntity<String> deleteChannelOrGroup(@RequestBody Map<String, Object> request){
        String token = (String) request.get("jwt");
        Long channelOrGroupId = Long.parseLong((String) request.get("chat"));
        Long id;
        try {
            id = JwtHandler.getIdFromAccessToken(token);
            chatService.deleteChannelOrGroupByAdmin(id, channelOrGroupId);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body("boz");
        }
        return ResponseEntity.ok().body("successful");
    }
}
