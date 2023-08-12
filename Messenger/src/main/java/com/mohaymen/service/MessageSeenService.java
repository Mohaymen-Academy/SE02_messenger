package com.mohaymen.service;

import com.mohaymen.model.*;
import com.mohaymen.repository.MessageRepository;
import com.mohaymen.repository.MessageSeenRepository;
import com.mohaymen.repository.ProfileRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
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

    public boolean addMessageView(Long userId, Long messageId) {
        Profile user = getProfile(userId);
        Message message = getMessage(messageId);
        Profile destination = message.getReceiver();
        ProfilePareId profilePareId = new ProfilePareId(user, destination);
        Optional<MessageSeen> messageSeenOptional = msRepository.findById(profilePareId);
        MessageSeen messageSeen;
        if (messageSeenOptional.isPresent()) {
            messageSeen = messageSeenOptional.get();
            addAllMessagesViews(messageSeen.getLastMessageSeenId() + 1, messageId, user, destination);
            messageSeen.setLastMessageSeenId(messageId);
        }
        else {
            messageSeen = new MessageSeen(user, destination, messageId);
            Long firstMessageId = destination.getType() == ChatType.USER
                    ? messageRepository.findFirstBySenderAndReceiver(user, destination).getMessageID()
                    : messageRepository.findFirstByReceiver(destination).getMessageID();
            System.out.println(firstMessageId);
            System.out.println("******************");
            addAllMessagesViews(firstMessageId, messageId, user, destination);
        }
        msRepository.save(messageSeen);
        return true;
    }

    private void addAllMessagesViews(Long minMessageId, Long maxMessageId,
                                     Profile user, Profile destination) {
        List<Message> messages;
        if (destination.getType() == ChatType.USER)
            messages = messageRepository.findBySenderAndReceiverAndMessageIDBetween
                    (user, destination, minMessageId, maxMessageId);
        else
            messages = messageRepository.findByReceiverAndMessageIDBetween
                    (destination, minMessageId, maxMessageId);
        messages.forEach(m -> System.out.println(m.getMessageID()));
        messages.stream().map(Message::addView).forEach(messageRepository::save);
    }

    private Message getMessage(Long messageId) {
        Optional<Message> messageOptional = messageRepository.findById(messageId);
        if (messageOptional.isEmpty()) throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        return messageOptional.get();
    }

    private Profile getProfile(Long profileId) {
        Optional<Profile> optionalProfile = profileRepository.findById(profileId);
        if (optionalProfile.isEmpty()) throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        return optionalProfile.get();
    }
}
