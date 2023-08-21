package com.mohaymen.web;

import com.fasterxml.jackson.annotation.JsonView;
import com.mohaymen.model.entity.MediaFile;
import com.mohaymen.model.entity.Profile;
import com.mohaymen.model.json_item.ChatListInfo;
import com.mohaymen.model.supplies.ChatType;
import com.mohaymen.model.json_item.Views;
import com.mohaymen.repository.LogRepository;
import com.mohaymen.model.supplies.security.JwtHandler;
import com.mohaymen.service.ChatService;
import com.mohaymen.service.MediaService;
import com.mohaymen.service.LogService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
public class ChatController {

    private final ChatService chatService;
    private final MediaService mediaService;
    private final LogService logger;

    public ChatController(ChatService chatService,
                          MediaService mediaService,
                          LogRepository logRepository) {
        this.chatService = chatService;
        this.mediaService = mediaService;
        this.logger = new LogService(logRepository, ChatController.class.getName());
    }

    @JsonView(Views.ChatDisplay.class)
    @GetMapping("/")
    public ResponseEntity<ChatListInfo> getChats(@RequestHeader(name = "Authorization") String token,
                                                 @RequestParam(name = "limit", defaultValue = "20") int limit,
                                                 @RequestParam(name = "active_chat", defaultValue = "0") Long activeChat) {
        Long userId;
        try {
            userId = JwtHandler.getIdFromAccessToken(token);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body(null);
        }
        try {
            ChatListInfo chatListInfo = chatService.getChats(userId, limit, activeChat);
            return ResponseEntity.ok().body(chatListInfo);
        } catch (Exception e) {
            logger.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body(null);
        }
    }


    @PostMapping("/create-chat")
    public ResponseEntity<String> createChat(@RequestHeader(name = "Authorization") String token,
                                             @RequestBody Map<String, Object> request) {
        ChatType type;
        Long userId;
        MediaFile mediaFile;
        List<Long> membersId = new ArrayList<>();
        String name = (String) request.get("name");
        String bio = (String) request.get("bio");
        try {
            type = ChatType.values()[((Number) request.get("type")).intValue()];
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Cast error!");
        }
        try {
            membersId = (List<Long>) request.get("members");
        } catch (Exception ignored) {
        }
        try {
            userId = JwtHandler.getIdFromAccessToken(token);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body("User id is not acceptable!");
        }
        Long profileId;
        try {
            profileId = chatService.createChat(userId, name, type, bio, membersId);
            try {
                mediaFile = mediaService.uploadFile(request);
                if (mediaFile != null)
                    mediaService.addProfilePicture(userId, profileId, mediaFile);
            } catch (Exception ignored) {
            }
            return ResponseEntity.ok().body("successful");
        } catch (Exception e) {
            logger.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body("fail");
        }
    }

    @PostMapping("/add-member")
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
        } catch (Exception e) {
            logger.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body("You are not allowed to add this user!");
        }
    }

    @PostMapping("/join-channel/{channelId}")
    public ResponseEntity<String> joinChannel(@PathVariable Long channelId,
                                              @RequestHeader(name = "Authorization") String token) {
        Long userId;
        try {
            userId = JwtHandler.getIdFromAccessToken(token);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body("User id is not acceptable!");
        }
        try {
            chatService.joinChannel(userId, channelId);
            return ResponseEntity.ok().body("successful");
        } catch (Exception e) {
            logger.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body("Failed");
        }
    }

    @PostMapping("/add-admin")
    public ResponseEntity<String> addAdmin(@RequestHeader(name = "Authorization") String token,
                                           @RequestBody Map<String, Object> request) {
        long userId, chatId, memberId;
        try {
            userId = JwtHandler.getIdFromAccessToken(token);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body("User id is not acceptable!");
        }
        try {
            chatId = ((Number) request.get("chatId")).longValue();
            memberId = ((Number) request.get("memberId")).longValue();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Cast error!");
        }
        try {
            chatService.addAdmin(userId, chatId, memberId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            logger.error(e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
//
//
//    @PutMapping("/pin-chat/{chatId}")
//    public ResponseEntity<String> addToPins(@RequestHeader(name = "Authorization") String token,
//                                            @PathVariable Long chatId) {
//        long userId;
//        try {
//            userId = JwtHandler.getIdFromAccessToken(token);
//        } catch (Exception e) {
//            return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body("User id is not acceptable!");
//        }
//        try {
//            chatService.pinChat(userId, chatId);
//            return ResponseEntity.ok().build();
//        } catch (Exception e) {
//            logger.error("Failed to pin chat : " + e.getMessage());
//            return ResponseEntity.badRequest().build();
//        }
//    }
//
//    @PutMapping("/unpin-chat/{chatId}")
//    public ResponseEntity<String> unpinChat(@RequestHeader(name = "Authorization") String token,
//                                            @PathVariable Long chatId) {
//        long userId;
//        try {
//            userId = JwtHandler.getIdFromAccessToken(token);
//        } catch (Exception e) {
//            return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body("User id is not acceptable!");
//        }
//        try {
//            chatService.unpinChat(userId, chatId);
//            return ResponseEntity.ok().build();
//        } catch (Exception e) {
//            return ResponseEntity.badRequest().build();
//        }
//    }

    @DeleteMapping("/delete-chat")
    public ResponseEntity<String> deleteChat(@RequestHeader(name = "Authorization") String token,
                                             @RequestBody Map<String, Object> request) {
        Long chatId = Long.parseLong((String) request.get("chatId"));
        Long userId;
        try {
            userId = JwtHandler.getIdFromAccessToken(token);
            chatService.deleteChat(userId, chatId);
        } catch (Exception e) {
            logger.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body(e.getMessage());
        }
        logger.info("this chat "+chatId+" was deleted successfully");
        return ResponseEntity.ok().body("successful");
    }

    @DeleteMapping("/leave")
    public ResponseEntity<String> leaveChat(@RequestHeader(name = "Authorization") String token,
                                            @RequestBody Map<String, Object> request) {
        long userId, chatId;
        try {
            userId = JwtHandler.getIdFromAccessToken(token);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body("User id is not acceptable!");
        }
        try {
            chatId = ((Number) request.get("chatId")).longValue();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Cast error!");
        }
        try {
            chatService.leaveChat(userId, chatId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            logger.error(e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @JsonView(Views.MemberInfo.class)
    @GetMapping("/{chatId}/members")
    public ResponseEntity<List<Profile>> getMembers(@RequestHeader(name = "Authorization") String token,
                                                    @PathVariable Long chatId) {
        long userId;
        try {
            userId = JwtHandler.getIdFromAccessToken(token);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body(null);
        }
        try {
            return ResponseEntity.ok().body(chatService.getMembers(userId, chatId));
        } catch (Exception e) {
            logger.error(e.getMessage());
            return ResponseEntity.badRequest().body(null);
        }
    }

    @GetMapping("/{chatId}/check")
    public ResponseEntity<Boolean> isMemberOfChannel(@RequestHeader(name = "Authorization") String token,
                                                     @PathVariable Long chatId) {
        long userId;
        try {
            userId = JwtHandler.getIdFromAccessToken(token);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body(null);
        }
        try {
            return ResponseEntity.ok().body(chatService.isMemberOfChannel(userId, chatId));
        } catch (Exception e) {
            logger.error(e.getMessage());
            return ResponseEntity.badRequest().body(false);
        }
    }
}
