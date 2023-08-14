package com.mohaymen.full_text_search;

import org.apache.lucene.analysis.fa.PersianAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.*;
import java.io.IOException;
import java.util.List;

public class MessageSearch extends SearchIndex {

    static final String INDEX_NAME = "/MessageIndex";

    public MessageSearch() {
        super(INDEX_NAME, new PersianAnalyzer());
    }

    private Document createDocument(String messageId,
                                    String senderProfileId,
                                    String receiverProfileId,
                                    String messageText) {
        Document document = new Document();
        document.add(new TextField("sender_profile_id", senderProfileId, Field.Store.YES));
        document.add(new TextField("receiver_profile_id", receiverProfileId, Field.Store.YES));
        document.add(new TextField("message_id", messageId, Field.Store.YES));
        document.add(new TextField("message_text", messageText, Field.Store.YES));
        return document;
    }

    public void indexMessageDocument(String messageId,
                                     String senderProfileId,
                                     String receiverProfileId,
                                     String messageText) {
        Document document =  createDocument(messageId, senderProfileId, receiverProfileId, messageText);
        try {
            indexDocument(document);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void updateMessage(String messageId,
                           String senderProfileId,
                           String receiverProfileId,
                           String messageText) {
        Document document =  createDocument(messageId, senderProfileId, receiverProfileId, messageText);
        try {
            updateDocument(new Term("message_id", messageId), document);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void deleteMessage(String messageId) {
        Query query = new TermQuery(new Term("message_id", messageId));
        try {
            deleteDocument(query);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public List<Document> searchInAllMessages(String senderProfileId,
                                              List<String> receiverPvIds,
                                              List<String> receiverChatIds,
                                              String queryString) {
        BooleanQuery.Builder booleanQueryBuilder = new BooleanQuery.Builder();

        booleanQueryBuilder.add(new FuzzyQuery(new Term("message_text", queryString)), BooleanClause.Occur.MUST);

        for (String id : receiverPvIds) {
            booleanQueryBuilder.add(getPvIdQuery(senderProfileId, id), BooleanClause.Occur.SHOULD);
        }
        for (String id : receiverChatIds) {
            booleanQueryBuilder.add(new TermQuery(new Term("receiver_profile_id", id)), BooleanClause.Occur.SHOULD);
        }

        try {
            return searchIndexQuery(booleanQueryBuilder.build());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public List<Document> searchInPv(String senderProfileId,
                                     String receiverProfileId,
                                     String queryString) {
        BooleanQuery idQuery = getPvIdQuery(senderProfileId, receiverProfileId);

        BooleanQuery booleanQuery = new BooleanQuery.Builder()
                .add(new FuzzyQuery(new Term("message_text", queryString)), BooleanClause.Occur.MUST)
                .add(idQuery, BooleanClause.Occur.MUST)
                .build();

        try {
            return searchIndexQuery(booleanQuery);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public List<Document> searchInChat(String receiverProfileId, String queryString) {
        BooleanQuery booleanQuery = new BooleanQuery.Builder()
                .add(new FuzzyQuery(new Term("message_text", queryString)), BooleanClause.Occur.MUST)
                .add(new TermQuery(new Term("receiver_profile_id",receiverProfileId)), BooleanClause.Occur.MUST)
                .build();

        try {
            return searchIndexQuery(booleanQuery);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private BooleanQuery getPvIdQuery(String senderProfileId,
                                      String receiverProfileId) {
        BooleanQuery idQuerySenderReceiver = new BooleanQuery.Builder()
                .add(new TermQuery(new Term("sender_profile_id", senderProfileId)), BooleanClause.Occur.MUST)
                .add(new TermQuery(new Term("receiver_profile_id", receiverProfileId)), BooleanClause.Occur.MUST)
                .build();

        BooleanQuery idQueryReceiverSender = new BooleanQuery.Builder()
                .add(new TermQuery(new Term("sender_profile_id", receiverProfileId)), BooleanClause.Occur.MUST)
                .add(new TermQuery(new Term("receiver_profile_id", senderProfileId)), BooleanClause.Occur.MUST)
                .build();

        return new BooleanQuery.Builder()
                .add(idQuerySenderReceiver, BooleanClause.Occur.SHOULD)
                .add(idQueryReceiverSender, BooleanClause.Occur.SHOULD)
                .build();
    }

}
