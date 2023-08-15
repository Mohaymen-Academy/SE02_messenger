package com.mohaymen.web;

import com.fasterxml.jackson.annotation.JsonView;
import com.mohaymen.model.entity.MediaFile;
import com.mohaymen.model.json_item.ChatListInfo;
import com.mohaymen.model.supplies.ChatType;
import com.mohaymen.model.json_item.Views;
import com.mohaymen.security.JwtHandler;
import com.mohaymen.service.ChatService;
import com.mohaymen.service.ProfileService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
import java.util.stream.Stream;

@CrossOrigin
@RestController
public class ChatController {

    private final ChatService chatService;
    private final ProfileService profileService;

    public ChatController(ChatService chatService, ProfileService profileService) {
        this.chatService = chatService;
        this.profileService = profileService;
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
        try {
            ChatListInfo chatListInfo = chatService.getChats(userId, limit);
            return ResponseEntity.ok().body(chatListInfo);
        }
        catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body(null);
        }
    }

    @DeleteMapping("/delete-chat")
    public ResponseEntity<String> deleteChannelOrGroup(@RequestHeader(name = "Authorization") String token,
                                                       @RequestBody Map<String, Object> request){
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
    public ResponseEntity<String> createChat(@RequestHeader(name = "Authorization") String token,
                                             @RequestPart(value = "data") MultipartFile file,
                                             @RequestParam(value = "name") String name,
                                             @RequestParam(value = "type") String typeInput,
                                             @RequestParam(value = "members") String members,
                                             @RequestParam(value = "bio") String bio) {
        ChatType type;
        Long userId;
        MediaFile mediaFile;
        List<Long> membersId;
        try {
            type = ChatType.values()[Integer.parseInt(typeInput)];
            if (members == null || members.isEmpty()) membersId = new ArrayList<>();
            else
                membersId = members;
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Cast error!");
        }
        try {
            userId = JwtHandler.getIdFromAccessToken(token);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body("User id is not acceptable!");
        }
        Long profileId;
        try {
            chatService.createChat(userId, name, type, bio, membersId);
            try {
                mediaFile = profileService.uploadFile
                        (file.getSize(),
                         file.getContentType(),
                         file.getOriginalFilename(),
                         file.getBytes());
                profileService.addCompressedImage(mediaFile);
                profileService.addProfilePicture(userId, profileId, mediaFile);
            } catch (Exception ignored){}
            return ResponseEntity.ok().body("successful");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body("fail");
        }
    }

    @PostMapping("add-member")
    public ResponseEntity<String> addMember(@RequestHeader(name = "Authorization") String token,
                                            @RequestBody Map<String, Object> request) {
        long userId, chatId, memberId;
        try {
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
        try {
            chatService.addMember(userId, chatId, memberId);
            return ResponseEntity.ok().body("successful");
        }
        catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body("You are not allowed to add this user!");
        }
    }
}
