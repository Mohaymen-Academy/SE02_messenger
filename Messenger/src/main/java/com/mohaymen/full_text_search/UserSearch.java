package com.mohaymen.full_text_search;

import lombok.SneakyThrows;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.*;
import java.io.IOException;
import java.util.List;

public class UserSearch extends SearchIndex {

    static final String INDEX_NAME = "/UserIndex";

    @SneakyThrows
    public UserSearch() {
        super(INDEX_NAME, new CustomHandleAnalyzer());
    }

    private Document createDocument(String profileId,
                                    String email,
                                    String handel) {
        Document document = new Document();
        document.add(new TextField("profile_id", profileId, Field.Store.YES));
        document.add(new TextField("email", email, Field.Store.YES));
        document.add(new TextField("handel", handel, Field.Store.YES));
        return document;
    }

    public void indexUserDocument(String profileId,
                                  String email,
                                  String handel) {
        Document document = createDocument(profileId, email, handel);
        try {
            indexDocument(document);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void updateUser(String profileId,
                           String email,
                           String handel) {
        Document document = createDocument(profileId, email, handel);
        try {
            updateDocument(new Term("profile_id", profileId), document);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void deleteUser(String profileId) {
        Query query = new TermQuery(new Term("profile_id", profileId));
        try {
            deleteDocument(query);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public List<Document> searchInAllUsers(String queryString) {
        BooleanQuery booleanQuery = new BooleanQuery.Builder()
                .add(new TermQuery(new Term("email", queryString)), BooleanClause.Occur.SHOULD)
                .add(new TermQuery(new Term("handel", queryString)), BooleanClause.Occur.SHOULD)
                .build();
        try {
            return searchIndexQuery(booleanQuery);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
