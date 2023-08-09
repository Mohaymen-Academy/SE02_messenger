package com.mohaymen.service;

import com.mohaymen.full_text_search.FullTextSearch;
import com.mohaymen.model.Profile;
import org.apache.lucene.document.Document;
import org.apache.lucene.queryparser.classic.ParseException;
import org.springframework.stereotype.Service;
import java.io.IOException;
import java.util.List;

@Service
public class SearchService {

    private final FullTextSearch fullTextSearch;

    public SearchService() {
        fullTextSearch = new FullTextSearch();
    }

    public void addMessage(Long senderProfileId, Long receiverProfileId, Long messageId, String messageText) {
        try {
            fullTextSearch.indexDocument(senderProfileId, receiverProfileId, messageId, messageText);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void searchInChatMessages(Profile sender, Profile receiver, String searchEntry) {
        List<Document> documents;
        try {
            documents = fullTextSearch.fuzzySearchIndex("message_text", searchEntry);
        } catch (ParseException | IOException e) {
            throw new RuntimeException(e);
        }
//        List<Long> messageIds
        if(documents != null && documents.size() > 0) {
            for (Document d : documents) {
                System.out.println(d.get("message_id"));
            }
        }
        else {
            System.out.println("not found");
        }
    }

    public void searchInProfiles(Profile sender, Profile receiver, String searchEntry) {

    }

    public void searchInChannels(Profile sender, Profile receiver, String searchEntry) {

    }

    public void searchInAllMessages(Profile sender, Profile receiver, String searchEntry) {

    }
}
