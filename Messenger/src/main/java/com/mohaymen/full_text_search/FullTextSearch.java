package com.mohaymen.full_text_search;

import lombok.SneakyThrows;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.fa.PersianAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.*;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class FullTextSearch {

    static final String INDEX_DIRECTORY = "../LuceneDirectory/index";

    private final Directory memoryIndex;

    private final Analyzer analyzer;

    @SneakyThrows
    public FullTextSearch() {
        memoryIndex = FSDirectory.open(Paths.get(INDEX_DIRECTORY));
        analyzer = new PersianAnalyzer();
    }

    public void indexDocument(String senderProfileId, String receiverProfileId, String messageId, String messageText) throws IOException {
        IndexWriterConfig indexWriterConfig = new IndexWriterConfig(analyzer);
        IndexWriter writer = new IndexWriter(memoryIndex, indexWriterConfig);
        Document document = new Document();

        document.add(new TextField("sender_profile_id", senderProfileId, Field.Store.YES));
        document.add(new TextField("receiver_profile_id", receiverProfileId, Field.Store.YES));
        document.add(new TextField("message_id", messageId, Field.Store.YES));
        document.add(new TextField("message_text", messageText, Field.Store.YES));

        writer.addDocument(document);
        writer.close();
    }

    public List<Document> searchIndex(Query query) throws IOException {
        IndexReader indexReader = DirectoryReader.open(memoryIndex);
        IndexSearcher searcher = new IndexSearcher(indexReader);
        TopDocs topDocs = searcher.search(query, 10);
        List<Document> documents = new ArrayList<>();
        for (ScoreDoc scoreDoc : topDocs.scoreDocs) {
            documents.add(searcher.doc(scoreDoc.doc));
        }
        return documents;
    }

    public List<Document> fuzzySearchIndex(String inField, String queryString) throws ParseException, IOException {
        Term term = new Term(inField, queryString);
        Query query = new FuzzyQuery(term);
        return searchIndex(query);
    }

}
