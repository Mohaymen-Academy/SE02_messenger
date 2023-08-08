package com.mohaymen.service;

import com.mohaymen.model.*;
import com.mohaymen.repository.ChatParticipantRepository;
import com.mohaymen.repository.MessageRepository;
import com.mohaymen.repository.ProfileRepository;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class MessageService {

    private final MessageRepository messageRepository;
    private final ChatParticipantRepository cpRepository;
    private final ProfileRepository profileRepository;

    public MessageService(MessageRepository messageRepository,
                          ChatParticipantRepository cpRepository,
                          ProfileRepository profileRepository) {
        this.messageRepository = messageRepository;
        this.cpRepository = cpRepository;
        this.profileRepository = profileRepository;
    }

    public boolean sendMessage(Long sender, Long receiver, String text) {
        Message message = new Message();
        Optional<Profile> userOptional = profileRepository.findById(sender);
        if (userOptional.isEmpty()) return false;
        Profile user = userOptional.get();
        message.setSender(user);
        Optional<Profile> destinationOptional = profileRepository.findById(receiver);
        if (destinationOptional.isEmpty()) return false;
        Profile destination = destinationOptional.get();
        message.setReceiver(destination);
        message.setText(text);
        message.setTime(LocalDateTime.now());
        message.setViewCount(0);
        messageRepository.save(message);
        if (!doesChatExist(user, destination)) createChatParticipant(user, destination);
        return true;
    }

    private boolean doesChatExist(Profile user, Profile destination) {
        ChatParticipantID cpID = new ChatParticipantID(user, destination);
        Optional<ChatParticipant> participant = cpRepository.findById(cpID);
        return participant.isPresent();
    }

    private void createChatParticipant(Profile user, Profile destination) {
        ChatParticipant chatParticipant1 = new ChatParticipant(user, destination, false);
        cpRepository.save(chatParticipant1);
        if (destination.getType() == ChatType.USER) {
            ChatParticipant chatParticipant2 = new ChatParticipant(destination, user, false);
            cpRepository.save(chatParticipant2);
        }
    }
}
