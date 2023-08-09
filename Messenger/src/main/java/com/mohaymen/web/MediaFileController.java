package com.mohaymen.web;

import net.coobird.thumbnailator.Thumbnails;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

@RestController
public class MediaFileController {

    @PostMapping("/upload/{id}")
    public String uploadCompressedProfile(@PathVariable Long id, @RequestParam("file") MultipartFile file) {
        // Compress the image and save it
//        byte[] compressedImage = compressImage(file);
        // You can save or process the compressed image as per your requirement
        // ...
        return "Image uploaded and compressed successfully";
    }
}
