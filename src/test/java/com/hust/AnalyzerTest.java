package com.hust;

import java.io.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import static com.hust.SpamMailDetection.*;

public class AnalyzerTest {
    public static void main(String[] args) {
        String[] list = {"hello", "world", "hello", "hi"};
        Map<String, Integer> tmpmap = new HashMap<String, Integer>();
        int count = 0;
        for (String s : list) {
            if (tmpmap.containsKey(s)) {
                tmpmap.put(s, tmpmap.get(s) + 1);
            } else {
                tmpmap.put(s, 1);
            }
        }
        for (String key: tmpmap.keySet()) {
            System.out.println(key + ":" + tmpmap.get(key));
        }
    }

    @Test
    public void testGetFileList() {
        SpamMailDetection spamMailDetection = new SpamMailDetection();
        List<String> list = getFileList(HAM_PATH);
        for (String file : list) {
            String text = spamMailDetection.readBody(file);
            String[] wordList = EmailSegment.cutWords(text).split(" ");
            for(String word : wordList) {
                System.out.println(word);
            }
        }
//        StringBuffer stringBuffer = new StringBuffer();
//        for (String item : list) {
//            stringBuffer.append(spamMailDetection.readFile(item));
//        }
//        String string = stringBuffer.toString();
//        String[] content = PhraseAnalyzer.split(string, " ").split(" ");
//        for(String item : content) {
//            System.out.println(item);
//        }
    }

    @Test
    public void testReadBody() {
        SpamMailDetection spamMailDetection = new SpamMailDetection();
        String filePath = "H:\\data\\ceas08-1\\ceas08-1\\spam\\0005_0449";
        String res = spamMailDetection.readBody(filePath);
        String[] content = PhraseAnalyzer.split(res, " ").split(" ");
        for (String item : content) {
            System.out.println(item);
        }
    }

    @Test
    public void testGetFileCount() {
        String path = "E:\\Downloads\\ceas08-1\\ceas08-1\\data";
        int fileCount = SpamMailDetection.getFileCount(path);
        System.out.println(fileCount);
    }

    @Test
    public void testExtractFeature() {
        FeatureExtraction featureExtraction = new FeatureExtraction();
        List<String> featureList = featureExtraction.extractFeature();
        for (String word : featureList) {
            System.out.println(word);
        }
    }

    @Test
    public void testGetFeatureCount() {
        FeatureExtraction featureExtraction = new FeatureExtraction();
        Map<String, Integer> resMap = featureExtraction.getFeatureCount(HAM_PATH);
        for (String word : resMap.keySet()) {
            System.out.println(word);
        }
    }

    @Test
    public void testGetFileName() {
        FeatureExtraction featureExtraction = new FeatureExtraction();
        String file = "F:\\spam filter\\data\\Test\\0200_0679";
        System.out.println(featureExtraction.getFileName(file));
    }

    @Test
    public void testGenerateClassification() {
        FeatureExtraction featureExtraction = new FeatureExtraction();
        Map<String, String> tmp = featureExtraction.generateClassification();
        for (String key : tmp.keySet()) {
            System.out.println(key + ":" + tmp.get(key));
        }
    }

