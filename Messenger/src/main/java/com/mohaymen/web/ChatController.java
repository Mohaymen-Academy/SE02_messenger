package com.mohaymen.web;

import com.mohaymen.model.ChatParticipant;
import com.mohaymen.model.ProfileDisplay;
import com.mohaymen.security.JwtHandler;
import com.mohaymen.service.ChatService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
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

    @GetMapping("/")
    public List<ProfileDisplay> getChats(@RequestBody Map<String, Object> request) {
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

    @PutMapping("/add/ToPin")
    public ResponseEntity<String> updateToPinChats(@RequestBody Map<String, Object> request) {
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
        Long toPinId = Long.valueOf((Integer) request.get("toPinId"));
        try {
            chatService.addToPins(userId, toPinId);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("{\"error\": \"" + e.getMessage() + "\"}");
        }
        return ResponseEntity.status(200).body("{profile "+toPinId+"added to pin chat+}");
    }


    @GetMapping("/get/PinnedChats")
    public List<ChatParticipant> getPinnedChats(@RequestBody Map<String, Object> request) {
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
        return chatService.getPinnedChats(userId);
    }

}
