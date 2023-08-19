package com.mohaymen.web;

import com.fasterxml.jackson.annotation.JsonView;
import com.mohaymen.model.entity.MediaFile;
import com.mohaymen.model.json_item.Views;
import com.mohaymen.security.JwtHandler;
import com.mohaymen.service.MediaService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

public class MediaFileController {

    private MediaService mediaService;

    public MediaFileController(MediaService mediaService){
        this.mediaService = mediaService;
    }

    @PostMapping("/picture/{id}")
    public ResponseEntity<String> addProfilePicture(@PathVariable Long id, @RequestBody Map<String, Object> data,
                                                    @RequestHeader(name = "Authorization") String token){
        MediaFile mediaFile;
        Long userId;
        try {
            userId = JwtHandler.getIdFromAccessToken(token);
        } catch (Exception e){
            return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body("fail");
        }
        try {
            mediaFile = mediaService.uploadFile(data);
        } catch (Exception e){
            return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body("fail");
        }
        if(!mediaService.addProfilePicture(userId, id, mediaFile))
            return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body("fail");

        return ResponseEntity.ok().body("successful");
    }

    @ResponseBody
    @DeleteMapping("/picture/{id}/{mediaFileId}")
    public ResponseEntity<String> deleteProfilePhoto(@PathVariable Long id, @PathVariable Long mediaFileId, @RequestBody Map<String, Object> data){
        Long userId;
        try {
            userId = JwtHandler.getIdFromAccessToken((String) data.get("jwt"));
        } catch (Exception e){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("invalid jwt");
        }
        if(!mediaService.deleteProfilePicture(userId, id, mediaFileId))
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("have not permission");
        return ResponseEntity.status(HttpStatus.OK).body("successfully deleted");
    }

    @JsonView(Views.getOriginalPicture.class)
    @GetMapping("/picture/{id}/{mediaId}")
    public ResponseEntity<MediaFile> getOriginalProfilePicture(@PathVariable Long id, @PathVariable Long mediaId, @RequestBody Map<String, Object> input){
        Long userId;
        try {
            userId = JwtHandler.getIdFromAccessToken((String) input.get("jwt"));
        } catch (Exception e){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
        return ResponseEntity.ok().body(mediaService.getOriginalProfilePicture(userId, id, mediaId));
    }

    private boolean isImageFile(MultipartFile file) {
        MediaType mediaType = MediaType.parseMediaType(file.getContentType());
        return mediaType.getType().equals("image");
    }

    @JsonView(Views.getCompressedPicture.class)
    @GetMapping("/compressed-profile/{id}")
    public ResponseEntity<MediaFile> getCompressedProfilePicture(@PathVariable Long id,
                                                                 @RequestHeader(name = "Authorization") String token) {
        //check if user is blocked or no
        Long userId;
        try {
            userId = JwtHandler.getIdFromAccessToken(token);
        } catch (Exception e){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
        return ResponseEntity.ok().body(mediaService.getCompressedProfilePicture(id));
    }
}
