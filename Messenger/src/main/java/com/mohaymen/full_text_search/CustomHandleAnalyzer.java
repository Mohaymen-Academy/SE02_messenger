package com.mohaymen.full_text_search;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.core.KeywordTokenizer;
import org.apache.lucene.analysis.core.UpperCaseFilter;

public class CustomHandleAnalyzer extends Analyzer {

    @Override
    protected Analyzer.TokenStreamComponents createComponents(String fieldName) {
        KeywordTokenizer src = new KeywordTokenizer();
        TokenStream result = new UpperCaseFilter(src);
        return new TokenStreamComponents(src, result);
    }

    @Override
    protected TokenStream normalize(String fieldName, TokenStream in) {
        TokenStream result = new UpperCaseFilter(in);
        return result;
    }

}