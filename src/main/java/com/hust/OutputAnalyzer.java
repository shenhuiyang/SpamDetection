package com.hust;

import java.io.*;

public class OutputAnalyzer {

    public static void main(String[] args) {
        OutputAnalyzer.analyseResult(SpamMailDetection.RESULT_PATH);
    }


    /**
     * 分析result.txt文件，得出spam误判为ham的数量以及ham误判为spam的数量
     */
    public static void analyseResult(String resultFile) {
        try {
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(new File(resultFile))));
            String tmp = "";
            int count = 0;
            int spamAsHam = 0;
            int hamAsSpam = 0;
            while ((tmp = bufferedReader.readLine()) != null) {
                String[] content = tmp.split("\t");
                if (!content[1].equals(content[2])) {
                    if (content[1].equals("spam")) {
                        spamAsHam++;
                    } else {
                        hamAsSpam++;
                    }
                    count++;
                    System.out.println(content[1] + " " + content[0] + " is misjudge as " + content[2] + " probability is " + content[3]);
                }
            }
            bufferedReader.close();
            System.out.println("total misjudge : " + count);
            System.out.println("spam misjudge as ham : " + spamAsHam);
            System.out.println("ham misjudge as spam : " + hamAsSpam);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
