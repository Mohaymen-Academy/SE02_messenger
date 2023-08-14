package com.mohaymen.service;

import com.mohaymen.model.entity.ChatParticipant;
import com.mohaymen.model.entity.MediaFile;
import com.mohaymen.model.entity.Profile;
import com.mohaymen.model.entity.ProfilePicture;
import com.mohaymen.model.supplies.ChatType;
import com.mohaymen.model.supplies.ProfilePareId;
import com.mohaymen.repository.ChatParticipantRepository;
import com.mohaymen.repository.MediaFileRepository;
import com.mohaymen.repository.ProfilePictureRepository;
import com.mohaymen.repository.ProfileRepository;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class ProfileService {

    private final ProfilePictureRepository profilePictureRepository;
    private final ProfileRepository profileRepository;
    private final MediaFileRepository mediaFileRepository;
    private final ChatParticipantRepository cpRepository;

    public ProfileService(ProfilePictureRepository profilePictureRepository,
                          ProfileRepository profileRepository,
                          MediaFileRepository mediaFileRepository,
                          ChatParticipantRepository cpRepository) {
        this.profilePictureRepository = profilePictureRepository;
        this.profileRepository = profileRepository;
        this.mediaFileRepository = mediaFileRepository;
        this.cpRepository = cpRepository;
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

    public boolean editProfileName(Long userId, Long profileId, String name) {
        Profile user = getProfile(userId);
        if (profileId != null)  {
            Profile profile = getProfile(profileId);
            if (!profile.getType().equals(ChatType.USER)) {
                Optional<ChatParticipant> cpOptional = cpRepository.findById(new ProfilePareId(user, profile));
                if (cpOptional.isEmpty()) return false;
                if (!cpOptional.get().isAdmin()) return false;
                profile.setProfileName(name);
                profileRepository.save(profile);
                return true;
            }
        }
        user.setProfileName(name);
        profileRepository.save(user);
        return true;
    }

    private Profile getProfile(Long profileId) {
        Optional<Profile> optionalProfile = profileRepository.findById(profileId);
        if (optionalProfile.isEmpty()) throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        return optionalProfile.get();
    }
}
