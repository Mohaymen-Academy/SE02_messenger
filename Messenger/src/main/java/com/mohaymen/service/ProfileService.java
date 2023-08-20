package com.mohaymen.service;

import com.mohaymen.model.entity.ChatParticipant;
import com.mohaymen.model.entity.MediaFile;
import com.mohaymen.model.entity.Profile;
import com.mohaymen.model.entity.ProfilePicture;
import com.mohaymen.model.json_item.ProfileInfo;
import com.mohaymen.model.supplies.ChatType;
import com.mohaymen.model.supplies.ContactID;
import com.mohaymen.model.supplies.ProfilePareId;
import com.mohaymen.repository.ChatParticipantRepository;
import com.mohaymen.repository.MediaFileRepository;
import com.mohaymen.repository.ProfilePictureRepository;
import com.mohaymen.repository.ProfileRepository;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import java.util.*;

@Getter
@Service
public class ProfileService {

    private final ProfilePictureRepository profilePictureRepository;
    private final ProfileRepository profileRepository;
    private final MediaFileRepository mediaFileRepository;
    private final ChatParticipantRepository cpRepository;
    private final ServerService serverService;
    private final SearchService searchService;
    private final ContactService contactService;

    public ProfileService(ProfilePictureRepository profilePictureRepository,
                          ProfileRepository profileRepository,
                          MediaFileRepository mediaFileRepository,
                          ChatParticipantRepository cpRepository,
                          ServerService serverService,
                          SearchService searchService, ContactService contactService) {
        this.profilePictureRepository = profilePictureRepository;
        this.profileRepository = profileRepository;
        this.mediaFileRepository = mediaFileRepository;
        this.cpRepository = cpRepository;
        this.serverService = serverService;
        this.searchService = searchService;
        this.contactService = contactService;
    }

    private void editProfileName(Profile profile, String name, boolean isUser) {
        profile.setProfileName(name);
        profileRepository.save(profile);
        if (!isUser)
            serverService.sendMessage(profile.getType().name().toLowerCase()
                    + " name changed to " + name, profile);
    }

    private void editBiography(Profile profile, String newBio, boolean isUser) {
        profile.setBiography(newBio);
        profileRepository.save(profile);
        if (!isUser)
            serverService.sendMessage(profile.getType().name().toLowerCase()
                    + " Bio changed to " + newBio, profile);
    }

    private void editUsername(Profile profile, String newHandle) {
        profile.setHandle(newHandle);
        profileRepository.save(profile);
    }

    public boolean isNewHandleValid(Long profileId, String newHandle) {
        return !profileRepository.existsByHandleAndProfileIDNot(newHandle, profileId);
    }

    public boolean editInfo(Long userId, Long profileId, String newName, String newBio, String newUsername) {
        Profile profile = hasPermission(userId, profileId);
        if (profile == null)
            return false;
        boolean isUser = profile.getType() == ChatType.USER;
        if (newName != null)
            editProfileName(profile, newName, isUser);
        if (newBio != null)
            editBiography(profile, newBio, isUser);
        if (newUsername != null)
            editUsername(profile, newUsername);

        searchService.updateUser(profile);

        return true;
    }

    public Profile getProfile(Long profileId) {
        Optional<Profile> optionalProfile = profileRepository.findById(profileId);
        if (optionalProfile.isEmpty()) throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        return optionalProfile.get();
    }

    public Profile hasPermission(Long userId, Long profileId) {
        Profile profile = profileRepository.findById(profileId).get();
        Profile user = profileRepository.findById(userId).get();
        if (profile.getType() == ChatType.USER) {
            if (!userId.equals(profileId))
                return null;
        } else {
            ProfilePareId profilePareId = new ProfilePareId(user, profile);
            Optional<ChatParticipant> profilePareIdOptional = cpRepository.findById(profilePareId);
            if (profilePareIdOptional.isEmpty())
                return null;
            if (!profilePareIdOptional.get().isAdmin())
                return null;
        }
        return profile;
    }

    public ProfileInfo getInfo(Long userId, Long profileId){
        Profile profile = profileRepository.findById(profileId).get();
        Profile user = profileRepository.findById(userId).get();
        ContactID contactID = new ContactID(user, profile);
        boolean isContact = contactService.contactExists(contactID) != null;
        List<MediaFile> preLoadingProfiles = new ArrayList<>();
        List<ProfilePicture> profilePictures = profilePictureRepository.findByProfile_ProfileID(profileId);
        for (ProfilePicture profilePicture : profilePictures){
            preLoadingProfiles.add(profilePicture.getMediaFile());
        }
        return ProfileInfo.builder().isContact(isContact).
                profile(profile).profilePictures(preLoadingProfiles).build();
    }

//    public void profilePictureNotDownloaded(Long userId){
//        List<ChatParticipant> participants = cpRepository.findByDestination(profileRepository.
//                findById(userId).get());
//        for (ChatParticipant participant : participants){
//
//            participant.setProfilePictureDownloaded(false);
//            cpRepository.save(participant);
//        }
//    }

//    public void profilePictureIsDownloaded(Long userId, Long profileId){
//        Profile user = getProfile(userId);
//        Profile profile = getProfile(profileId);
//        ChatParticipant chatParticipant = cpRepository.findByDestinationAndUser(profile, user);
//        if(chatParticipant == null)
//            return;
//        chatParticipant.setProfilePictureDownloaded(true);
//        cpRepository.save(chatParticipant);
//    }
}
