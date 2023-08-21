package com.mohaymen.web;

import com.fasterxml.jackson.annotation.JsonView;
import com.mohaymen.model.entity.MediaFile;
import com.mohaymen.model.entity.Message;
import com.mohaymen.model.json_item.MediaDisplay;
import com.mohaymen.model.json_item.MessageDisplay;
import com.mohaymen.model.json_item.Views;
import com.mohaymen.repository.LogRepository;
import com.mohaymen.model.supplies.security.JwtHandler;
import com.mohaymen.service.LogService;
import com.mohaymen.service.MediaService;
import com.mohaymen.service.MessageService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

@RestController
public class MessageController {

    private final MessageService messageService;
    private final MediaService mediaService;
    private final LogService logger;

    public MessageController(MessageService messageService,
                             MediaService mediaService,
                             LogRepository logRepository) {
        this.messageService = messageService;
        this.mediaService = mediaService;
        this.logger = new LogService(logRepository, MessageController.class.getName());
    }

    @PostMapping("/{receiver}")
    public ResponseEntity<String> SendMessage(@PathVariable Long receiver,
                                              @RequestHeader(name = "Authorization") String token,
                                              @RequestBody Map<String, Object> request) {
        long sender;
        Long replyMessage = null, forwardMessage = null;
        String text, textStyle;
        try {
            sender = JwtHandler.getIdFromAccessToken(token);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body(null);
        }
        text = (String) request.get("text");
        textStyle = (String) request.get("text_style");
        if (textStyle == null)
            textStyle = "";
        try {
            forwardMessage = ((Number) request.get("forward_message")).longValue();
            messageService.forwardMessage(sender, receiver, forwardMessage);
            return ResponseEntity.ok().body("messages is sent.");
        } catch (Exception ignored) {
        }
        try {
            replyMessage = ((Number) request.get("reply_message")).longValue();
        } catch (Exception ignored) {
        }
        try {
            MediaFile mediaFile = mediaService.uploadFile(request);
            messageService.sendMessage(sender, receiver, text, textStyle, replyMessage, forwardMessage, mediaFile);
            return ResponseEntity.ok().body("messages is sent.");
        } catch (Exception e) {
            logger.error(e.getMessage());
            System.out.println(e.getMessage());
            return ResponseEntity.badRequest().body("cannot send message");
        }
    }

