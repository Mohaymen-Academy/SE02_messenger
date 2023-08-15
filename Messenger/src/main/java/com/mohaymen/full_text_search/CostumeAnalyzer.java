package com.mohaymen.full_text_search;

import org.apache.lucene.analysis.*;
import org.apache.lucene.analysis.ar.ArabicNormalizationFilter;
import org.apache.lucene.analysis.core.DecimalDigitFilter;
import org.apache.lucene.analysis.fa.PersianCharFilter;
import org.apache.lucene.analysis.fa.PersianNormalizationFilter;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import java.io.Reader;

public class CostumeAnalyzer extends Analyzer {

    @Override
    protected Analyzer.TokenStreamComponents createComponents(String fieldName) {
        StandardTokenizer src = new StandardTokenizer();
        TokenStream result = new LowerCaseFilter(src);
        result = new LowerCaseFilter(result);
        result = new DecimalDigitFilter(result);
        result = new ArabicNormalizationFilter(result);
        result = new PersianNormalizationFilter(result);
        return new TokenStreamComponents(src, result);
    }

    @Override
    protected TokenStream normalize(String fieldName, TokenStream in) {
        TokenStream result = new LowerCaseFilter(in);
        result = new DecimalDigitFilter(result);
        result = new ArabicNormalizationFilter(result);
        result = new PersianNormalizationFilter(result);
        return result;
    }

    @Override
    protected Reader initReader(String fieldName, Reader reader) {
        return new PersianCharFilter(reader);
    }

}