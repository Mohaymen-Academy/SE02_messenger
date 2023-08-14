package com.mohaymen.web;

import com.fasterxml.jackson.annotation.JsonView;
import com.mohaymen.model.json_item.ChatListInfo;
import com.mohaymen.model.supplies.ChatType;
import com.mohaymen.model.json_item.Views;
import com.mohaymen.security.JwtHandler;
import com.mohaymen.service.ChatService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@CrossOrigin
@RestController
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @JsonView(Views.ChatDisplay.class)
    @GetMapping("/")
    public ResponseEntity<ChatListInfo> getChats(@RequestHeader(name = "Authorization") String token,
                                                 @RequestParam(name = "limit", defaultValue = "20") int limit) {
        Long userId;
        try {
            userId = JwtHandler.getIdFromAccessToken(token);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body(null);
        }
        return ResponseEntity.status(HttpStatus.OK).body(chatService.getChats(userId, limit));
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

    @PostMapping("/create-chat")
    public ResponseEntity<String> createChat(@RequestBody Map<String, Object> request) {
        String token, bio = null, name;
        ChatType type;
        Long userId;
        List<Long> membersId;
        try {
            token = (String) request.get("jwt");
            name = (String) request.get("name");
            type = ChatType.valueOf((String) request.get("type"));
            membersId = (List<Long>) request.get("members");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Cast error!");
        }
        try {
            userId = JwtHandler.getIdFromAccessToken(token);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body("User id is not acceptable!");
        }
        try {
            bio = (String) request.get("bio");
        } catch (Exception ignored) {}
        chatService.createChat(userId, name, type, bio, membersId);
        return ResponseEntity.status(HttpStatus.OK).body("successful");
    }

    @PostMapping("add-member")
    public ResponseEntity<String> addMember(@RequestBody Map<String, Object> request) {
        String token;
        long userId, chatId, memberId;
        try {
            token = (String) request.get("jwt");
            chatId = ((Number) request.get("chatId")).longValue();
            memberId = ((Number) request.get("memberId")).longValue();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Cast error!");
        }
        try {
            userId = JwtHandler.getIdFromAccessToken(token);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body("User id is not acceptable!");
        }
        if (chatService.addMember(userId, chatId, memberId))
            return ResponseEntity.status(HttpStatus.OK).body("successful");
        else
            return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body("You are not allowed to add this user!");
    }
}
