package com.mohaymen.web;

import com.fasterxml.jackson.annotation.JsonView;
import com.mohaymen.model.entity.MediaFile;
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
        long sender, mediaId;
        Long replyMessage = null, forwardMessage = null;
        String text, textStyle;
        MediaFile mediaFile = null;
        try {
            sender = JwtHandler.getIdFromAccessToken(token);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body(null);
        }
        text = (String) request.get("text");
        textStyle = (String) request.get("text_style");
        if(textStyle == null)
            textStyle = "";
        try {
            replyMessage = ((Number) request.get("reply_message")).longValue();
        } catch (Exception ignored) {}
        try {
            forwardMessage = ((Number) request.get("forward_message")).longValue();
        } catch (Exception ignored) {}
        try {
            mediaId = ((Number) request.get("media_id")).longValue();
            mediaFile = mediaService.getMedia(mediaId);
        } catch (Exception ignored) {}
        try {
            if (mediaFile == null)
                mediaFile = mediaService.uploadFile(request);
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
        }
        catch (Exception e) {
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

    @PutMapping("/pinMessage/{messageId}")
    public ResponseEntity<String> pinMessage(@PathVariable Long messageId,
                                             @RequestHeader(name = "Authorization") String token) {
        Long userID;
        try {
            userID = JwtHandler.getIdFromAccessToken(token);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE);
        }
        try {
            messageService.pinMessage(userID, messageId);
            return ResponseEntity.ok().body("Message is pinned");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body(e.getMessage());
        }
    }

    @PutMapping("/unpinMessage/{messageId}")
    public ResponseEntity<String> unpinMessage(@PathVariable Long messageId,
                                               @RequestHeader(name = "Authorization") String token) {
        Long userID;
        try {
            userID = JwtHandler.getIdFromAccessToken(token);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE);
        }
        try {
            messageService.unpinMessage(userID, messageId);
            return ResponseEntity.ok().body("Message is unpinned");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body(e.getMessage());
        }
    }

//    @GetMapping("/getPinnedMessages/{chatId}")
//    public ResponseEntity<MessageDisplay> getPinnedMessages(@PathVariable Long chatId,
//                                                            @RequestHeader(name = "Authorization") String token) {
//
//    }

}
