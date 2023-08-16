package com.mohaymen.web;

import com.fasterxml.jackson.annotation.JsonView;
import com.mohaymen.model.entity.MediaFile;
import com.mohaymen.model.json_item.MessageDisplay;
import com.mohaymen.model.json_item.Views;
import com.mohaymen.security.JwtHandler;
import com.mohaymen.service.MessageService;
import com.mohaymen.service.ProfileService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.util.Map;

@CrossOrigin
@RestController
public class MessageController {

    private final MessageService messageService;
    private final ProfileService profileService;

    public MessageController(MessageService messageService, ProfileService profileService) {
        this.messageService = messageService;
        this.profileService = profileService;
    }

    @PostMapping("/{receiver}")
    public String SendMessage(@PathVariable Long receiver,
                              @RequestHeader(name = "Authorization") String token,
                              @RequestBody Map<String, Object> request) {
        long sender;
        Long replyMessage = null;
        String text;
        try {
            sender = JwtHandler.getIdFromAccessToken(token);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE);
        }
        text = (String) request.get("text");
        try {
            replyMessage = ((Number) request.get("reply_message")).longValue();
        } catch (Exception ignored) {}
        try {
            MediaFile mediaFile = profileService.uploadFile(request);
            if (messageService.sendMessage(sender, receiver, text, replyMessage, mediaFile)) {
                return "Message is sent.";
            }
            else return "Cannot send message!";
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @JsonView(Views.GetMessage.class)
    @GetMapping("/{chatId}")
    public ResponseEntity<MessageDisplay> getMessages(@PathVariable Long chatId,
                                                      @RequestHeader(name = "Authorization") String token,
                                                      @RequestParam(name = "message_id", defaultValue = "0") Long messageID) {
        Long userID;
        try {
            userID = JwtHandler.getIdFromAccessToken(token);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body(null);
        }
        try {
            return ResponseEntity.status(HttpStatus.OK).body(messageService.getMessages(chatId, userID, messageID));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }

    @PostMapping("/edit-message/{messageId}")
    public String editMessage(@PathVariable Long messageId,
                              @RequestHeader(name = "Authorization") String token,
                              @RequestBody Map<String, Object> request) {
        Long userID;
        String newMessage;
        try {
            newMessage = (String) request.get("text");
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
        try {
            userID = JwtHandler.getIdFromAccessToken(token);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE);
        }
        if (messageService.editMessage(userID, messageId, newMessage))
            return "Message is edited";
        else
            return "Cannot edit message!";
    }

    @DeleteMapping("/{messageId}")
    public String deleteMessage(@PathVariable Long messageId,
                                @RequestHeader(name = "Authorization") String token) {
        Long userID;
        try {
            userID = JwtHandler.getIdFromAccessToken(token);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE);
        }
        if (messageService.deleteMessage(userID, messageId))
            return "Message is deleted.";
        else
            return "Cannot delete message!";
    }

}