    @JsonView(Views.GetMessage.class)
    @GetMapping("/{chatId}")
    public ResponseEntity<MessageDisplay> getMessages(@PathVariable Long chatId,
                                                      @RequestHeader(name = "Authorization") String token,
                                                      @RequestParam(name = "message_id", defaultValue = "0") Long messageID,
                                                      @RequestParam(name = "direction", defaultValue = "0") int direction) {
        Long userID;
        try {
            userID = JwtHandler.getIdFromAccessToken(token);
        } catch (Exception e) {
            logger.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body(null);
        }
        try {
            return ResponseEntity.status(HttpStatus.OK).body(messageService.getMessages(chatId, userID, messageID, direction));
        } catch (Exception e) {
            logger.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }

    @PostMapping("/edit-message/{messageId}")
    public ResponseEntity<String> editMessage(@PathVariable Long messageId,
                                              @RequestHeader(name = "Authorization") String token,
                                              @RequestBody Map<String, Object> request) {
        Long userID;
        String newMessage, textStyle;
        newMessage = (String) request.get("text");
        textStyle = (String) request.get("text_style");
        try {
            userID = JwtHandler.getIdFromAccessToken(token);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body(null);
        }
        try {
            messageService.editMessage(userID, messageId, newMessage, textStyle);
            return ResponseEntity.ok().body("message is edited.");
        } catch (Exception e) {
            logger.error(e.getMessage());
            return ResponseEntity.badRequest().body("cannot edit message.");
        }
    }

    @DeleteMapping("/{messageId}")
    public ResponseEntity<String> deleteMessage(@PathVariable Long messageId,
                                                @RequestHeader(name = "Authorization") String token) {
        Long userID;
        try {
            userID = JwtHandler.getIdFromAccessToken(token);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body(null);
        }
        try {
            messageService.deleteMessage(userID, messageId);
            return ResponseEntity.ok().body("message is deleted.");
        } catch (Exception e) {
            logger.error(e.getMessage());
            return ResponseEntity.badRequest().body("cannot delete this message!");
        }
    }

//    @PutMapping("/pinMessage")
//    public ResponseEntity<String> pinMessage(@RequestBody Map<String, Object> messageReq,
//                                             @RequestHeader(name = "Authorization") String token) {
//        Long userId;
//        try {
//            userId = JwtHandler.getIdFromAccessToken(token);
//        } catch (Exception e) {
//            throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE);
//        }
//
//        Long messageId = ((Number) messageReq.get("messageId")).longValue();
//        try {
//            messageService.setPinMessage(userId, messageId, true);
//            return ResponseEntity.ok().body("Message is pinned");
//        } catch (Exception e) {
//            return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body(e.getMessage());
//        }
//    }
//
//    @PutMapping("/unpinMessage")
//    public ResponseEntity<String> unpinMessage(@RequestBody Map<String, Object> messageReq,
//                                               @RequestHeader(name = "Authorization") String token) {
//        Long userID;
//        try {
//            userID = JwtHandler.getIdFromAccessToken(token);
//        } catch (Exception e) {
//            throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE);
//        }
//
//        Long messageId = ((Number) messageReq.get("messageId")).longValue();
//        try {
//            messageService.setPinMessage(userID, messageId, false);
//            return ResponseEntity.ok().body("Message is unpinned");
//        } catch (Exception e) {
//            return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body(e.getMessage());
//        }
//    }

    @JsonView(Views.GetMessage.class)
    @GetMapping("/update/{messageId}")
    public ResponseEntity<Message> getSingleMessage(@PathVariable Long messageId,
                                                    @RequestHeader(name = "Authorization") String token) {
        try {
            JwtHandler.getIdFromAccessToken(token);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE);
        }
        try {
            return ResponseEntity.ok().body(messageService.getSingleMessage(messageId));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }

    @PutMapping("/update")
    public ResponseEntity<String> setLastUpdate(@RequestHeader(name = "Authorization") String token,
                                                @RequestBody Map<String, Object> request) {
        long userId, chatId, updateId;
        try {
            userId = JwtHandler.getIdFromAccessToken(token);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE);
        }
        try {
            chatId = ((Number) request.get("chat_id")).longValue();
            updateId = ((Number) request.get("update_id")).longValue();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("cast error");
        }
        try {
            messageService.setLastUpdate(chatId, userId, updateId);
            return ResponseEntity.ok().body("successful");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("fail");
        }
    }

//    @JsonView(Views.GetMessage.class)
//    @GetMapping("/getPinnedMessages/{chatId}")
//    public ResponseEntity<?> getPinnedMessages(@PathVariable Long chatId,
//                                               @RequestHeader(name = "Authorization") String token) {
//        Long userId;
//        try {
//            userId = JwtHandler.getIdFromAccessToken(token);
//        } catch (Exception e) {
//            throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE);
//        }
//        try {
//            return ResponseEntity.ok().body(messageService.getPinMessage(userId, chatId));
//        } catch (Exception e) {
//            logger.error(e.getMessage());
//            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("fail");
//        }
//    }

    @JsonView(Views.GetMedia.class)
    @GetMapping("/media/{chatId}")
    public ResponseEntity<MediaDisplay> getMedia(@PathVariable Long chatId,
                                                 @RequestHeader(name = "Authorization") String token) {
        Long userId;
        try {
            userId = JwtHandler.getIdFromAccessToken(token);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE);
        }
        return ResponseEntity.ok().body(messageService.getMediaOfChat(userId, chatId));
    }
}
