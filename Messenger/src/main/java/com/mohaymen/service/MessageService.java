package com.mohaymen.service;

import com.mohaymen.model.entity.*;
import com.mohaymen.model.json_item.*;
import com.mohaymen.model.supplies.*;
import com.mohaymen.repository.*;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import java.time.Instant;
import java.util.*;

@Service
public class MessageService {

    private final MessageRepository messageRepository;

    private final ChatParticipantRepository cpRepository;

    private final ProfileRepository profileRepository;

    private final MessageSeenRepository msRepository;

    private final BlockRepository blockRepository;

    private final UpdateRepository updateRepository;

    private final SearchService searchService;

    private final MessageSeenService msService;

    private final ChatParticipantService cpService;

    private final UpdateService updateService;

    final int limit = 20;

    public MessageService(MessageRepository messageRepository,
                          ChatParticipantRepository cpRepository,
                          ProfileRepository profileRepository,
                          SearchService searchService,
                          MessageSeenService msService,
                          MessageSeenRepository msRepository,
                          BlockRepository blockRepository,
                          UpdateRepository updateRepository,
                          ChatParticipantService cpService,
                          UpdateService updateService) {
        this.messageRepository = messageRepository;
        this.cpRepository = cpRepository;
        this.profileRepository = profileRepository;
        this.searchService = searchService;
        this.msService = msService;
        this.msRepository = msRepository;
        this.blockRepository = blockRepository;
        this.updateRepository = updateRepository;
        this.cpService = cpService;
        this.updateService = updateService;
    }

    private void checkIfBlocked(Profile user, Profile destination) throws Exception {
        Block block = getBlockParticipant(user, destination);
        if (block != null)
            throw new Exception("You have blocked this user,you can not send him/her a message");
        Block block2 = getBlockParticipant(destination, user);
        if (block2 != null)
            throw new Exception("This user has blocked you,you can not send him/her a message");
    }

    private Message createMessage(Profile user, Profile destination, String text,
                                  String textStyle, MediaFile mediaFile, Long replyMessage,
                                  Long forwardMessage) {
        return Message.builder()
                .sender(user)
                .receiver(destination)
                .text(text)
                .textStyle(textStyle)
                .time(Instant.now())
                .viewCount(user.getProfileID().equals(destination.getProfileID()) ? 1 : 0)
                .media(mediaFile)
                .replyMessageId(replyMessage)
                .forwardMessageId(forwardMessage)
                .build();
    }

    public Message sendMessage(Long sender, Long receiver,
                               String text, String textStyle, Long replyMessage,
                               Long forwardMessage, MediaFile mediaFile) throws Exception {
        Profile user = getProfile(sender);
        Profile destination = getProfile(receiver);

        checkIfBlocked(user, destination);

        Message message = createMessage(user, destination, text, textStyle, mediaFile, replyMessage, forwardMessage);
        messageRepository.save(message);
        if (text != null)
            searchService.addMessage(message);
        cpService.createChatParticipant(user, destination, false);
        msService.addMessageView(sender, message.getMessageID());
        setAdditionalMessageInfo(message);
        return message;
    }

    private Long findTargetMessageId(Long messageId, Profile user, Profile receiver) {
        if (messageId == 0) {
            updateLastUpdate(user, receiver);
            Optional<MessageSeen> messageSeenOptional = msRepository.findById(new ProfilePareId(user, receiver));
            if (messageSeenOptional.isPresent())
                messageId = messageSeenOptional.get().getLastMessageSeenId();
            if (receiver.getType() == ChatType.CHANNEL) {
                ChatParticipant cp = getChatParticipant(user, receiver);
                if (cp == null)
                    messageId = messageRepository.findTopByReceiverOrderByMessageIDDesc(receiver).getMessageID();
            }
        }
        return messageId;
    }

    private List<Message> setUpMessages(Profile receiver, Profile user, Long messageId, Pageable pageable) {
        if (receiver.getType() == ChatType.USER)
            return messageRepository.findPVUpMessages(user, receiver, messageId, limit + 1);
        else
            return messageRepository.findByReceiverAndMessageIDLessThanOrderByTimeDesc
                    (receiver, messageId, pageable);
    }

