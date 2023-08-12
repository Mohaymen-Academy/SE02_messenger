package com.mohaymen.service;

import com.mohaymen.model.ChatParticipant;
import com.mohaymen.model.ChatType;
import com.mohaymen.model.Profile;
import com.mohaymen.model.ChatDisplay;
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
            Long messageId = p.getLastMessageSeen() != null ?
                    p.getLastMessageSeen().getMessageID() : 0;
            ChatDisplay profileDisplay = ChatDisplay.builder()
                    .profile(profile)
                    // change to last message of chat instead of last seen message
                    .lastMessage(p.getLastMessageSeen())
                    .unreadMessageCount(getUnreadMessageCount(user, profile, messageId))
                    .build();
            chats.add(profileDisplay);
        }
        return chats;
    }

    private String getProfileDisplayName(Profile user, Long profileId) {
        Optional<Profile> profileOptional = profileRepository.findById(profileId);
        if (profileOptional.isEmpty()) throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        return new ContactService(contactRepository, profileRepository)
                .getProfileDisplayName(user, profileOptional.get());
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
}
