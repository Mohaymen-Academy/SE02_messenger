package com.mohaymen.full_text_search;

import lombok.SneakyThrows;
import org.apache.lucene.analysis.ar.ArabicNormalizationFilterFactory;
import org.apache.lucene.analysis.core.DecimalDigitFilterFactory;
import org.apache.lucene.analysis.core.LowerCaseFilterFactory;
import org.apache.lucene.analysis.custom.CustomAnalyzer;
import org.apache.lucene.analysis.fa.PersianNormalizationFilterFactory;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.FuzzyQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;

import java.io.IOException;
import java.util.List;

public class ChannelSearch extends SearchIndex {

    static final String INDEX_NAME = "ChannelIndex";

    @SneakyThrows
    public ChannelSearch() {
        super(INDEX_NAME);
        this.analyzer = CustomAnalyzer.builder()
                .withTokenizer("standard")
                .addTokenFilter(LowerCaseFilterFactory.class)
                .addTokenFilter(DecimalDigitFilterFactory.class)
                .addTokenFilter(DecimalDigitFilterFactory.class)
                .addTokenFilter(ArabicNormalizationFilterFactory.class)
                .addTokenFilter(PersianNormalizationFilterFactory.class)
                .build();
    }

    private Document createDocument(String profileId,
                                    String channelName) {
        Document document = new Document();
        document.add(new TextField("profile_id", profileId, Field.Store.YES));
        document.add(new TextField("channel_name", channelName, Field.Store.YES));
        return document;
    }

    public void indexChannelDocument(String profileId,
                                     String channelName) {
        Document document = createDocument(profileId, channelName);
        try {
            indexDocument(document);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void updateChannel(String profileId,
                              String channelName) {
        Document document = createDocument(profileId, channelName);
        try {
            updateDocument(new Term("profile_id", profileId), document);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void deleteChannel(String profileId) {
        Query query = new TermQuery(new Term("profile_id", profileId));
        try {
            deleteDocument(query);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public List<Document> searchInAllChannels(String queryString) {
        try {
            return searchIndexQuery(new FuzzyQuery(new Term("channel_name", queryString)));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