    @Test
    public void testReadFile() {
        String file = "H:\\corpus\\Result\\hamMap.txt";
        try {
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(new File(file))));
            String tmp = "";
            while ((tmp = bufferedReader.readLine()) != null) {
                String[] contentList = tmp.split("\t");
                double value = Double.parseDouble(contentList[1]);
                System.out.println(contentList[0] + "\t" + value);
            }
            bufferedReader.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testContain() {
        String content = "RE:opensuse";
        if (content.contains("re:")){
            System.out.println("True");
        } else {
            System.out.println("False");
        }
    }
    @Test
    public void testCutWords() {
        EmailSegment emailSegment = new EmailSegment();
        String text = "Dear Amazon.com Customer,";
        String[] content = emailSegment.cutWords(text).split(" ");
        for (String word : content) {
            System.out.println(word);
        }
//        String ham_corpus = "H:\\data\\ceas08-1\\ceas08-1\\ham-full";
//        String ham_path = "H:\\Experiment\\Experiment5\\Train\\ham";
//        String spam_path = "H:\\Experiment\\Experiment5\\Train\\spam";
//        String spam_corpus = "H:\\data\\ceas08-1\\ceas08-1\\spam-full";
//        List<String> fileList = getFileList(spam_corpus);
//        String null_mail = "H:\\data\\ceas08-1\\ceas08-1\\null-email\\tmp\\";
//
//        for (String file : fileList) {
//            String res = SpamMailDetection.readBody(file);
//            if (res.equals("")) {
//                System.out.println(file);
//                File old = new File(file);
//                File newFile = new File(null_mail + old.getName());
//                old.renameTo(newFile);
//            }
////            String[] wordList = EmailSegment.cutWords(res).split(" ");
//        }
    }

    /**
    @Test
    public void testMailWord() {
        EmailSegment emailSegment = new EmailSegment();
        String sampleMail = SpamMailDetection.EMAIL_PATH + "\\2790_0906";
        String res = SpamMailDetection.readBody(sampleMail);
        String[] wordList = emailSegment.cutWords(res).split(" ");
        Map<String, Double> probabilityMap = new HashMap<String, Double>();
        try {
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(new File(SpamMailDetection.PROBABILITY_MAP_PATH))));
            String tmp = "";
            while ((tmp = bufferedReader.readLine()) != null) {
                String[] ret = tmp.split("\t");
                probabilityMap.put(ret[0], Double.parseDouble(ret[1]));
            }
            bufferedReader.close();
        }catch (IOException e) {
            e.printStackTrace();
        }
        for (String word : wordList) {
            if(probabilityMap.containsKey(word)) {
                System.out.println(word + "\t" + probabilityMap.get(word));
            }
        }
    }
*/
    @Test
    public void testStopWord() {
        String text = "tis is num 0ddds 1 2";
        String[] res = EmailSegment.cutWords(text).split(" ");
        for (String item : res) {
            System.out.println(item);
        }
    }

    @Test
    public void testIntSortMap() {
        Map<String, Integer> map = new HashMap<String, Integer>();
        map.put("hello",7);
        map.put("world",9);
        map.put("you", 8);
////        Map<String, Integer> retMap = MapSort.intSortMap(map);
//        for (String item : retMap.keySet()) {
//            System.out.println(item + retMap.get(item));
//        }
    }

    @Test
    public void testSubject() {
        EmailSegment emailSegment = new EmailSegment();
        String file = "H:\\Experiment\\Experiment3\\Train\\ham\\0005_0297";
        String subject = EmailSubject.getSubject(file);
        String[] list = EmailSegment.cutWords(subject).split(" ");
        for (String word : list) {
            System.out.println(word);
        }
        double reCoefficeient = EmailSubject.getReSubject(subject);
        double hobbyCoefficent = EmailSubject.getHobbySubject(subject);
        System.out.println(subject);
        System.out.println(reCoefficeient + "\t" + hobbyCoefficent);
    }

    @Test
    public void testUAI() {
        SpamMailDetection smd = new SpamMailDetection();
        List<String> fileList = getFileList(SpamMailDetection.EMAIL_PATH);
        int countUAI = 0;
        for (String file : fileList) {
            String subject = EmailSubject.getSubject(file);
            if (subject.contains("UAI")) {
                System.out.println(file);
                countUAI++;
            }
        }
        System.out.println("countUAI = " + countUAI);
    }

    @Test
    public void testSplit() {
        List<String> fileList = SpamMailDetection.getFileList(MISJUDGE_SPAM);
        for (String file : fileList) {
            String subject = EmailSubject.getSubject(file);
            double re = EmailSubject.getReSubject(subject);
            System.out.println(subject + " :" + re);
        }
    }
}
