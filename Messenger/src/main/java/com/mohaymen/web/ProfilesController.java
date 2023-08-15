package com.mohaymen.web;

import com.mohaymen.security.JwtHandler;
import com.mohaymen.service.ProfileService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import java.util.Map;

@RestController
public class ProfilesController {

    private final ProfileService profileService;

    public ProfilesController(ProfileService profileService) {
        this.profileService = profileService;
    }

    @PostMapping("/edit-name")
    public ResponseEntity<String> editProfileName(@RequestBody Map<String, Object> request) {
        String token, name;
        Long userId, profileId = null;
        try {
            token = (String) request.get("jwt");
            name = (String) request.get("name");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Cast error!");
        }
        try {
            userId = JwtHandler.getIdFromAccessToken(token);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body("User id is not acceptable!");
        }
        try {
            profileId = ((Number) request.get("profileId")).longValue();
        } catch (Exception ignored) {}
        try {
            profileService.editProfileName(userId, profileId, name);
            return ResponseEntity.status(HttpStatus.OK).body("successful");
        }
        catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE)
                    .body("You are not allowed to change the name!");
        }
    }

}