package com.mohaymen.web;

import com.mohaymen.security.JwtHandler;
import com.mohaymen.service.BlockService;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class BlockController {

    private final BlockService blockService;

    public BlockController(BlockService blockService) {
        this.blockService = blockService;
    }

    @PostMapping("/block/{block_id}")
    public void blockUser(@RequestHeader(name = "Authorization") String token,
                          @PathVariable String block_id) {
        Long userId;
        try {
            userId = JwtHandler.getIdFromAccessToken(token);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        try {
            blockService.blockUser(userId, Long.parseLong(block_id));
        } catch (Exception e) {

        }
    }
}


