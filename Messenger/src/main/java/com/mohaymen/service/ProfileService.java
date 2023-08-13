package com.mohaymen.service;

import com.mohaymen.model.MediaFile;
import com.mohaymen.model.Profile;
import com.mohaymen.model.ProfilePicture;
import com.mohaymen.repository.MediaFileRepository;
import com.mohaymen.repository.ProfilePictureRepository;
import com.mohaymen.repository.ProfileRepository;
import net.coobird.thumbnailator.Thumbnails;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

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

    public void addProfilePicture(Long profileID, MediaFile picture){
        ProfilePicture profilePicture = new ProfilePicture();
        Profile profile = profileRepository.findById(profileID).get();
        profilePicture.setProfile(profile);
        profilePicture.setMediaFile(picture);
        profile.setLastProfilePicture(picture);
        profilePictureRepository.save(profilePicture);
    }

    public List<byte[]> getProfilePictures(Long id){
        List<ProfilePicture> profilePictures = profilePictureRepository.findByProfile_ProfileID(id);
        List<byte[]> pictureContents = new ArrayList<>();
        for(ProfilePicture profilePicture : profilePictures){
            pictureContents.add(profilePicture.getMediaFile().getContent());
        }
        return pictureContents;
    }

    public MediaFile uploadFile(double contentSize, String contentType, String fileName, byte[] content) {
        MediaFile mediaFile = new MediaFile();
        mediaFile.setContentSize(contentSize);
        mediaFile.setContentType(contentType);
        mediaFile.setMediaName(fileName);
        mediaFile.setContent(content);
        mediaFileRepository.save(mediaFile);
        return mediaFile;
    }

    public void addCompressedImage(MediaFile mediaFile) throws Exception {
        mediaFile.setCompressedContent(compressFile(mediaFile.getContent(), 128, 0.5f));
        mediaFile.setPreLoadingContent(compressFile(mediaFile.getContent(), 8, 1));
        mediaFileRepository.save(mediaFile);
    }

    private byte[] compressFile(byte[] content, int size, float quality) throws Exception {
        ByteArrayInputStream input = new ByteArrayInputStream(content);
        BufferedImage image = ImageIO.read(input);

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        Thumbnails.of(image)
                .size(size,size)
                .outputFormat("jpg")
                .outputQuality(quality)
                .toOutputStream(output);

        return output.toByteArray();
    }

    public MediaFile getFile(Long id){
        return mediaFileRepository.findById(id).get();
    }
}
