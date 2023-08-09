package com.mohaymen.web;

import com.mohaymen.model.Message;
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
             @RequestBody Map<String, Object> request) {
        long sender;
        String text, token;
        try {
            token = (String) request.get("jwt");
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE);
        }
        try {
            sender = JwtHandler.getIdFromAccessToken(token);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
        try {
            text = (String) request.get("text");
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
        if (messageService.sendMessage(sender, receiver, text)) return "Message is sent.";
        else return "Cannot send message!";
    }

    /**
     * direction = 0 : up, direction = 1 : down;
     * messageID = 0 : last messages
     */
    @GetMapping("/{chatID}")
    public List<Message> getMessages(@PathVariable Long chatID,
                                     @RequestBody Map<String, Object> request) {
        Long userID;
        String token;
        int direction;
        long messageID;
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
        try {
            messageID = ((Number) request.get("message_id")).longValue();
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
        try {
            direction = (Integer) request.get("direction");
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
        return messageService.getMessages(chatID, userID, messageID, direction);
    }
}
