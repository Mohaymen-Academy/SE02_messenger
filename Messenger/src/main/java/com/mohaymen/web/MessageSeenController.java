package com.mohaymen.web;

import com.mohaymen.security.JwtHandler;
import com.mohaymen.service.LogService;
import com.mohaymen.service.MessageSeenService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class MessageSeenController {

    private final MessageSeenService messageSeenService;
    private final LogService logger;

    public MessageSeenController(MessageSeenService messageSeenService,
                                 LogService logger) {
        this.messageSeenService = messageSeenService;
        this.logger = logger;
        logger.setLogger(MessageSeenController.class.getName());
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
            logger.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).build();
        }
    }
}
