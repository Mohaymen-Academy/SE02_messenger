package com.mohaymen.service;

import com.mohaymen.model.entity.*;
import com.mohaymen.model.json_item.MessageDisplay;
import com.mohaymen.model.json_item.ReplyMessageInfo;
import com.mohaymen.model.supplies.ChatType;
import com.mohaymen.model.supplies.ProfilePareId;
import com.mohaymen.model.supplies.UpdateType;
import com.mohaymen.repository.*;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class MessageService {

    private final MessageRepository messageRepository;
    private final ChatParticipantRepository cpRepository;
    private final ProfileRepository profileRepository;
    private final MessageSeenRepository msRepository;
    private final SearchService searchService;
    private final MessageSeenService msService;
    private final BlockRepository blockRepository;
    private final UpdateRepository updateRepository;
    private final ServerService serverService;

    public MessageService(MessageRepository messageRepository,
                          ChatParticipantRepository cpRepository,
                          ProfileRepository profileRepository,
                          SearchService searchService,
                          MessageSeenService msService,
                          MessageSeenRepository msRepository,
                          BlockRepository blockRepository,
                          UpdateRepository updateRepository,
                          ServerService serverService) {
        this.messageRepository = messageRepository;
        this.cpRepository = cpRepository;
        this.profileRepository = profileRepository;
        this.searchService = searchService;
        this.msService = msService;
        this.msRepository = msRepository;
        this.blockRepository = blockRepository;
        this.updateRepository = updateRepository;
        this.serverService = serverService;
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
    }

    private boolean doesNotChatParticipantExist(Profile user, Profile destination) {
        ProfilePareId cpID = new ProfilePareId(user, destination);
        Optional<ChatParticipant> participant = cpRepository.findById(cpID);
        return participant.isEmpty();
    }

    public void createChatParticipant(Profile user, Profile destination) {
        String id;
        if (destination.getType() != ChatType.USER) id = destination.getHandle();
        else id = createRandomId();
        ChatParticipant chatParticipant = new ChatParticipant(user, destination, id, false);
        cpRepository.save(chatParticipant);
        if (destination.getType() == ChatType.USER && cpRepository.findById(new ProfilePareId(destination, user)).isEmpty()) {
            ChatParticipant chatParticipant2 = new ChatParticipant(destination, user, id, false);
            cpRepository.save(chatParticipant2);
        }
    }

    private String createRandomId() {
        UUID uuid = UUID.randomUUID();
        List<ChatParticipant> chatParticipants = cpRepository.findByChatId(uuid.toString());
        if (!chatParticipants.isEmpty()) return createRandomId();
        return uuid.toString();
    }


    public MessageDisplay getMessages(Long chatID, Long userID, Long messageID, int direction) throws Exception {
        // get profiles
        Profile user = getProfile(userID);
        Profile receiver = getProfile(chatID);

        // limit for query
        int limit = 20;
        Pageable pageable = PageRequest.of(0, limit + 1);

        // find target message id
        if (messageID == 0) {
            Optional<MessageSeen> messageSeenOptional = msRepository.findById(new ProfilePareId(user, receiver));
            if (messageSeenOptional.isPresent()) messageID = messageSeenOptional.get().getLastMessageSeenId();
            if (receiver.getType() == ChatType.CHANNEL) {
                Optional<ChatParticipant> cpOptional = cpRepository.findById(new ProfilePareId(user, receiver));
                if (cpOptional.isEmpty())
                    messageID = messageRepository.findTopByReceiverOrderByMessageIDDesc(receiver).getMessageID();
            }
        }

        // get messages
        List<Message> upMessages = new ArrayList<>();
        List<Message> downMessages = new ArrayList<>();
        if (receiver.getType() == ChatType.USER) {
            if (direction == 0 || direction == 1)
                upMessages = messageRepository.findPVUpMessages(user, receiver, messageID, limit + 1);
            if (direction == 0 || direction == 2)
                downMessages = messageRepository.findPVDownMessages(user, receiver, messageID, limit + 1);
        } else {
            if (direction == 0 || direction == 1)
                upMessages = messageRepository.findByReceiverAndMessageIDLessThanOrderByTimeDesc
                        (receiver, messageID, pageable);
            if (direction == 0 || direction == 2)
                downMessages = messageRepository.findByReceiverAndMessageIDGreaterThanOrderByTimeDesc
                        (receiver, messageID, pageable);
        }
        boolean isUpFinished = upMessages.size() <= limit;
        boolean isDownFinished = downMessages.size() <= limit;

        // remove the last message if size is (limit + 1)
        upMessages = isUpFinished ? upMessages : upMessages.subList(0, limit);
        downMessages = isDownFinished ? downMessages : downMessages.subList(0, limit);

        // get the message itself
        Message message = null;
        if (direction == 0) {
            Optional<Message> messageOptional = messageRepository.findById(messageID);
            if (messageOptional.isPresent())
                message = messageOptional.get();
        }

        // create and return MessageDisplay
        MessageDisplay messageDisplay = new MessageDisplay(
                upMessages,
                downMessages,
                message,
                isDownFinished,
                isUpFinished,
                serverService.getServer());
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
        if (!message.getSender().getProfileID().equals(userId))
            throw new Exception("you cannot edit this message!");
        message.setText(newMessage);
        message.setTextStyle(textStyle);
        message.setEdited(true);
        messageRepository.save(message);
        searchService.updateMessage(message);
        setNewUpdate(message, UpdateType.EDIT);
    }

    public void deleteMessage(Long userId, Long messageId) throws Exception {
        Optional<Message> optionalMessage = messageRepository.findById(messageId);
        if (optionalMessage.isEmpty()) throw new Exception("message not found!");
        Message message = optionalMessage.get();
        Profile chat = message.getReceiver();
        ChatParticipant chatParticipant = cpRepository.findById(new ProfilePareId(message.getSender(), chat)).get();
        if (!message.getSender().getProfileID().equals(userId) && !chatParticipant.isAdmin())
            throw new Exception("You cannot delete this message.");
//        List<ChatParticipant> chtByDestMsg = cpRepository.findByPinnedMessageAndDestination(message, chat);
//        if the message was a pin message for other chats we should make the pin message to null
        cpRepository.updateMessageIdByProfileDestinationAndMessageId(chat,message);
        setNewUpdate(message, UpdateType.DELETE);
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

    private void setNewUpdate(Message message, UpdateType type) {
        Optional<ChatParticipant> cpOptional = cpRepository.findById
                (new ProfilePareId(message.getSender(), message.getReceiver()));
        if (cpOptional.isPresent()) {
            String chatId = cpOptional.get().getChatId();
            Update update = new Update(chatId, type, message.getMessageID());
            updateRepository.save(update);
            List<Message> messages = messageRepository.findByReplyMessageId(message.getMessageID());
            for (Message m : messages) {
                if (type == UpdateType.DELETE) {
                    m.setReplyMessageId(null);
                    messageRepository.save(m);
                }
                update = new Update(chatId, UpdateType.EDIT, m.getMessageID());
                updateRepository.save(update);
            }
        }

    }

    private Profile getProfile(Long profileId) throws Exception {
        Optional<Profile> optionalProfile = profileRepository.findById(profileId);
        if (optionalProfile.isEmpty()) {
            System.out.println("get profile");
            throw new Exception("User not found!");
        }
        return optionalProfile.get();
    }

    public Message getSingleMessage(Long messageId) throws Exception {
        Optional<Message> msg = messageRepository.findById(messageId);
        if (msg.isEmpty())
            throw new Exception("Message not found!");
        Message message = msg.get();
        setReplyAndForwardMessageInfo(message);
        return message;
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
    @Transactional
    public void setPinMessage(Long userID, Long messageId, boolean pin) throws Exception {
        Message message = checkIsPossible(userID, messageId);
        Profile user = getProfile(userID);
        Profile chat = getProfile(message.getReceiver().getProfileID());
        if (!pin)
            message = null;
        if (chat.getType() == ChatType.USER) {
            ChatParticipant chatParticipant1 = getChatParticipant(user, chat);


            if (chatParticipant1 != null) {
                chatParticipant1.setPinnedMessage(message);
                cpRepository.save(chatParticipant1);
            }

            Block block = getBlockParticipant(chat, user);
            ChatParticipant chatParticipant2 = getChatParticipant(chat, user);
            if (chatParticipant2 != null && block == null) {
                chatParticipant2.setPinnedMessage(message);
                cpRepository.save(chatParticipant2);
            }

        } else {
            List<ChatParticipant> destinations = cpRepository.findByDestination(chat);
            for (ChatParticipant p : destinations) {
                p.setPinnedMessage(message);
                cpRepository.save(p);
            }
        }
    }


    public ChatParticipant getChatParticipant(Profile user, Profile chat) {
        Optional<ChatParticipant> chatParticipant = cpRepository.findById(new ProfilePareId(user, chat));
        return chatParticipant.orElse(null);
    }

    public Block getBlockParticipant(Profile user, Profile chat) {
        Optional<Block> blockParticipant = blockRepository.findById(new ProfilePareId(user, chat));
        return blockParticipant.orElse(null);
    }

    public void setLastUpdate(Long chatId, Long userId, Long updateId) throws Exception {
        Profile user = getProfile(userId);
        Profile chat = getProfile(chatId);
        Optional<ChatParticipant> cpOptional = cpRepository.findById(new ProfilePareId(user, chat));
        if (cpOptional.isPresent()) {
            ChatParticipant chatParticipant = cpOptional.get();
            chatParticipant.setLastUpdate(updateId);
            cpRepository.save(chatParticipant);
        }
    }

    public void forwardMessage(Long sender, Long receiver, Long forwardMessage) throws Exception {
        Message message = getMessage(forwardMessage);
        forwardMessage = message.getForwardMessageId() == null ? forwardMessage : message.getForwardMessageId();
        sendMessage(sender, receiver, message.getText(),
                message.getTextStyle(), null, forwardMessage, message.getMedia());
    }


    public Message getPinMessage(Long userId, Long chatId) throws Exception {
        Profile user = getProfile(userId);
        Profile chat = getProfile(chatId);
        ChatParticipant chatParticipant = getChatParticipant(user, chat);
        return chatParticipant.getPinnedMessage();
    }

    public List<Message> getMediaOfChat(Long userId, Long profileId, String mediaType) {
        Profile user = profileRepository.findById(userId).get();
        Profile chat = profileRepository.findById(profileId).get();
        mediaType = mediaType + "%";
        if (chat.getType() == ChatType.USER)
            return messageRepository.findMediaOfPVChat(user, chat, mediaType);
        else
            return messageRepository.findMediaOfChannelOrGroup(chat, mediaType);
    }
}

