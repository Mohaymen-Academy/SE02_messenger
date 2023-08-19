package com.mohaymen.service;

import com.mohaymen.model.entity.*;
import com.mohaymen.model.json_item.MessageDisplay;
import com.mohaymen.model.json_item.ReplyMessageInfo;
import com.mohaymen.model.supplies.ChatType;
import com.mohaymen.model.supplies.ProfilePareId;
import com.mohaymen.repository.*;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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
    private final BlockRepository blockRepository;

    public MessageService(MessageRepository messageRepository,
                          ChatParticipantRepository cpRepository,
                          ProfileRepository profileRepository,
                          SearchService searchService,
                          MessageSeenService msService,
                          MessageSeenRepository msRepository, BlockRepository blockRepository) {
        this.messageRepository = messageRepository;
        this.cpRepository = cpRepository;
        this.profileRepository = profileRepository;
        this.searchService = searchService;
        this.msService = msService;
        this.msRepository = msRepository;

        this.blockRepository = blockRepository;
    }

    public void sendMessage(Long sender, Long receiver,
                            String text, String textStyle, Long replyMessage,
                            Long forwardMessage, MediaFile mediaFile) throws Exception {
        Message message = new Message();
        Profile user = getProfile(sender);
        message.setSender(user);
        Profile destination = getProfile(receiver);
        Optional<Block> blockOptional = blockRepository.findById(new ProfilePareId(user, destination));
        if (blockOptional.isPresent())
            throw new Exception("You have blocked this user,you can not send him/her a message");
        Optional<Block> blockOptional2 = blockRepository.findById(new ProfilePareId(destination, user));
        if (blockOptional2.isPresent())
            throw new Exception("This user has blocked you,you can not send him/her a message");

        message.setReceiver(destination);
        message.setText(text);
        message.setTextStyle(textStyle);
        message.setTime(LocalDateTime.now());
        message.setViewCount(0);
        message.setMedia(mediaFile);
        message.setReplyMessageId(replyMessage);
        message.setForwardMessageId(forwardMessage);
        messageRepository.save(message);
        if (message.getText() != null)
            searchService.addMessage(message);
        if (doesNotChatParticipantExist(user, destination)) createChatParticipant(user, destination);
        msService.addMessageView(sender, message.getMessageID());
        setIsUpdatedTrue(user, destination);
    }

    private boolean doesNotChatParticipantExist(Profile user, Profile destination) {
        ProfilePareId cpID = new ProfilePareId(user, destination);
        Optional<ChatParticipant> participant = cpRepository.findById(cpID);
        return participant.isEmpty();
    }

    private void createChatParticipant(Profile user, Profile destination) {
        ChatParticipant chatParticipant1 = new ChatParticipant(user, destination, false);
        cpRepository.save(chatParticipant1);
        if (destination.getType() == ChatType.USER && cpRepository.findById(new ProfilePareId(destination, user)).isEmpty()) {
            ChatParticipant chatParticipant2 = new ChatParticipant(destination, user, false);
            cpRepository.save(chatParticipant2);
        }
    }

    public MessageDisplay getMessages(Long chatID, Long userID, Long messageID) throws Exception {
        Profile user = getProfile(userID);
        Profile receiver = getProfile(chatID);
        int limit = 20;
        Pageable pageable = PageRequest.of(0, limit + 1);
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
        } else {
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
        if (messageOptional.isPresent())
            message = messageOptional.get();
        setIsUpdatedFalse(user, receiver);
        MessageDisplay messageDisplay = new MessageDisplay(upMessages, downMessages, message, isDownFinished, isUpFinished);
        messageDisplay.getMessages().forEach(this::setReplyAndForwardMessageInfo);
        return messageDisplay;
    }

    private void setReplyAndForwardMessageInfo(Message message) {
        if (message.getReplyMessageId() != null) {
            Optional<Message> messageOptional = messageRepository.findById(message.getReplyMessageId());
            if (messageOptional.isPresent()) {
                Message repliedMessage = messageOptional.get();
                message.setReplyMessageInfo(new ReplyMessageInfo(repliedMessage.getMessageID(),
                        repliedMessage.getSender().getProfileName(),
                        repliedMessage.getText(),
                        repliedMessage.getMedia() != null ?
                                repliedMessage.getMedia().getPreLoadingContent() : null));
            }
        }
        if (message.getForwardMessageId() != null) {
            Optional<Message> messageOptional = messageRepository.findById(message.getForwardMessageId());
            if (messageOptional.isPresent()) {
                Message forwardedMessage = messageOptional.get();
                message.setForwardMessageSender
                        (forwardedMessage.getReceiver().getType() == ChatType.CHANNEL
                                ? forwardedMessage.getReceiver().getProfileName()
                                : forwardedMessage.getSender().getProfileName());
            }
        }
    }

    public void editMessage(Long userId, Long messageId, String newMessage, String textStyle) throws Exception {
        Optional<Message> optionalMessage = messageRepository.findById(messageId);
        if (optionalMessage.isEmpty()) throw new Exception("message not found");
        Message message = optionalMessage.get();
        if (!message.getSender().getProfileID().equals(userId)) throw new Exception("you cannot edit this message!");
        message.setText(newMessage);
        message.setTextStyle(textStyle);
        message.setEdited(true);
        messageRepository.save(message);
        setIsUpdatedTrue(message.getSender(), message.getReceiver());

    }

    public void deleteMessage(Long userId, Long messageId) throws Exception {
        Optional<Message> optionalMessage = messageRepository.findById(messageId);
        if (optionalMessage.isEmpty()) throw new Exception("message not found!");
        Message message = optionalMessage.get();
        Profile chat = message.getReceiver();
        ChatParticipant chatParticipant = cpRepository.findById(new ProfilePareId(message.getSender(), chat)).get();
        if (!message.getSender().getProfileID().equals(userId) && !chatParticipant.isAdmin())
            throw new Exception("You cannot delete this message.");
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
            cpOptional = cpRepository.findById(new ProfilePareId(user, destination));
            if (cpOptional.isPresent()) {
                ChatParticipant chatParticipant = cpOptional.get();
                chatParticipant.setUpdated(true);
                cpRepository.save(chatParticipant);
            }
        } else {
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

    private Message getMessage(Long messageId) throws Exception {
        Optional<Message> msg = messageRepository.findById(messageId);
        if (msg.isEmpty())
            throw new Exception("Message doesn't exist");
        return msg.get();
    }

    private Message checkIsPossible(Long userID, Long messageId) throws Exception {
        Message message = getMessage(messageId);
        Profile chat = message.getReceiver();
        Profile user = getProfile(userID);
        if (chat.getType() != ChatType.USER) {
            ProfilePareId profilePareId = new ProfilePareId(user, chat);
            Optional<ChatParticipant> profilePareIdOptional = cpRepository.findById(profilePareId);
            if (profilePareIdOptional.isEmpty())
                throw new Exception("this user is not a member of this chat");
            if (!profilePareIdOptional.get().isAdmin())
                throw new Exception("this user is not the admin of the chat");
        }
        return message;
    }


    //todo is pin message available in a closed group or channel?
    //pin a message is available for a deleted account in telegram!
    //check when block user handled
    //can someone pin a message without seeing it?is it handled in front?
    //how does pin work?
    //an admin can pin a message for every one in chat
    //in pvs both side pin for each other,no option for pinning for yourself yet
    public void pinMessage(Long userID, Long messageId) throws Exception {
        Message message = checkIsPossible(userID, messageId);
        message.setPinned(true);
        messageRepository.save(message);

    }

    public void unpinMessage(Long userID, Long messageId) throws Exception {
        Message message = checkIsPossible(userID, messageId);
        message.setPinned(false);
        messageRepository.save(message);
    }

//    public MessageDisplay getPinMessages(Long chatID) throws Exception {
//    }
}
