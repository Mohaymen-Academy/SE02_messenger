package com.mohaymen.service;

import com.mohaymen.model.entity.Message;
import com.mohaymen.model.entity.MessageSeen;
import com.mohaymen.model.entity.Profile;
import com.mohaymen.model.supplies.ChatType;
import com.mohaymen.model.supplies.ProfilePareId;
import com.mohaymen.repository.MessageRepository;
import com.mohaymen.repository.MessageSeenRepository;
import com.mohaymen.repository.ProfileRepository;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class MessageSeenService {

    private final ProfileRepository profileRepository;
    private final MessageRepository messageRepository;
    private final MessageSeenRepository msRepository;
    public MessageSeenService(ProfileRepository profileRepository,
                              MessageRepository messageRepository,
                              MessageSeenRepository msRepository) {
        this.profileRepository = profileRepository;
        this.messageRepository = messageRepository;
        this.msRepository = msRepository;

    }

    public void addMessageView(Long userId, Long messageId) throws Exception {
        Profile user = getProfile(userId);
        Message message = getMessage(messageId);
        Profile destination = message.getReceiver().getProfileID()
                .equals(userId) ? message.getSender() : message.getReceiver();
        ProfilePareId profilePareId = new ProfilePareId(user, destination);
        Optional<MessageSeen> messageSeenOptional = msRepository.findById(profilePareId);
        MessageSeen messageSeen;
        if (messageSeenOptional.isPresent()) {
            messageSeen = messageSeenOptional.get();
            Long lastMessageSeen = messageSeen.getLastMessageSeenId();
            addAllMessagesViews(lastMessageSeen + 1, messageId, user, destination);
            messageSeen.setLastMessageSeenId(Math.max(lastMessageSeen, messageId));
        }
        else {
            messageSeen = new MessageSeen(user, destination, messageId);
            Long firstMessageId = destination.getType() == ChatType.USER
                    ? messageRepository.findPVFirstMessage(user, destination).getMessageID()
                    : messageRepository.findFirstByReceiver(destination).getMessageID();
            addAllMessagesViews(firstMessageId, messageId, user, destination);
        }
        msRepository.save(messageSeen);
    }

    private void addAllMessagesViews(Long minMessageId, Long maxMessageId,
                                     Profile user, Profile destination) {
        List<Message> messages;
        if (destination.getType() == ChatType.USER)
            messages = messageRepository.findMessagesInRange
                    (user, destination, minMessageId, maxMessageId);
        else
            messages = messageRepository.findByReceiverAndMessageIDGreaterThanAndMessageIDLessThan
                    (destination, minMessageId, maxMessageId);

        messages.stream().map(Message::addView).forEach(messageRepository::save);
    }

    private Message getMessage(Long messageId) throws Exception {
        Optional<Message> messageOptional = messageRepository.findById(messageId);
        if (messageOptional.isEmpty()) throw new Exception("message not found!");
        return messageOptional.get();
    }

    private Profile getProfile(Long profileId) throws Exception {
        Optional<Profile> optionalProfile = profileRepository.findById(profileId);
        if (optionalProfile.isEmpty()) throw new Exception("profile not found!");
        return optionalProfile.get();
    }
}
