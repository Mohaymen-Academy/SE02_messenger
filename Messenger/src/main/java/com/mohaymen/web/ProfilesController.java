package com.mohaymen.web;

import com.fasterxml.jackson.annotation.JsonView;
import com.mohaymen.model.entity.MediaFile;
import com.mohaymen.model.json_item.Views;
import com.mohaymen.model.supplies.ProfilePareId;
import com.mohaymen.security.JwtHandler;
import com.mohaymen.service.ProfileService;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/profile")
public class ProfilesController {

    private final ProfileService profileService;

    public ProfilesController(ProfileService profileService) {
        this.profileService = profileService;
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
            mediaFile = profileService.uploadFile(data);
        } catch (Exception e){
            return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body("fail");
        }
        if(!profileService.addProfilePicture(userId, id, mediaFile))
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
        if(!profileService.deleteProfilePicture(userId, id, mediaFileId))
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("have not permission");
        return ResponseEntity.status(HttpStatus.OK).body("successfully deleted");
    }

    @GetMapping("/picture/{id}")
    public List<byte[]> getProfiles(@PathVariable Long id, @RequestBody Map<String, Object> input){
        Long userId;
        try {
            userId = JwtHandler.getIdFromAccessToken((String) input.get("jwt"));
        } catch (Exception e){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
        return profileService.getProfilePictures(userId, id);
    }

    private boolean isImageFile(MultipartFile file) {
        MediaType mediaType = MediaType.parseMediaType(file.getContentType());
        return mediaType.getType().equals("image");
    }

    @GetMapping("/username-validation")
    public ResponseEntity<String> isNewUsernameValid(@RequestHeader(name = "Authorization") String token,
                                                     @RequestBody Map<String, Object> request){
        Long userId;
        try {
            userId = JwtHandler.getIdFromAccessToken(token);
        } catch (Exception e){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("invalid jwt");
        }
        String newUsername = (String) request.get("username");
        if(!profileService.isNewHandleValid(userId, newUsername))
            return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body("Invalid username");
        return ResponseEntity.ok().body("valid username");
    }

    @PutMapping("/edit-info/{id}")
    public ResponseEntity<String> editInfo(@PathVariable Long id,
                                           @RequestHeader(name = "Authorization") String token,
                                           @RequestBody Map<String, Object> request) {
        Long userId;
        try {
            userId = JwtHandler.getIdFromAccessToken(token);
        } catch (Exception e){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("invalid jwt");
        }
        String newName = (String) request.get("name");
        String newBio = (String) request.get("biography");
        String newUsername = (String) request.get("username");
        if(!profileService.editInfo(userId, id, newName, newBio, newUsername))
            return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body("invalid edit");
        return ResponseEntity.ok().body("successful");
    }

    @JsonView(Views.getCompressedProfilePicture.class)
    @GetMapping("/compressed-profile/{id}")
    public ResponseEntity<MediaFile> getCompressedProfilePicture(@PathVariable Long id,
                                                                  @RequestHeader(name = "Authorization") String token) {
        Long userId;
        try {
            userId = JwtHandler.getIdFromAccessToken(token);
        } catch (Exception e){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
        return ResponseEntity.ok().body(profileService.getCompressedProfilePicture(id));
    }
//    @GetMapping("/download/{id}")
//    public ResponseEntity<ByteArrayResource> download(@PathVariable Long id) {
//        MediaFile photo = profileService.getFile(id);
//        if (photo == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND);
//
//        byte[] data = photo.getCompressedContent();
//        ByteArrayResource byteArrayResource = new ByteArrayResource(data);
//
//        return ResponseEntity.ok().contentType(MediaType.APPLICATION_OCTET_STREAM)
//                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + photo.getMediaName() + "\"")
//                .body(byteArrayResource);
//    }
}
