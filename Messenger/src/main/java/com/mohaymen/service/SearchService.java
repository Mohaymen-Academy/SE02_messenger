package com.mohaymen.service;

import com.mohaymen.full_text_search.ChannelSearch;
import com.mohaymen.full_text_search.MessageSearch;
import com.mohaymen.full_text_search.UserSearch;
import com.mohaymen.model.entity.Account;
import com.mohaymen.model.entity.ChatParticipant;
import com.mohaymen.model.entity.Message;
import com.mohaymen.model.entity.Profile;
import com.mohaymen.model.json_item.SearchResultItem;
import com.mohaymen.model.json_item.SearchResultItemGroup;
import com.mohaymen.model.supplies.ChatType;
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

    private final MessageRepository messageRepository;

    private final ChatParticipantRepository chatParticipantRepository;

    private final ProfileRepository profileRepository;

    private final MessageSearch messageSearch;

    private final ChannelSearch channelSearch;

    private final UserSearch userSearch;

    public SearchService(MessageRepository messageRepository, ChatParticipantRepository chatParticipantRepository, ProfileRepository profileRepository) {
        this.messageRepository = messageRepository;
        this.chatParticipantRepository = chatParticipantRepository;
        this.profileRepository = profileRepository;
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
        if(!profile.isPresent())
            return new ArrayList<>();
        Profile p = profile.get();
        List<ChatParticipant> chatParticipants = chatParticipantRepository.findByUser(p);
        List<String> receiverPvIds = new ArrayList<>();
        List<String> receiverChatIds = new ArrayList<>();
        for (ChatParticipant chatParticipant : chatParticipants) {
            if(chatParticipant.getDestination().getType() == ChatType.USER) {
                receiverPvIds.add(chatParticipant.getDestination().getProfileID().toString());
            }
            else {
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

    public void updateUser(Account account) {
        userSearch.updateUser(account.getProfile().getProfileID().toString(),
                account.getEmail(),
                account.getProfile().getHandle());
    }

    public void deleteUser(Account account) {
        userSearch.deleteUser(account.getProfile().getProfileID().toString());
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


        SearchResultItemGroup itemGroup = SearchResultItemGroup.builder()
                .title("پیام ها")
                .items(new ArrayList<>())
                .build();

        for (Message m : searchInAllMessages(profileId, searchEntry)) {
            itemGroup.getItems()
                    .add(SearchResultItem.builder()
                            .profile(m.getSender())
                            .text(m.getText())
                            .message_id(m.getMessageID())
                            .build());
        }

        resultItems.add(itemGroup);

        return resultItems;
    }
}
