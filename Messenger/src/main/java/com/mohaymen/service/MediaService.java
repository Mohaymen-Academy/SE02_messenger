package com.mohaymen.service;

import com.mohaymen.model.entity.MediaFile;
import com.mohaymen.model.entity.Profile;
import com.mohaymen.model.entity.ProfilePicture;
import com.mohaymen.model.supplies.ProfilePictureID;
import com.mohaymen.repository.MediaFileRepository;
import com.mohaymen.repository.ProfilePictureRepository;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.stereotype.Service;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Base64;
import java.util.Map;
import java.util.Optional;

@Service
public class MediaService {

    private ProfileService profileService;
    private ProfilePictureRepository profilePictureRepository;
    private AccountService accountService;
    private MediaFileRepository mediaFileRepository;

    public MediaService(ProfileService profileService,
                        ProfilePictureRepository profilePictureRepository,
                        AccountService accountService,
                        MediaFileRepository mediaFileRepository){
        this.profileService = profileService;
        this.profilePictureRepository = profilePictureRepository;
        this.accountService = accountService;
        this.mediaFileRepository = mediaFileRepository;
    }

    public boolean addProfilePicture(Long userId, Long profileID, MediaFile picture) {
        ProfilePicture profilePicture = new ProfilePicture();
        Profile profile = profileService.hasPermission(userId, profileID);
        if (profile == null)
            return false;
        profilePicture.setProfile(profile);
        profilePicture.setMediaFile(picture);
        profile.setLastProfilePicture(picture);
//        profilePictureNotDownloaded(profileID);
        profilePictureRepository.save(profilePicture);
        accountService.UpdateLastSeen(userId);
        return true;
    }

    public boolean deleteProfilePicture(Long userId, Long profileId, Long mediaFileId) {
        Profile profile = profileService.hasPermission(userId, profileId);
        if (profile == null)
            return false;
        ProfilePictureID profilePictureID = new ProfilePictureID(profile,
                mediaFileRepository.findById(mediaFileId).get());
        profilePictureRepository.delete(profilePictureRepository.findById(profilePictureID).get());
        deleteFile(mediaFileId);
        accountService.UpdateLastSeen(userId);
        return true;
    }

    public void deleteFile(Long mediaId){
        mediaFileRepository.deleteById(mediaId);
    }

    public MediaFile getOriginalProfilePicture(Long mediaFileId) {
        //check if this user id has blocked profile id
        return mediaFileRepository.findById(mediaFileId).get();
    }

    public MediaFile uploadFile(Map<String, Object> fileData) throws Exception {
        MediaFile mediaFile = new MediaFile();
        String contentStr = (String) fileData.get("content");
        if (contentStr == null)
            return null;
        double fileSize = ((Number) fileData.get("size")).doubleValue();
        String contentType = (String) fileData.get("media-type");
        String fileName = (String) fileData.get("fileName");
        byte[] content = Base64.getDecoder().decode(contentStr);
        mediaFile.setContentSize(fileSize);
        mediaFile.setContentType(contentType);
        mediaFile.setMediaName(fileName);
        mediaFile.setContent(content);
        addCompressedImage(mediaFile);
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
                .size(size, size)
                .outputFormat("jpg")
                .outputQuality(quality)
                .toOutputStream(output);

        return output.toByteArray();
    }

    public MediaFile getFile(Long id) {
        return mediaFileRepository.findById(id).get();
    }

    public MediaFile getCompressedPicture(Long mediaId){
        return mediaFileRepository.findById(mediaId).get();
    }

    public MediaFile getMedia(Long mediaFile) {
        Optional<MediaFile> optionalMediaFile = mediaFileRepository.findById(mediaFile);
        return optionalMediaFile.orElse(null);
    }
}
