package com.mohaymen.service;

import com.mohaymen.model.MediaFile;
import com.mohaymen.model.ProfilePicture;
import com.mohaymen.repository.MediaFileRepository;
import com.mohaymen.repository.ProfilePictureRepository;
import com.mohaymen.repository.ProfileRepository;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

@Service
public class ProfileService {

    private final ProfilePictureRepository profilePictureRepository;
    private final ProfileRepository profileRepository;
    private final MediaFileRepository mediaFileRepository;

    public ProfileService(ProfilePictureRepository profilePictureRepository, ProfileRepository profileRepository, MediaFileRepository mediaFileRepository) {
        this.profilePictureRepository = profilePictureRepository;
        this.profileRepository = profileRepository;
        this.mediaFileRepository = mediaFileRepository;
    }

    public void addProfilePicture(Long profileID, Long mediaID){
        ProfilePicture profilePicture = new ProfilePicture();
        profilePicture.setProfile(profileRepository.findById(profileID).get());
        profilePicture.setMediaFile(mediaFileRepository.findById(mediaID).get());
        profilePictureRepository.save(profilePicture);
    }

    public Long uploadFile(double contentSize, String contentType, String fileName, byte[] content) {
        MediaFile mediaFile = new MediaFile();
        mediaFile.setContentSize(contentSize);
        mediaFile.setContentType(contentType);
        mediaFile.setMediaName(fileName);
        mediaFile.setContent(content);
        mediaFileRepository.save(mediaFile);
        return mediaFile.getMediaId();
    }

    public void addCompressedImage(Long mediaFileID) throws Exception {
        MediaFile mediaFile = mediaFileRepository.findById(mediaFileID).get();
        mediaFile.setCompressedContent(compressFile(mediaFile.getContent()));
        mediaFileRepository.save(mediaFile);
    }

    private byte[] compressFile(byte[] content) throws Exception {
        ByteArrayInputStream input = new ByteArrayInputStream(content);
        BufferedImage image = ImageIO.read(input);

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        Thumbnails.of(image)
                .size(200,200)
                .outputFormat("jpg")
                .outputQuality(0.2) // Set the desired image quality (0.1 to 1.0)
                .toOutputStream(output);

        return output.toByteArray();
    }

    public MediaFile getFile(Long fileID){
        return mediaFileRepository.findById(fileID).get();
    }
}
