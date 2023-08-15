package com.mohaymen.web;

import com.mohaymen.security.JwtHandler;
import com.mohaymen.service.MessageSeenService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
public class MessageSeenController {

    private final MessageSeenService messageSeenService;

    public MessageSeenController(MessageSeenService messageSeenService) {
        this.messageSeenService = messageSeenService;
    }

    @PostMapping("/seen/{messageId}")
    public String addMessageView(@PathVariable Long messageId,
                                 @RequestHeader(name = "Authorization") String token) {
        Long userId;
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
