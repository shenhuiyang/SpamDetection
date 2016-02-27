package com.hust;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.wltea.analyzer.lucene.IKAnalyzer;

import java.io.IOException;
import java.io.StringReader;


public class PhraseAnalyzer {

    public static void main(String[] args) {
        String text = "this is 中文分词和English.";
        String[] terms = null;
        terms = PhraseAnalyzer.split(text, " ").split(" ");
        for (String item : terms) {
            System.out.println(item);
        }
    }

    /**
     * 中文分词->获取文本的关键字向量
     *
     * @param text
     * @param splitToken
     * @return
     */
    public static String split(String text, String splitToken) {
        StringBuffer result = new StringBuffer("");
        try {
            Analyzer analyzer = new IKAnalyzer(true);
            StringReader reader = new StringReader(text);
            TokenStream ts = analyzer.tokenStream(null, reader);
            ts.addAttribute(CharTermAttribute.class);
            while (ts.incrementToken()) {
                CharTermAttribute ta = ts.getAttribute(CharTermAttribute.class);
                result.append(ta.toString() + splitToken);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result.toString();
    }
}
