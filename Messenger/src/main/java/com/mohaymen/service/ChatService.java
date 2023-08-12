package com.mohaymen.service;

import com.mohaymen.model.*;
import com.mohaymen.repository.ChatParticipantRepository;
import com.mohaymen.repository.ContactRepository;
import com.mohaymen.repository.MessageRepository;
import com.mohaymen.repository.ProfileRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class ChatService {

    private final ChatParticipantRepository cpRepository;
    private final ProfileRepository profileRepository;
    private final ContactRepository contactRepository;
    private final MessageRepository messageRepository;

    public ChatService(ChatParticipantRepository cpRepository,
                       ProfileRepository profileRepository,
                       ContactRepository contactRepository,
                       MessageRepository messageRepository) {
        this.cpRepository = cpRepository;
        this.profileRepository = profileRepository;
        this.contactRepository = contactRepository;
        this.messageRepository = messageRepository;
    }

    public List<ChatDisplay> getChats(Long userId) {
        Profile user = getProfile(userId);
        List<ChatParticipant> participants = cpRepository.findByUser(user);
        List<ChatDisplay> chats = new ArrayList<>();
        for (ChatParticipant p : participants) {
            Profile profile = getProfile(p.getDestination().getProfileID());
            profile.setProfileName(getProfileDisplayName(user, profile));
            Long messageId = p.getLastMessageSeen() != null ?
                    p.getLastMessageSeen().getMessageID() : 0;
            ChatDisplay profileDisplay = ChatDisplay.builder()
                    .profile(profile)
                    .lastMessage(getLastMessage(user, profile))
                    .unreadMessageCount(getUnreadMessageCount(user, profile, messageId))
                    .build();
            chats.add(profileDisplay);
        }
        return chats;
    }

    private int getUnreadMessageCount(Profile user, Profile profile, Long messageId) {
        if (profile.getType() == ChatType.USER)
            return messageRepository.countBySenderAndReceiverAndMessageIDGreaterThan(profile, user, messageId);
        else
            return messageRepository.countByReceiverAndMessageIDGreaterThan(profile, messageId);
    }

    private Profile getProfile(Long profileId) {
        Optional<Profile> optionalProfile = profileRepository.findById(profileId);
        if (optionalProfile.isEmpty()) throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        return optionalProfile.get();
    }

    private String getProfileDisplayName(Profile user, Profile profile) {
        return new ContactService(contactRepository, profileRepository)
                .getProfileWithCustomName(user, profile).getProfileName();
    }

    private Message getLastMessage(Profile user, Profile profile) {
        if (profile.getType() == ChatType.USER)
            return messageRepository.findTopBySenderAndReceiverOrderByMessageIDDesc(user, profile);
        else
            return messageRepository.findTopByReceiverOrderByMessageIDDesc(profile);
    }

}
