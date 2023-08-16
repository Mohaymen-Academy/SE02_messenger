package com.mohaymen.service;

import com.mohaymen.model.entity.*;
import com.mohaymen.model.json_item.MessageDisplay;
import com.mohaymen.model.supplies.ChatType;
import com.mohaymen.model.supplies.ProfilePareId;
import com.mohaymen.repository.ChatParticipantRepository;
import com.mohaymen.repository.MessageRepository;
import com.mohaymen.repository.MessageSeenRepository;
import com.mohaymen.repository.ProfileRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class MessageService {

    private final MessageRepository messageRepository;
    private final ChatParticipantRepository cpRepository;
    private final ProfileRepository profileRepository;
    private final MessageSeenRepository msRepository;
    private final SearchService searchService;
    private final MessageSeenService msService;

    public MessageService(MessageRepository messageRepository,
                          ChatParticipantRepository cpRepository,
                          ProfileRepository profileRepository,
                          SearchService searchService,
                          MessageSeenService msService,
                          MessageSeenRepository msRepository) {
        this.messageRepository = messageRepository;
        this.cpRepository = cpRepository;
        this.profileRepository = profileRepository;
        this.searchService = searchService;
        this.msService = msService;
        this.msRepository = msRepository;
    }

    public boolean sendMessage(Long sender, Long receiver,
                               String text, Long replyMessage,
                               MediaFile mediaFile) throws Exception {
        Message message = new Message();
        Profile user = getProfile(sender);
        message.setSender(user);
        Profile destination = getProfile(receiver);
        message.setReceiver(destination);
        message.setText(text);
        message.setTime(LocalDateTime.now());
        message.setViewCount(0);
        message.setMedia(mediaFile);
        if (replyMessage != null) {
            Optional<Message> optionalMessage = messageRepository.findById(replyMessage);
            optionalMessage.ifPresent(message::setReplyMessage);
        }
        messageRepository.save(message);
        searchService.addMessage(message);
        if (doesNotChatParticipantExist(user, destination)) createChatParticipant(user, destination);
        msService.addMessageView(sender, message.getMessageID());
        setIsUpdatedTrue(user, destination);
        return true;
    }

    private boolean doesNotChatParticipantExist(Profile user, Profile destination) {
        ProfilePareId cpID = new ProfilePareId(user, destination);
        Optional<ChatParticipant> participant = cpRepository.findById(cpID);
        return participant.isEmpty();
    }

    private void createChatParticipant(Profile user, Profile destination) {
        ChatParticipant chatParticipant1 = new ChatParticipant(user, destination, false, false);
        cpRepository.save(chatParticipant1);
        if (destination.getType() == ChatType.USER) {
            ChatParticipant chatParticipant2 = new ChatParticipant(destination, user, false, false);
            cpRepository.save(chatParticipant2);
        }
    }

    public MessageDisplay getMessages(Long chatID, Long userID, Long messageID) throws Exception {
        Profile user = getProfile(userID);
        Profile receiver = getProfile(chatID);
        int limit = 20;
        Pageable pageable = PageRequest.of(0, limit + 1, Sort.by("message_id").descending());
        List<Message> upMessages;
        List<Message> downMessages;
        if (messageID == 0) {
            Optional<MessageSeen> messageSeenOptional = msRepository.findById(new ProfilePareId(user, receiver));
            if (messageSeenOptional.isPresent()) messageID = messageSeenOptional.get().getLastMessageSeenId();
            if (receiver.getType() == ChatType.CHANNEL) {
                Optional<ChatParticipant> cpOptional = cpRepository.findById(new ProfilePareId(user, receiver));
                if (cpOptional.isEmpty())
                    messageID = messageRepository.findTopByReceiverOrderByMessageIDDesc(receiver).getMessageID();
            }
        }
        if (receiver.getType() == ChatType.USER) {
            upMessages = messageRepository.findPVUpMessages(user, receiver, messageID, limit + 1);
            downMessages = messageRepository.findPVDownMessages(user, receiver, messageID, limit + 1);
        }
        else {
            upMessages = messageRepository.findByReceiverAndMessageIDLessThanOrderByTimeDesc
                    (receiver, messageID, pageable);
            downMessages = messageRepository.findByReceiverAndMessageIDGreaterThanOrderByTimeDesc
                    (receiver, messageID, pageable);
        }
        boolean isUpFinished = upMessages.size() <= limit;
        boolean isDownFinished = downMessages.size() <= limit;
        upMessages = isUpFinished ? upMessages : upMessages.subList(0, limit);
        downMessages = isDownFinished ? downMessages : downMessages.subList(0, limit);
        Message message = null;
        Optional<Message> messageOptional = messageRepository.findById(messageID);
        if(messageOptional.isPresent())
            message = messageOptional.get();
        setIsUpdatedFalse(user, receiver);
        return new MessageDisplay(upMessages, downMessages, message, isDownFinished, isUpFinished);
    }

    public boolean editMessage(Long userId, Long messageId, String newMessage) {
        Optional<Message> optionalMessage = messageRepository.findById(messageId);
        if (optionalMessage.isEmpty()) return false;
        Message message = optionalMessage.get();
        if (!message.getSender().getProfileID().equals(userId)) return false;
        message.setText(newMessage);
        message.setEdited(true);
        messageRepository.save(message);
        setIsUpdatedTrue(message.getSender(), message.getReceiver());
        return true;
    }

    public boolean deleteMessage(Long userId, Long messageId) {
        Optional<Message> optionalMessage = messageRepository.findById(messageId);
        if (optionalMessage.isEmpty()) return false;
        Message message = optionalMessage.get();
        Profile chat = message.getReceiver();
        ChatParticipant chatParticipant = cpRepository.findById(new ProfilePareId(message.getSender(), chat)).get();
        if (!message.getSender().getProfileID().equals(userId) && !chatParticipant.isAdmin()) return false;
        messageRepository.deleteById(messageId);
        if (message.getReceiver().getType().equals(ChatType.USER)) {
            List<Message> messages = messageRepository.findPVTopNMessages(message.getSender(), message.getReceiver(), 1);
            if (messages.isEmpty()) {
                cpRepository.deleteById(new ProfilePareId(message.getSender(), message.getReceiver()));
                cpRepository.deleteById(new ProfilePareId(message.getReceiver(), message.getSender()));
                msRepository.deleteById(new ProfilePareId(message.getSender(), message.getReceiver()));
                msRepository.deleteById(new ProfilePareId(message.getReceiver(), message.getSender()));
            }
        }
        setIsUpdatedTrue(message.getSender(), message.getReceiver());
        return true;
    }

    private Profile getProfile(Long profileId) throws Exception {
        Optional<Profile> optionalProfile = profileRepository.findById(profileId);
        if (optionalProfile.isEmpty()) {
            System.out.println("get profile");
            throw new Exception("User not found!");
        }
        return optionalProfile.get();
    }

    private void setIsUpdatedTrue(Profile user, Profile destination) {
        if (destination.getType().equals(ChatType.USER)) {
            Optional<ChatParticipant> cpOptional = cpRepository.findById(new ProfilePareId(destination, user));
            if (cpOptional.isPresent()) {
                ChatParticipant chatParticipant = cpOptional.get();
                chatParticipant.setUpdated(true);
                cpRepository.save(chatParticipant);
            }
        }
        else {
            List<ChatParticipant> chatParticipants = cpRepository.findByDestination(destination);
            for (ChatParticipant cp : chatParticipants) {
                cp.setUpdated(true);
                cpRepository.save(cp);
            }
        }
    }

    private void setIsUpdatedFalse(Profile user, Profile destination) {
        Optional<ChatParticipant> cpOptional = cpRepository.findById(new ProfilePareId(user, destination));
        if (cpOptional.isPresent()) {
            ChatParticipant chatParticipant = cpOptional.get();
            chatParticipant.setUpdated(false);
            cpRepository.save(chatParticipant);
        }
    }
}
