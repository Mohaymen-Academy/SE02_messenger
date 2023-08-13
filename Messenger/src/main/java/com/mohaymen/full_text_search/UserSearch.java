package com.mohaymen.full_text_search;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import java.io.IOException;

public class UserSearch extends SearchIndex {

    static final String INDEX_NAME = "/UserIndex";

    public UserSearch() {
        super(INDEX_NAME, new StandardAnalyzer());
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

}