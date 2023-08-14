package com.mohaymen.web;

import com.fasterxml.jackson.annotation.JsonView;
import com.mohaymen.model.Message;
import com.mohaymen.model.Views;
import com.mohaymen.security.JwtHandler;
import com.mohaymen.service.MessageService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;
import java.util.Map;

@RestController
public class MessageController {

    private final MessageService messageService;

    public MessageController(MessageService messageService) {
        this.messageService = messageService;
    }

    @PostMapping("/{receiver}")
    public String SendMessage(@PathVariable Long receiver,
                              @RequestHeader(name = "Authorization") String token,
                              @RequestBody Map<String, Object> request) {
        long sender;
        Long replyMessage = null;
        String text;
        try {
            text = (String) request.get("text");
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
        try {
            sender = JwtHandler.getIdFromAccessToken(token);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE);
        }
        try {
            replyMessage = ((Number) request.get("replyMessage")).longValue();
        } catch (Exception ignored) {}
        if (messageService.sendMessage(sender, receiver, text, replyMessage)) return "Message is sent.";
        else return "Cannot send message!";
    }

    /**
     * direction = 0 : up, direction = 1 : down;
     * messageID = 0 : last messages
     */
    @JsonView(Views.GetMessage.class)
    @GetMapping("/{chatId}")
    public List<Message> getMessages(@PathVariable Long chatId,
                                     @RequestHeader(name = "Authorization") String token,
                                     @RequestParam(name = "message_id", defaultValue = "0") Long messageID,
                                     @RequestParam(name = "direction", defaultValue = "0") int direction) {
        Long userID;
        try {
            userID = JwtHandler.getIdFromAccessToken(token);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE);
        }
        return messageService.getMessages(chatId, userID, messageID, direction);
    }

    @PostMapping("/edit-message/{messageId}")
    public String editMessage(@PathVariable Long messageId,
                              @RequestBody Map<String, Object> request) {
        Long userID;
        String token, newMessage;
        try {
            token = (String) request.get("jwt");
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
                                @RequestBody Map<String, Object> request) {
        Long userID;
        String token;
        try {
            token = (String) request.get("jwt");
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
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
