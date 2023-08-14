package com.mohaymen.web;

import com.mohaymen.model.MediaFile;
import com.mohaymen.security.JwtHandler;
import com.mohaymen.service.ProfileService;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

@RestController
public class UserController {

    private final ProfileService profileService;

    public UserController(ProfileService profileService) {
        this.profileService = profileService;
    }

    @PostMapping("/profile/{id}")
    public String addProfilePicture(@PathVariable Long id, @RequestPart(value = "data") MultipartFile file) {
        Long mediaID;
        try {
            mediaID = profileService.uploadFile(file.getSize(), file.getContentType(), file.getOriginalFilename(), file.getBytes());
            if (isImageFile(file))
                profileService.addCompressedImage(mediaID);
        } catch (Exception e) {
            return "failed";
        }
        profileService.addProfilePicture(id, mediaID);
        return "ok";
    }

    private boolean isImageFile(MultipartFile file) {
        MediaType mediaType = MediaType.parseMediaType(file.getContentType());
        return mediaType.getType().equals("image");
    }


    @PutMapping("/edit/biography")
    public ResponseEntity<?> editBiography(@RequestHeader(name = "Authentication") String token,
                                           @RequestBody Map<String, Object> request) {
        Long sender;
        try {
            sender = JwtHandler.getIdFromAccessToken(token);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
        String newBio = (String) request.get("biography");
        boolean isUpdated = profileService.editBiography(sender, newBio);
        if (isUpdated) {
            return ResponseEntity.ok().body("Biography updated successfully.");
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found.");
        }
    }

    @PutMapping("/edit/Username")
    public ResponseEntity<String> editUsername(@RequestHeader(name = "Authentication") String token,
                                               @RequestBody Map<String, Object> request) {
        Long sender;
        try {
            sender = JwtHandler.getIdFromAccessToken(token);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
        String newUsername = (String) request.get("username");
        try {
            profileService.editUsername(sender, newUsername);
            return ResponseEntity.ok().body("Username updated successfully");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to update username");
        }
    }

    @PutMapping("/edit/name")
    public ResponseEntity<String> editName(@RequestHeader(name = "Authentication") String token,
                                               @RequestBody Map<String, Object> request) {
        Long sender;
        try {
            sender = JwtHandler.getIdFromAccessToken(token);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
        String newName = (String) request.get("name");
        try {
            profileService.editProfileName(sender, newName);
            return ResponseEntity.ok().body("name updated successfully");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to update name");
        }
    }

//    @GetMapping("/download/{id}")
//    public ResponseEntity<byte[]> download(@PathVariable Long id) {
//        MediaFile photo = profileService.getFile(id);
//        if (photo == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND);
//
//        byte[] data = photo.getContent();
//        HttpHeaders headers = new HttpHeaders();
//        headers.setContentType(MediaType.valueOf(photo.getContentType()));
//        ContentDisposition build = ContentDisposition
//                .builder("attachment")
//                .filename(photo.getMediaName())
//                .build();
//        headers.setContentDisposition(build);
//
//        return new ResponseEntity<>(data, headers, HttpStatus.OK);
//    }
}