    private List<Message> setDownMessages(Profile receiver, Profile user, Long messageId, Pageable pageable) {
        if (receiver.getType() == ChatType.USER)
            return messageRepository.findPVDownMessages(user, receiver, messageId, limit + 1);
        else
            return messageRepository.findByReceiverAndMessageIDGreaterThanOrderByTimeDesc
                    (receiver, messageId, pageable);
    }

    private MessageDisplay createMessageDisplay(List<Message> upMessages, List<Message> downMessages,
                                                Message message, boolean isDownFinished, boolean isUpFinished) {
        MessageDisplay messageDisplay = new MessageDisplay(
                upMessages,
                downMessages,
                message,
                isDownFinished,
                isUpFinished,
                ServerService.getServer());
        messageDisplay.getMessages().forEach(this::setAdditionalMessageInfo);
        return messageDisplay;
    }

    public MessageDisplay getMessages(Long chatID, Long userID, Long messageId, int direction) throws Exception {
        Profile user = getProfile(userID);
        Profile receiver = getProfile(chatID);
        Pageable pageable = PageRequest.of(0, limit + 1);
        messageId = findTargetMessageId(messageId, user, receiver);
        List<Message> upMessages = new ArrayList<>();
        List<Message> downMessages = new ArrayList<>();
        //direction 0 means no direction , 2 means down , 1 means up
        if (direction == 0 || direction == 1)
            upMessages = setUpMessages(receiver, user, messageId, pageable);
        if (direction == 0 || direction == 2)
            downMessages = setDownMessages(receiver, user, messageId, pageable);
        boolean isUpFinished = upMessages.size() <= limit;
        boolean isDownFinished = downMessages.size() <= limit;
        // remove the last message if size is (limit + 1)
        upMessages = isUpFinished ? upMessages : upMessages.subList(0, limit);
        downMessages = isDownFinished ? downMessages : downMessages.subList(0, limit);
        Message message = null;
        if (direction == 0) {
            Optional<Message> messageOptional = messageRepository.findById(messageId);
            if (messageOptional.isPresent())
                message = messageOptional.get();
        }
        return createMessageDisplay(upMessages, downMessages, message, isDownFinished, isUpFinished);
    }

    private void updateLastUpdate(Profile user, Profile receiver) {
        ChatParticipant cp = getChatParticipant(user, receiver);
        if (cp != null) {
            String chatId = cp.getChatId();
            Update update = updateRepository
                    .findTopByChatIdOrderByIdDesc(chatId);
            Long updateId = update != null ? update.getId() : 0;
            cp.setLastUpdate(updateId);
            cpRepository.save(cp);
        }
    }

    private void setReplyMessageInfo(Message message) {
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
    }

