package com.mohaymen.full_text_search;

import lombok.SneakyThrows;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.core.KeywordAnalyzer;
import org.apache.lucene.analysis.miscellaneous.PerFieldAnalyzerWrapper;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MessageSearch extends SearchIndex {

    static final String INDEX_NAME = "/MessageIndex";

    @SneakyThrows
    public MessageSearch() {
        super(INDEX_NAME, createAnalyzer());
    }

    private static Analyzer createAnalyzer() {
        Map<String, Analyzer> analyzerMap = new HashMap<>();
        analyzerMap.put(FiledNameEnum.SenderId.value, new KeywordAnalyzer());
        analyzerMap.put(FiledNameEnum.ReceiverId.value, new KeywordAnalyzer());
        analyzerMap.put(FiledNameEnum.MessageId.value, new KeywordAnalyzer());
        analyzerMap.put(FiledNameEnum.MessageText.value, new CustomAnalyzer());
        return new PerFieldAnalyzerWrapper(new CustomAnalyzer(), analyzerMap);
    }

    private Document createDocument(String messageId,
                                    String senderProfileId,
                                    String receiverProfileId,
                                    String messageText) {
        Document document = new Document();
        document.add(new TextField(FiledNameEnum.SenderId.value, senderProfileId, Field.Store.YES));
        document.add(new TextField(FiledNameEnum.ReceiverId.value, receiverProfileId, Field.Store.YES));
        document.add(new TextField(FiledNameEnum.MessageId.value, messageId, Field.Store.YES));
        document.add(new TextField(FiledNameEnum.MessageText.value, messageText, Field.Store.YES));
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
            updateDocument(new Term(FiledNameEnum.MessageId.value,
                            analyzer.normalize(FiledNameEnum.MessageId.value, messageId)),
                    document);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void deleteMessage(String messageId) {
        Query query = new TermQuery(
                new Term(FiledNameEnum.MessageId.value,
                        analyzer.normalize(FiledNameEnum.MessageId.value, messageId)));
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
        BooleanQuery.Builder finalBooleanQuery = new BooleanQuery.Builder();

        BooleanQuery.Builder searchEntryBooleanQuery = new BooleanQuery.Builder();

        TokenStream stream  = analyzer.tokenStream(FiledNameEnum.MessageText.value, queryString);
        try {
            while(stream.incrementToken()) {
                searchEntryBooleanQuery.add(new FuzzyQuery(
                        new Term(FiledNameEnum.MessageText.value,
                                analyzer.normalize(FiledNameEnum.MessageText.value, stream.getAttribute(CharTermAttribute.class).toString())), 1),
                        BooleanClause.Occur.MUST);
            }
        }
        catch(IOException ignored) { }

        finalBooleanQuery.add(searchEntryBooleanQuery.build(),
                BooleanClause.Occur.MUST);

        BooleanQuery.Builder idBooleanQuery = new BooleanQuery.Builder();

        for (String id : receiverPvIds) {
            idBooleanQuery.add(
                    getPvIdQuery(senderProfileId, id),
                    BooleanClause.Occur.SHOULD);
        }
        for (String id : receiverChatIds) {
            idBooleanQuery.add(new TermQuery(
                    new Term(FiledNameEnum.ReceiverId.value,
                            analyzer.normalize(FiledNameEnum.ReceiverId.value, id))),
                    BooleanClause.Occur.SHOULD);
        }

        finalBooleanQuery.add(idBooleanQuery.build(),
                BooleanClause.Occur.MUST);

        try {
            return searchIndexQuery(finalBooleanQuery.build());
        } catch (IOException e) {
            return new ArrayList<>();
        }
    }

    public List<Document> searchInPv(String senderProfileId,
                                     String receiverProfileId,
                                     String queryString) {
        BooleanQuery idQuery = getPvIdQuery(senderProfileId, receiverProfileId);

        BooleanQuery booleanQuery = new BooleanQuery.Builder()
                .add(new FuzzyQuery(
                        new Term(FiledNameEnum.MessageText.value,
                                analyzer.normalize(FiledNameEnum.MessageText.value, queryString)), 1),
                        BooleanClause.Occur.MUST)
                .add(idQuery, BooleanClause.Occur.MUST)
                .build();

        try {
            return searchIndexQuery(booleanQuery);
        } catch (IOException e) {
            return new ArrayList<>();
        }
    }

    public List<Document> searchInChat(String receiverProfileId, String queryString) {
        BooleanQuery booleanQuery = new BooleanQuery.Builder()
                .add(new FuzzyQuery(
                        new Term(FiledNameEnum.MessageText.value,
                                analyzer.normalize(FiledNameEnum.MessageText.value, queryString)), 1),
                        BooleanClause.Occur.MUST)
                .add(new TermQuery(
                        new Term(FiledNameEnum.ReceiverId.value,
                                analyzer.normalize(FiledNameEnum.ReceiverId.value, receiverProfileId))),
                        BooleanClause.Occur.MUST)
                .build();

        try {
            return searchIndexQuery(booleanQuery);
        } catch (IOException e) {
            return new ArrayList<>();
        }
    }

    private BooleanQuery getPvIdQuery(String senderProfileId,
                                      String receiverProfileId) {
        BooleanQuery idQuerySenderReceiver = new BooleanQuery.Builder()
                .add(new TermQuery(
                        new Term(FiledNameEnum.SenderId.value,
                                analyzer.normalize(FiledNameEnum.SenderId.value, senderProfileId))),
                        BooleanClause.Occur.MUST)
                .add(new TermQuery(
                        new Term(FiledNameEnum.ReceiverId.value,
                                analyzer.normalize(FiledNameEnum.ReceiverId.value, receiverProfileId))),
                        BooleanClause.Occur.MUST)
                .build();

        BooleanQuery idQueryReceiverSender = new BooleanQuery.Builder()
                .add(new TermQuery(
                        new Term(FiledNameEnum.SenderId.value,
                                analyzer.normalize(FiledNameEnum.SenderId.value, receiverProfileId))),
                        BooleanClause.Occur.MUST)
                .add(new TermQuery(
                        new Term(FiledNameEnum.ReceiverId.value,
                                analyzer.normalize(FiledNameEnum.ReceiverId.value, senderProfileId))),
                        BooleanClause.Occur.MUST)
                .build();

        return new BooleanQuery.Builder()
                .add(idQuerySenderReceiver, BooleanClause.Occur.SHOULD)
                .add(idQueryReceiverSender, BooleanClause.Occur.SHOULD)
                .build();
    }

}
