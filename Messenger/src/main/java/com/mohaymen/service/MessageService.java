package com.mohaymen.service;

import com.mohaymen.model.*;
import com.mohaymen.repository.ChatParticipantRepository;
import com.mohaymen.repository.MessageRepository;
import com.mohaymen.repository.ProfileRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class MessageService {

    private final MessageRepository messageRepository;
    private final ChatParticipantRepository cpRepository;
    private final ProfileRepository profileRepository;
    private final SearchService searchService;
    private final MessageSeenService msService;

    public MessageService(MessageRepository messageRepository,
                          ChatParticipantRepository cpRepository,
                          ProfileRepository profileRepository,
                          SearchService searchService,
                          MessageSeenService msService) {
        this.messageRepository = messageRepository;
        this.cpRepository = cpRepository;
        this.profileRepository = profileRepository;
        this.searchService = searchService;
        this.msService = msService;
    }

    public boolean sendMessage(Long sender, Long receiver, String text, Long replyMessage) {
        Message message = new Message();
        Profile user = getProfile(sender);
        message.setSender(user);
        Profile destination = getProfile(receiver);
        message.setReceiver(destination);
        message.setText(text);
        message.setTime(LocalDateTime.now());
        message.setViewCount(0);
        if (replyMessage != null) {
            Optional<Message> optionalMessage = messageRepository.findById(replyMessage);
            optionalMessage.ifPresent(message::setReplyMessage);
        }
        messageRepository.save(message);
        searchService.addMessage(message);
        if (doesNotChatParticipantExist(user, destination)) createChatParticipant(user, destination);
        msService.addMessageView(sender, message.getMessageID());
        return true;
    }

    private boolean doesNotChatParticipantExist(Profile user, Profile destination) {
        ProfilePareId cpID = new ProfilePareId(user, destination);
        Optional<ChatParticipant> participant = cpRepository.findById(cpID);
        return participant.isEmpty();
    }

    private void createChatParticipant(Profile user, Profile destination) {
        ChatParticipant chatParticipant1 = new ChatParticipant(user, destination, false);
        cpRepository.save(chatParticipant1);
        if (destination.getType() == ChatType.USER) {
            ChatParticipant chatParticipant2 = new ChatParticipant(destination, user, false);
            cpRepository.save(chatParticipant2);
        }
    }

    public List<Message> getMessages(Long chatID, Long userID,
                                     Long messageID, int direction) {
        Optional<Profile> userOptional = profileRepository.findById(userID);
        if (userOptional.isEmpty()) throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        Profile user = userOptional.get();
        Optional<Profile> receiverOptional = profileRepository.findById(chatID);
        if (receiverOptional.isEmpty()) throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        Profile receiver = receiverOptional.get();
        int limit = 3;
        Pageable pageable = PageRequest.of(0, limit, Sort.by("message_id").descending());
        if (messageID == 0)
            if (receiver.getType() == ChatType.USER)
                return messageRepository.findPVTopNMessages(user, receiver, limit);
            else
                return messageRepository.findTopNByReceiverAndMessageIDOrderByTimeDesc
                    (receiver, messageID, pageable, limit);
        if (direction == 0)
            if (receiver.getType() == ChatType.USER)
                return messageRepository.findPVUpMessages(user, receiver, messageID, limit);
            else
                return messageRepository.findByReceiverAndMessageIDLessThanOrderByTimeDesc
                        (receiver, messageID, pageable);
        else
            if (receiver.getType() == ChatType.USER)
                return messageRepository.findPVDownMessages(user, receiver, messageID, limit);
            else
                return messageRepository.findByReceiverAndMessageIDGreaterThanOrderByTimeDesc
                    (receiver, messageID, pageable);

    }

    public boolean editMessage(Long userId, Long messageId, String newMessage) {
        Optional<Message> optionalMessage = messageRepository.findById(messageId);
        if (optionalMessage.isEmpty()) return false;
        Message message = optionalMessage.get();
        if (!message.getSender().getProfileID().equals(userId)) return false;
        message.setText(newMessage);
        message.setEdited(true);
        messageRepository.save(message);
        return true;
    }

    public boolean deleteMessage(Long userId, Long messageId) {
        Optional<Message> optionalMessage = messageRepository.findById(messageId);
        if (optionalMessage.isEmpty()) return false;
        Message message = optionalMessage.get();
        if (!message.getSender().getProfileID().equals(userId)) return false;
        messageRepository.deleteById(messageId);
        return true;
    }

    private Profile getProfile(Long profileId) {
        Optional<Profile> optionalProfile = profileRepository.findById(profileId);
        if (optionalProfile.isEmpty()) throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        return optionalProfile.get();
    }
}
