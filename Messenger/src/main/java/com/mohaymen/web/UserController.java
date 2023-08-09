package com.mohaymen.web;

import com.mohaymen.model.MediaFile;
import com.mohaymen.service.ProfileService;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

@RestController
public class UserController {

    private final ProfileService profileService;

    public UserController(ProfileService profileService){
        this.profileService = profileService;
    }

    @PostMapping("/profile/{id}")
    public String addProfilePicture(@PathVariable Long id, @RequestPart(value = "data") MultipartFile file){
        Long mediaID;
        try {
            mediaID = profileService.uploadFile(file.getSize(), file.getContentType(), file.getOriginalFilename(), file.getBytes());
            if(isImageFile(file))
                profileService.addCompressedImage(mediaID);
        } catch (Exception e){
            return "failed";
        }
        profileService.addProfilePicture(id, mediaID);
        return "ok";
    }

    private boolean isImageFile(MultipartFile file) {
        MediaType mediaType = MediaType.parseMediaType(file.getContentType());
        return mediaType.getType().equals("image");
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
