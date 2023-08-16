package com.mohaymen.service;

import com.mohaymen.model.entity.ChatParticipant;
import com.mohaymen.model.entity.MediaFile;
import com.mohaymen.model.entity.Profile;
import com.mohaymen.model.entity.ProfilePicture;
import com.mohaymen.model.supplies.ChatType;
import com.mohaymen.model.supplies.ProfilePareId;
import com.mohaymen.model.supplies.ProfilePictureID;
import com.mohaymen.repository.ChatParticipantRepository;
import com.mohaymen.repository.MediaFileRepository;
import com.mohaymen.repository.ProfilePictureRepository;
import com.mohaymen.repository.ProfileRepository;
import lombok.Getter;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Map;
import java.util.Optional;
import java.util.ArrayList;
import java.util.List;

@Getter
@Service
public class ProfileService {

    private final ProfilePictureRepository profilePictureRepository;
    private final ProfileRepository profileRepository;
    private final MediaFileRepository mediaFileRepository;
    private final ChatParticipantRepository cpRepository;
    private final ServerService serverService;

    public ProfileService(ProfilePictureRepository profilePictureRepository,
                          ProfileRepository profileRepository,
                          MediaFileRepository mediaFileRepository,
                          ChatParticipantRepository cpRepository,
                          ServerService serverService) {
        this.profilePictureRepository = profilePictureRepository;
        this.profileRepository = profileRepository;
        this.mediaFileRepository = mediaFileRepository;
        this.cpRepository = cpRepository;
        this.serverService = serverService;
    }

    public boolean addProfilePicture(Long userId, Long profileID, MediaFile picture){
        ProfilePicture profilePicture = new ProfilePicture();
        Profile profile = hasPermission(userId, profileID);
        if(profile == null)
            return false;
        profilePicture.setProfile(profile);
        profilePicture.setMediaFile(picture);
        profile.setLastProfilePicture(picture);
        profilePictureNotDownloaded(profileID);
        profilePictureRepository.save(profilePicture);
        return true;
    }

    public boolean deleteProfilePicture(Long userId, Long profileId, Long mediaFileId){
        Profile profile = hasPermission(userId, profileId);
        if(profile == null)
            return false;
        ProfilePictureID profilePictureID = new ProfilePictureID(profile,
                mediaFileRepository.findById(mediaFileId).get());
        profilePictureRepository.delete(profilePictureRepository.findById(profilePictureID).get());
        return true;
    }

    public List<byte[]> getProfilePictures(Long userId, Long profileId){
        //check if this user id has blocked profile id
        List<ProfilePicture> profilePictures = profilePictureRepository.findByProfile_ProfileID(profileId);
        List<byte[]> pictureContents = new ArrayList<>();
        for(ProfilePicture profilePicture : profilePictures){
            pictureContents.add(profilePicture.getMediaFile().getContent());
        }
        return pictureContents;
    }

    public MediaFile uploadFile(Map<String, Object> fileData) throws Exception {
        MediaFile mediaFile = new MediaFile();
        String contentStr = (String) fileData.get("content");
        if(contentStr == null)
            return null;
        String fileSizeStr = (String) fileData.get("size");
        String contentType = (String) fileData.get("type");
        String fileName = (String) fileData.get("fileName");
        byte[] content = contentStr.getBytes();
        double fileSize = Double.parseDouble(fileSizeStr);
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
                .size(size,size)
                .outputFormat("jpg")
                .outputQuality(quality)
                .toOutputStream(output);

        return output.toByteArray();
    }

    public MediaFile getFile(Long id){
        return mediaFileRepository.findById(id).get();
    }

    private void editProfileName(Profile profile, String name, boolean isUser) {
        profile.setProfileName(name);
        profileRepository.save(profile);
        if(!isUser)
            serverService.sendMessage(profile.getType().name().toLowerCase()
                        + " name changed to " + name, profile);
    }

    private void editBiography(Profile profile, String newBio, boolean isUser) {
        profile.setBiography(newBio);
        profileRepository.save(profile);
        if(!isUser)
            serverService.sendMessage(profile.getType().name().toLowerCase()
                    + " Bio changed to " + newBio, profile);
    }

    private void editUsername(Profile profile, String newHandle) {
        profile.setHandle(newHandle);
        profileRepository.save(profile);
    }

    public boolean isNewHandleValid(Long profileId, String newHandle){
        return !profileRepository.existsByHandleAndProfileIDNot(newHandle, profileId);
    }

    public boolean editInfo(Long userId, Long profileId, String newName, String newBio, String newUsername){
        Profile profile = hasPermission(userId, profileId);
        if(profile == null)
            return false;
        boolean isUser = profile.getType() == ChatType.USER;
        if(newName != null)
            editProfileName(profile, newName, isUser);
        if(newBio != null)
            editBiography(profile, newBio, isUser);
        if(newUsername != null)
            editUsername(profile, newUsername);
        return true;
    }

    public Profile getProfile(Long profileId) {
        Optional<Profile> optionalProfile = profileRepository.findById(profileId);
        if (optionalProfile.isEmpty()) throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        return optionalProfile.get();
    }

    private Profile hasPermission(Long userId, Long profileId){
        Profile profile = profileRepository.findById(profileId).get();
        Profile user = profileRepository.findById(userId).get();
        if (profile.getType() == ChatType.USER){
            if(!userId.equals(profileId))
                return null;
        }
        else {
            ProfilePareId profilePareId = new ProfilePareId(user, profile);
            Optional<ChatParticipant> profilePareIdOptional = cpRepository.findById(profilePareId);
            if(profilePareIdOptional.isEmpty())
                return null;
            if(!profilePareIdOptional.get().isAdmin())
                return null;
        }
        return profile;
    }

    public void profilePictureNotDownloaded(Long userId){
        List<ChatParticipant> participants = cpRepository.findByDestination(profileRepository.
                findById(userId).get());
        for (ChatParticipant participant : participants){

            participant.setProfilePictureDownloaded(false);
            cpRepository.save(participant);
        }
    }

    public void profilePictureIsDownloaded(Long userId, Long profileId){
        Profile user = getProfile(userId);
        Profile profile = getProfile(profileId);
        ChatParticipant chatParticipant = cpRepository.findByDestinationAndUser(profile, user);
        chatParticipant.setProfilePictureDownloaded(true);
        cpRepository.save(chatParticipant);
    }
}
