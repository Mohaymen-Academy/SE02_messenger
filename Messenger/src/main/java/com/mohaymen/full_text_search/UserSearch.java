package com.mohaymen.full_text_search;

import lombok.SneakyThrows;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.KeywordAnalyzer;
import org.apache.lucene.analysis.miscellaneous.PerFieldAnalyzerWrapper;
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

public class UserSearch extends SearchIndex {

    static final String INDEX_NAME = "/UserIndex";

    @SneakyThrows
    public UserSearch() {
        super(INDEX_NAME, createAnalyzer());
    }

    private static Analyzer createAnalyzer() {
        Map<String, Analyzer> analyzerMap = new HashMap<>();
        analyzerMap.put(FiledNameEnum.ProfileId.value, new KeywordAnalyzer());
        analyzerMap.put(FiledNameEnum.Email.value, new CustomHandleAnalyzer());
        analyzerMap.put(FiledNameEnum.EmailShort.value, new CustomHandleAnalyzer());
        analyzerMap.put(FiledNameEnum.Handle.value, new CustomHandleAnalyzer());
        return new PerFieldAnalyzerWrapper(new CustomAnalyzer(), analyzerMap);
    }

    private Document createDocument(String profileId,
                                    String email,
                                    String handle) {
        Document document = new Document();
        document.add(new TextField(FiledNameEnum.ProfileId.value, profileId, Field.Store.YES));
        document.add(new TextField(FiledNameEnum.Email.value, email, Field.Store.YES));
        document.add(new TextField(FiledNameEnum.EmailShort.value, email.split("@")[0], Field.Store.YES));
        document.add(new TextField(FiledNameEnum.Handle.value, handle, Field.Store.YES));
        return document;
    }

    private Document createDocument(String profileId,
                                    String handle) {
        Document document = new Document();
        document.add(new TextField(FiledNameEnum.ProfileId.value, profileId, Field.Store.YES));
        document.add(new TextField(FiledNameEnum.Handle.value, handle, Field.Store.YES));
        return document;
    }

    public void indexUserDocument(String profileId,
                                  String email,
                                  String handle) {
        Document document = createDocument(profileId, email, handle);
        try {
            indexDocument(document);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void updateUser(String profileId,
                           String handle) {
        Document document = createDocument(profileId, handle);
        try {
            updateDocument(
                    new Term(FiledNameEnum.ProfileId.value,
                            analyzer.normalize(FiledNameEnum.ProfileId.value, profileId)),
                    document);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void deleteUser(String profileId) {
        Query query = new TermQuery(
                new Term(FiledNameEnum.ProfileId.value,
                        analyzer.normalize(FiledNameEnum.ProfileId.value, profileId)));
        try {
            deleteDocument(query);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public List<Document> searchInAllUsers(String queryString) {
        BooleanQuery booleanQuery = new BooleanQuery.Builder()
                .add(new PrefixQuery(new Term(FiledNameEnum.Email.value,
                        analyzer.normalize(FiledNameEnum.Email.value, queryString))),
                        BooleanClause.Occur.SHOULD)
                .add(new PrefixQuery(new Term(FiledNameEnum.EmailShort.value,
                                analyzer.normalize(FiledNameEnum.EmailShort.value, queryString))),
                        BooleanClause.Occur.SHOULD)
                .add(new PrefixQuery(new Term(FiledNameEnum.Handle.value,
                        analyzer.normalize(FiledNameEnum.Handle.value, queryString))),
                        BooleanClause.Occur.SHOULD)
                .build();
        try {
            return searchIndexQuery(booleanQuery);
        } catch (IOException e) {
            return new ArrayList<>();
        }
    }

}
