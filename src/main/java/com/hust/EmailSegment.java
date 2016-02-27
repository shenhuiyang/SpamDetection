package com.hust;

import org.wltea.analyzer.core.IKSegmenter;
import org.wltea.analyzer.core.Lexeme;

import java.io.IOException;
import java.io.StringReader;

public class EmailSegment {

    /*
	 * 对文章进行分词
	 * 输入参数为String类型的文章
	 * 输出为以空格号分隔的文章词语
	 * */
    public static String cutWords(String content)
    {
        String words = "";
        //利用IKAnalyzer进行分词
        StringReader reader = new StringReader(content);
        IKSegmenter ik = new IKSegmenter(reader,true);
        Lexeme lex = null;
        try {
            while((lex = ik.next()) != null)
            {
                words += (lex.getLexemeText() + " ");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        if(words.length() <= 0 )
            words = null;
        else
            words = words.substring(0, words.length()-1);
        return words;
    }

}