    private void setForwardMessageInfo(Message message) {
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

    private void setAdditionalMessageInfo(Message message) {
        setReplyMessageInfo(message);
        setForwardMessageInfo(message);
    }

    public void editMessage(Long userId, Long messageId, String newMessage, String textStyle) throws Exception {
        Optional<Message> optionalMessage = messageRepository.findById(messageId);
        if (optionalMessage.isEmpty()) throw new Exception("message not found");
        Message message = optionalMessage.get();
        if (!message.getSender().getProfileID().equals(userId))
            throw new Exception("you cannot edit this message!");
        message.setText(newMessage);
        message.setTextStyle(textStyle);
        message.setEdited(true);
        messageRepository.save(message);
        searchService.updateMessage(message);
        updateService.setNewUpdate(message, UpdateType.EDIT);
    }

    @Transactional
    public void deleteMessage(Long userId, Long messageId) throws Exception {
        Optional<Message> optionalMessage = messageRepository.findById(messageId);
        if (optionalMessage.isEmpty()) throw new Exception("message not found!");
        Message message = optionalMessage.get();
        Profile chat = message.getReceiver();
        ChatParticipant chatParticipant = getChatParticipant(message.getSender(), chat);
        if (!message.getSender().getProfileID().equals(userId) && !chatParticipant.isAdmin())
            throw new Exception("You cannot delete this message.");
        cpRepository.updateMessageIdByProfileDestinationAndMessageId(chat, message);
        updateService.setNewUpdate(message, UpdateType.DELETE);
        messageRepository.deleteById(messageId);
        searchService.deleteMessage(message);
        if (message.getReceiver().getType().equals(ChatType.USER)) {
            List<Message> messages = messageRepository.findPVTopNMessages(message.getSender(), message.getReceiver(), 1);
            if (messages.isEmpty()) {
                cpRepository.deleteById(new ProfilePareId(message.getSender(), message.getReceiver()));
                cpRepository.deleteById(new ProfilePareId(message.getReceiver(), message.getSender()));
                msRepository.deleteById(new ProfilePareId(message.getSender(), message.getReceiver()));
                msRepository.deleteById(new ProfilePareId(message.getReceiver(), message.getSender()));
            }
        }
    }

    private Profile getProfile(Long profileId) throws Exception {
        Optional<Profile> optionalProfile = profileRepository.findById(profileId);
        if (optionalProfile.isEmpty())
            throw new Exception("User not found!");
        return optionalProfile.get();
    }

    public Message getSingleMessage(Long messageId) throws Exception {
        Optional<Message> msg = messageRepository.findById(messageId);
        if (msg.isEmpty())
            throw new Exception("Message not found!");
        Message message = msg.get();
        setAdditionalMessageInfo(message);
        return message;
    }

    private Message getMessage(Long messageId) throws Exception {
        Optional<Message> msg = messageRepository.findById(messageId);
        if (msg.isEmpty())
            throw new Exception("Message doesn't exist");
        return msg.get();
    }

    private ChatParticipant getChatParticipant(Profile user, Profile chat) {
        Optional<ChatParticipant> chatParticipant = cpRepository.findById(new ProfilePareId(user, chat));
        return chatParticipant.orElse(null);
    }

    private Block getBlockParticipant(Profile user, Profile chat) {
        Optional<Block> blockParticipant = blockRepository.findById(new ProfilePareId(user, chat));
        return blockParticipant.orElse(null);
    }

    public void setLastUpdate(Long chatId, Long userId, Long updateId) throws Exception {
        Profile user = getProfile(userId);
        Profile chat = getProfile(chatId);
        ChatParticipant cp = getChatParticipant(user, chat);
        if (cp != null) {
            cp.setLastUpdate(updateId);
            cpRepository.save(cp);
        }
    }

    public Message forwardMessage(Long sender, Long receiver, Long forwardMessage) throws Exception {
        Message message = getMessage(forwardMessage);
        forwardMessage = message.getForwardMessageId() == null ? forwardMessage : message.getForwardMessageId();
        Message m = sendMessage(sender, receiver, message.getText(),
                message.getTextStyle(), null, forwardMessage, message.getMedia());
        setAdditionalMessageInfo(m);
        return m;
    }

    public MediaDisplay getMediaOfChat(Long userId, Long profileId) {
        Profile user = profileRepository.findById(userId).get();
        Profile chat = profileRepository.findById(profileId).get();
        if (chat.getType() == ChatType.USER)
            return new MediaDisplay(messageRepository.findAudioOrMediaOfPVChat(user, chat, "image%","video%"),
                    messageRepository.findAudioOrMediaOfPVChat(user, chat, "ogg%", "ogg%"),
                    messageRepository.findAudioOrMediaOfPVChat(user, chat, "mp3%", "mp3%"),
                    messageRepository.findFilesOfPVChat(user, chat));
        else
            return new MediaDisplay(messageRepository.findAudioOrMediaOfChannelOrGroup(chat, "image%", "video%"),
                    messageRepository.findAudioOrMediaOfChannelOrGroup(chat, "ogg%", "ogg%"),
                    messageRepository.findAudioOrMediaOfChannelOrGroup(chat, "mp3%", "mp3%"),
                    messageRepository.findFilesOfChannelOrGroup(chat));
    }
}