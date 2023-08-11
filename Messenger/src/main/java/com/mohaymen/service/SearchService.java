package com.mohaymen.service;

import com.mohaymen.full_text_search.FullTextSearch;
import com.mohaymen.model.Profile;
import org.apache.lucene.document.Document;
import org.springframework.stereotype.Service;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class SearchService {

    private final FullTextSearch fullTextSearch;

    public SearchService() {
        fullTextSearch = new FullTextSearch();
    }

    public void addMessage(Long senderProfileId, Long receiverProfileId, Long messageId, String messageText) {
        try {
            fullTextSearch.indexDocument(senderProfileId.toString(), receiverProfileId.toString(), messageId.toString(), messageText);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public List<Long> searchInPv(Long senderId, Long receiverId, String searchEntry) {
        List<Document> documents;
        try {
            documents = fullTextSearch.searchInPv(senderId.toString(),
                    receiverId.toString(),
                    searchEntry);
            documents.addAll(fullTextSearch.searchInPv(receiverId.toString(),
                    senderId.toString(),
                    searchEntry));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        List<Long> messageIds = new ArrayList<>();
        System.out.println(documents.size());
        for (Document d : documents) {
            messageIds.add(Long.valueOf(d.get("message_id")));
        }
        return messageIds;
    }

    public void searchInAllMessages(Profile sender, Profile receiver, String searchEntry) {

    }
}
