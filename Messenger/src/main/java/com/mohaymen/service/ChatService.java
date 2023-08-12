package com.mohaymen.service;

import com.mohaymen.model.*;
import com.mohaymen.repository.*;
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
    private final MessageSeenRepository msRepository;

    public ChatService(ChatParticipantRepository cpRepository,
                       ProfileRepository profileRepository,
                       ContactRepository contactRepository,
                       MessageRepository messageRepository,
                       MessageSeenRepository msRepository) {
        this.cpRepository = cpRepository;
        this.profileRepository = profileRepository;
        this.contactRepository = contactRepository;
        this.messageRepository = messageRepository;
        this.msRepository = msRepository;
    }

    public List<ChatDisplay> getChats(Long userId) {
        Profile user = getProfile(userId);
        List<ChatParticipant> participants = cpRepository.findByUser(user);
        List<ChatDisplay> chats = new ArrayList<>();
        for (ChatParticipant p : participants) {
            Profile profile = getProfile(p.getDestination().getProfileID());
            profile.setProfileName(getProfileDisplayName(user, profile));
            ChatDisplay chatDisplay = ChatDisplay.builder()
                    .profile(profile)
                    .lastMessage(getLastMessage(user, profile))
                    .unreadMessageCount(getUnreadMessageCount(user, profile,
                            getLastMessageId(user, profile)))
                    .build();
            chats.add(chatDisplay);
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

    private long getLastMessageId(Profile user, Profile profile) {
        ProfilePareId profilePareId = new ProfilePareId(user, profile);
        Optional<MessageSeen> messageSeenOptional = msRepository.findById(profilePareId);
        if (messageSeenOptional.isEmpty()) return 0;
        else return messageSeenOptional.get().getLastMessageSeenId();
    }

}
