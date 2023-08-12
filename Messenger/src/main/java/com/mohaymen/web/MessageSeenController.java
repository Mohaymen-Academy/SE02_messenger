package com.mohaymen.web;

import com.mohaymen.security.JwtHandler;
import com.mohaymen.service.MessageSeenService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

@RestController
public class MessageSeenController {

    private final MessageSeenService messageSeenService;

    public MessageSeenController(MessageSeenService messageSeenService) {
        this.messageSeenService = messageSeenService;
    }

    @PostMapping("/seen/{messageId}")
    public String addMessageView(@PathVariable Long messageId,
                                 @RequestBody Map<String, Object> request) {
        Long userId;
        String token;
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
        if (messageSeenService.addMessageView(userId, messageId))
            return "done";
        return "Cannot add view.";
    }
}
