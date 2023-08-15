package com.mohaymen.service;

import com.mohaymen.model.entity.ChatParticipant;
import com.mohaymen.model.entity.Message;
import com.mohaymen.model.entity.MessageSeen;
import com.mohaymen.model.entity.Profile;
import com.mohaymen.model.json_item.ChatDisplay;
import com.mohaymen.model.json_item.ChatListInfo;
import com.mohaymen.model.supplies.ChatType;
import com.mohaymen.model.supplies.ProfilePareId;
import com.mohaymen.repository.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import java.util.*;

@Service
public class ChatService {

    private final ChatParticipantRepository cpRepository;
    private final ProfileRepository profileRepository;
    private final ContactRepository contactRepository;
    private final MessageRepository messageRepository;
    private final MessageSeenRepository msRepository;
    private final AccessService accessService;
    private final ServerService serverService;
    private final LogService logger;

    public ChatService(ChatParticipantRepository cpRepository,
                       ProfileRepository profileRepository,
                       ContactRepository contactRepository,
                       MessageRepository messageRepository,
                       MessageSeenRepository msRepository,
                       AccessService accessService,
                       ServerService serverService,
                       LogService logger) {
        this.cpRepository = cpRepository;
        this.profileRepository = profileRepository;
        this.contactRepository = contactRepository;
        this.messageRepository = messageRepository;
        this.msRepository = msRepository;
        this.accessService = accessService;
        this.serverService = serverService;
        this.logger = logger;
        logger.setLogger(ChatService.class.getName());
    }

    public ChatListInfo getChats(Long userId, int limit) {
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
        try {
            chats.sort(Comparator.comparing(x -> x.getLastMessage().getMessageID()));
            Collections.reverse(chats);
        }
        catch (Exception e) {
            logger.info("Cannot sort chats for user with id: " + userId);
        }
        if (chats.size() > limit)
            return new ChatListInfo(chats.subList(0, limit), false);
        else return new ChatListInfo(chats, true);
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
            return messageRepository.findPVTopNMessages(user, profile, 1).get(0);
        else
            return messageRepository.findTopByReceiverOrderByMessageIDDesc(profile);
    }

    private long getLastMessageId(Profile user, Profile profile) {
        ProfilePareId profilePareId = new ProfilePareId(user, profile);
        Optional<MessageSeen> messageSeenOptional = msRepository.findById(profilePareId);
        if (messageSeenOptional.isEmpty()) return 0;
        else return messageSeenOptional.get().getLastMessageSeenId();
    }

    public void deleteChannelOrGroupByAdmin(Long id, Long channelOrGroupId) throws Exception {
        Profile channelOrGroup = profileRepository.findById(channelOrGroupId).get();
        Profile admin = profileRepository.findById(id).get();
        if(channelOrGroup.getType() == ChatType.USER)
            throw new Exception("invalid");
        Optional<ChatParticipant> chatParticipant = cpRepository.findById(new ProfilePareId(admin, channelOrGroup));
        if(chatParticipant.isEmpty() || !chatParticipant.get().isAdmin())
            throw new Exception("You have not permission to delete this");
        accessService.deleteProfile(channelOrGroup);
    }

    public void createChat(Long userId, String name, ChatType type,
                           String bio, List<Long> members) {
        if (!type.equals(ChatType.USER)) throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        Profile chat = new Profile();
        chat.setProfileName(name);
        chat.setType(type);
        chat.setBiography(bio);
        chat.setMemberCount(1);
        chat.setHandle(createRandomHandle(type));
        chat.setDefaultProfileColor(AccessService.generateColor(chat.getHandle()));
        profileRepository.save(chat);
        cpRepository.save(new ChatParticipant(getProfile(userId), chat, true));
        for (Number memberId : members) addChatParticipant(memberId.longValue(), chat);
        serverService.sendMessage(type.name().toLowerCase() + " created", chat);
    }

    private String createRandomHandle(ChatType type) {
        UUID uuid = UUID.randomUUID();
        Optional<Profile> profile = profileRepository.findByTypeAndHandle(type, uuid.toString());
        if (profile.isPresent()) return createRandomHandle(type);
        return uuid.toString();
    }

    private Profile addChatParticipant(Long memberId, Profile chat) {
        Profile member = getProfile(memberId);
        if (member.isDeleted()) throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE);;
        Optional<ChatParticipant> chatParticipant = cpRepository.findById(new ProfilePareId(member, chat));
        if (chatParticipant.isEmpty()) {
            cpRepository.save(new ChatParticipant(getProfile(memberId), chat, false));
            chat.setMemberCount(chat.getMemberCount() + 1);
            profileRepository.save(chat);
            return member;
        }
        throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE);
    }

    public void addMember(Long userId, Long chatId, Long memberId) {
        Profile user = getProfile(userId);
        Profile chat = getProfile(chatId);
        Optional<ChatParticipant> cpOptional = cpRepository.findById(new ProfilePareId(user, chat));
        if (cpOptional.isEmpty()) throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        if (!cpOptional.get().isAdmin()) throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE);
        Profile newMember = addChatParticipant(memberId, chat);
        if (chat.getType().equals(ChatType.GROUP))
            serverService.sendMessage(newMember.getProfileName() + " joined the group", chat);
    }

}
