package com.mohaymen.web;

import com.mohaymen.security.JwtHandler;
import com.mohaymen.service.MessageSeenService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@CrossOrigin
@RestController
public class MessageSeenController {

    private final MessageSeenService messageSeenService;

    public MessageSeenController(MessageSeenService messageSeenService) {
        this.messageSeenService = messageSeenService;
    }

    @PostMapping("/seen/{messageId}")
    public ResponseEntity<String> addMessageView(@PathVariable Long messageId,
                                         @RequestHeader(name = "Authorization") String token) {
        Long userId;
        try {
            userId = JwtHandler.getIdFromAccessToken(token);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).build();
        }
        try {
            messageSeenService.addMessageView(userId, messageId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).build();
        }
    }
}
