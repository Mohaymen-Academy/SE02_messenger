package com.mohaymen.service;

import com.mohaymen.full_text_search.FullTextSearch;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.fa.PersianAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.document.Document;
import org.apache.lucene.queryparser.classic.ParseException;
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

    public void addMessage(String senderProfileId, String receiverProfileId, String messageId, String messageText) {
        try {
            fullTextSearch.indexDocument(senderProfileId, receiverProfileId, messageId, messageText);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void searchInMessages(String entry) {
        List<Document> documents;
        try {
            documents = fullTextSearch.fuzzySearchIndex("message_text", entry);
        } catch (ParseException | IOException e) {
            throw new RuntimeException(e);
        }
        if(documents != null && documents.size() > 0) {
            for (Document d : documents) {
                System.out.println(d.get("message_id"));
            }
        }
        else {
            System.out.println("not found");
        }
    }

}
