package com.mohaymen.web;

import com.mohaymen.model.entity.MediaFile;
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
public class UserController {

    private final ProfileService profileService;

    public UserController(ProfileService profileService){
        this.profileService = profileService;
    }

    @PostMapping("/add-profile")
    public String addProfilePicture(@RequestPart(value = "data") MultipartFile file,
                                    @RequestHeader(name = "Authorization") String token){
        MediaFile mediaFile;
        Long id;
        try {
            id = JwtHandler.getIdFromAccessToken(token);
        } catch (Exception e){
            throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE);
        }
        try {
            mediaFile = profileService.uploadFile(file.getSize(), file.getContentType(), file.getOriginalFilename(), file.getBytes());
//            if(isImageFile(file))
                profileService.addCompressedImage(mediaFile);
        } catch (Exception e){
            return "failed";
        }
        profileService.addProfilePicture(id, mediaFile);
        return "ok";
    }

    @GetMapping("/download")
    public List<byte[]> getProfiles(@RequestBody Map<String, Object> input){
        Long id;
        try {
            id = JwtHandler.getIdFromAccessToken((String) input.get("jwt"));
        } catch (Exception e){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
        return profileService.getProfilePictures(id);
    }

    private boolean isImageFile(MultipartFile file) {
        MediaType mediaType = MediaType.parseMediaType(file.getContentType());
        return mediaType.getType().equals("image");
    }

//    @GetMapping("")
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
