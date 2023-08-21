package com.mohaymen.service;

import com.mohaymen.full_text_search.ChannelSearch;
import com.mohaymen.full_text_search.MessageSearch;
import com.mohaymen.full_text_search.UserSearch;
import com.mohaymen.model.entity.*;
import com.mohaymen.model.json_item.SearchResultItem;
import com.mohaymen.model.json_item.SearchResultItemGroup;
import com.mohaymen.model.supplies.ChatType;
import com.mohaymen.model.supplies.ProfilePareId;
import com.mohaymen.repository.BlockRepository;
import com.mohaymen.repository.ChatParticipantRepository;
import com.mohaymen.repository.MessageRepository;
import com.mohaymen.repository.ProfileRepository;
import org.apache.lucene.document.Document;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class SearchService {
    private final BlockRepository blockRepository;

    private final MessageRepository messageRepository;

    private final ChatParticipantRepository chatParticipantRepository;

    private final ProfileRepository profileRepository;

    private final MessageSearch messageSearch;

    private final ChannelSearch channelSearch;

    private final UserSearch userSearch;
    private final AccountService accountService;

    public SearchService(BlockRepository blockRepository, MessageRepository messageRepository, ChatParticipantRepository chatParticipantRepository, ProfileRepository profileRepository, AccountService accountService) {
        this.blockRepository = blockRepository;
        this.messageRepository = messageRepository;
        this.chatParticipantRepository = chatParticipantRepository;
        this.profileRepository = profileRepository;
        this.accountService = accountService;
        messageSearch = new MessageSearch();
        channelSearch = new ChannelSearch();
        userSearch = new UserSearch();
    }

    public void addMessage(Message message) {
        messageSearch.indexMessageDocument(message.getMessageID().toString(),
                message.getSender().getProfileID().toString(),
                message.getReceiver().getProfileID().toString(),
                message.getText());
    }

    public void updateMessage(Message message) {
        messageSearch.updateMessage(message.getMessageID().toString(),
                message.getSender().getProfileID().toString(),
                message.getReceiver().getProfileID().toString(),
                message.getText());
    }

    public void deleteMessage(Message message) {
        messageSearch.deleteMessage(message.getMessageID().toString());
    }

    public List<Message> searchInPv(Long senderId, Long receiverId, String searchEntry) {
        List<Document> documents = messageSearch.searchInPv(senderId.toString(),
                receiverId.toString(),
                searchEntry);
        return getMessagesListFromDocuments(documents);
    }

    public List<Message> searchInChat(Long receiverId, String searchEntry) {
        List<Document> documents = messageSearch.searchInChat(receiverId.toString(), searchEntry);
        return getMessagesListFromDocuments(documents);
    }

    public List<Message> searchInAllMessages(Long profileId, String searchEntry) {
        Optional<Profile> profile = profileRepository.findById(profileId);
        if (profile.isEmpty())
            return new ArrayList<>();
        Profile p = profile.get();
        List<ChatParticipant> chatParticipants = chatParticipantRepository.findByUser(p);
        List<String> receiverPvIds = new ArrayList<>();
        List<String> receiverChatIds = new ArrayList<>();
        for (ChatParticipant chatParticipant : chatParticipants) {
            if (chatParticipant.getDestination().getType() == ChatType.USER) {
                receiverPvIds.add(chatParticipant.getDestination().getProfileID().toString());
            } else {
                receiverChatIds.add(chatParticipant.getDestination().getProfileID().toString());
            }
        }
        List<Document> documents = messageSearch.searchInAllMessages(profileId.toString(),
                receiverPvIds,
                receiverChatIds,
                searchEntry);
        return getMessagesListFromDocuments(documents);
    }

    private List<Message> getMessagesListFromDocuments(List<Document> documents) {
        List<Message> messages = new ArrayList<>();
        for (Document d : documents) {
            Optional<Message> message = messageRepository.findById(Long.valueOf(d.get("message_id")));
            message.ifPresent(messages::add);
        }
        return messages;
    }

    public void addChannel(Profile profile) {
        channelSearch.indexChannelDocument(profile.getProfileID().toString(),
                profile.getProfileName());
    }

    public void updateChannel(Profile profile) {
        channelSearch.updateChannel(profile.getProfileID().toString(),
                profile.getProfileName());
    }

    public void deleteChannel(Profile profile) {
        channelSearch.deleteChannel(profile.getProfileID().toString());
    }

    public List<Profile> searchInChannels(String searchEntry) {
        List<Document> documents = channelSearch.searchInAllChannels(searchEntry);
        return getProfilesFromDocuments(documents);
    }

    public void addUser(Account account) {
        userSearch.indexUserDocument(account.getProfile().getProfileID().toString(),
                account.getEmail(),
                account.getProfile().getHandle());
    }

    public void updateUser(Profile profile) {
        userSearch.updateUser(profile.getProfileID().toString(),
                profile.getHandle());
    }

    public void deleteUser(Profile profile) {
        userSearch.deleteUser(profile.getProfileID().toString());
    }

    public List<Profile> searchInUsers(String searchEntry) {
        List<Document> documents = userSearch.searchInAllUsers(searchEntry);
        return getProfilesFromDocuments(documents);
    }

    private List<Profile> getProfilesFromDocuments(List<Document> documents) {
        List<Profile> profiles = new ArrayList<>();
        for (Document d : documents) {
            Optional<Profile> profile = profileRepository.findById(Long.valueOf(d.get("profile_id")));
            profile.ifPresent(profiles::add);
        }
        return profiles;
    }


    public List<SearchResultItemGroup> GlobalSearch(Long profileId, String searchEntry) {

        List<SearchResultItemGroup> resultItems = new ArrayList<>();

        // channels

        SearchResultItemGroup channelsItemGroup = SearchResultItemGroup.builder()
                .title("کانال ها")
                .items(new ArrayList<>())
                .build();
        if (searchEntry.strip().length() > 2) {
            for (Profile p : searchInChannels(searchEntry)) {
                channelsItemGroup.getItems()
                        .add(SearchResultItem.builder()
                                .profile(p)
                                .text(p.getMemberCount() + " عضو ")
                                .message_id(0L)
                                .build());
            }
        }
        resultItems.add(channelsItemGroup);

        // users

        SearchResultItemGroup usersItemGroup = SearchResultItemGroup.builder()
                .title("کاربر ها")
                .items(new ArrayList<>())
                .build();
        if (searchEntry.strip().length() > 2) {
            for (Profile p : searchInUsers(searchEntry)) {
                //for savedMessage
                if (p.getProfileID().equals(profileId)) {
                    p.setProfileName("Saved Message");
                    p.setDefaultProfileColor("#0000ff");
                    p.setLastProfilePicture(null);
                }
                //check for people who blocked you
                Optional<Block> blockOptional = blockRepository.findById(new ProfilePareId(p,profileRepository.findById(profileId).get()));
                if (blockOptional.isPresent()) {
                   p.setLastProfilePicture(null);
                }
                p.setStatus(blockOptional.isPresent() ? "Last seen a long time ago" : accountService.getLastSeen(p.getProfileID()));
                //for block users

                usersItemGroup.getItems()
                        .add(SearchResultItem.builder()
                                .profile(p)
                                .text(p.getHandle())
                                .message_id(0L)
                                .build());
            }
        }
        resultItems.add(usersItemGroup);

        // messages

        SearchResultItemGroup messagesItemGroup = SearchResultItemGroup.builder()
                .title("پیام ها")
                .items(new ArrayList<>())
                .build();
        if (searchEntry.strip().length() > 0) {
            for (Message m : searchInAllMessages(profileId, searchEntry)) {
                messagesItemGroup.getItems()
                        .add(SearchResultItem.builder()
                                .profile(m.getSender())
                                .text(m.getText())
                                .message_id(m.getMessageID())
                                .build());
            }
        }
        resultItems.add(messagesItemGroup);

        messagesItemGroup.setLength(messagesItemGroup.getItems().size());

        return resultItems;
    }

}
