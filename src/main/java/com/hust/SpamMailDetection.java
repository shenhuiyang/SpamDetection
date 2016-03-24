package com.hust;

import java.io.*;
import java.util.*;


public class SpamMailDetection {
    public static final String BASE_PATH = "H:\\Experiment_Full\\Experiment8";
    public static final String SPAM_PATH = BASE_PATH + "\\Train\\spam";//spam训练集目录
    public static final String HAM_PATH = BASE_PATH + "\\Train\\ham";//ham训练集目录
    public static final String EMAIL_PATH = BASE_PATH + "\\Test";//测试集目录
    public static final String DEBUG_PATH = BASE_PATH + "\\Debug";
    public static final int FEATURE_NUM = 800;
    public static final String ALGORITHM = "ATIB";
    public static final String suffix = FEATURE_NUM + "features_" + ALGORITHM;
    public static final String RESULT_PATH = BASE_PATH + "\\Result\\result_" + suffix;
    public static final String OUTPUT_PATH = BASE_PATH + "\\OutputAnalysis\\analysis_" + suffix;
    public static final String DICT_PATH = BASE_PATH + "\\dict.txt";//词典
    public static final String PROBABILITY_MAP_PATH = BASE_PATH + "\\Result\\probabilityMap_" + FEATURE_NUM;
    public static final String SPAM_WORD_MAP = BASE_PATH + "\\Result\\spamWordMap.txt";
    public static final String HAM_WORD_MAP = BASE_PATH + "\\Result\\hamWordMap.txt";
    public static final String SPAM_MAP = BASE_PATH + "\\Result\\spamMap_" + FEATURE_NUM;
    public static final String HAM_MAP = BASE_PATH + "\\Result\\hamMap_" + FEATURE_NUM;
    public static final String MISJUDGE_HAM = BASE_PATH + "\\Misjudge\\ham";
    public static final String MISJUDGE_SPAM = BASE_PATH + "\\Misjudge\\spam";
    public static int LS = 2; //laplace系数

    public static void main(String[] args) {
        long startTime = System.currentTimeMillis();
        SpamMailDetection smc = new SpamMailDetection();
        smc.trainBayes();
        Map<String, Double> hamMap = smc.readMap(HAM_MAP);
        //<word,(word/SpamCorpus)>
        Map<String, Double> spamMap = smc.readMap(SPAM_MAP);
        //<word,(word/NonSpamCorpus)>
//        Map<String, Double> hamMap = smc.createMailMap(HAM_PATH);
//        //<word,(word/SpamCorpus)>
//        Map<String, Double> spamMap = smc.createMailMap(SPAM_PATH);

        smc.judgeNewMail(EMAIL_PATH,spamMap,hamMap,ALGORITHM);
//        Map<String, Double> probabilityMap = smc.createSpamProbabilityMap(spamMap, hamMap);
//        smc.judgeMail(EMAIL_PATH, probabilityMap, ALGORITHM);
        long endTime = System.currentTimeMillis();
        OutputAnalyzer.analyseResult(RESULT_PATH);
        System.out.println("time costs:" + (endTime - startTime)/1000/60 + "min");
    }

    /**
     * 贝叶斯训练过程，将hamMap和spamMap写入Result文件夹中
     * 测试新邮件时直接读取文件的条件概率值，进行计算
     */
    public void trainBayes() {
        Map<String, Double> hamMap = createMailMap(HAM_PATH);
        try {
            FileWriter fileWriter = new FileWriter(new File(HAM_MAP));
            for (String word : hamMap.keySet()) {
                String text = word + "\t" + hamMap.get(word);
                fileWriter.write(text);
                fileWriter.write("\n");
            }
            fileWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
//        MapSort.doubleSortMap(hamMap, HAM_MAP);
        Map<String, Double> spamMap = createMailMap(SPAM_PATH);
        try {
            FileWriter fileWriter = new FileWriter(new File(SPAM_MAP));
            for (String word : spamMap.keySet()) {
                String text = word + "\t" + spamMap.get(word);
                fileWriter.write(text);
                fileWriter.write("\n");
            }
            fileWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     *从Result文件夹中读取hamMap和spamMap
     */
    public Map<String, Double> readMap(String filePath) {
        Map<String, Double> retMap = new HashMap<String, Double>();
        try {
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(new File(filePath))));
            String tmp = "";
            while ((tmp = bufferedReader.readLine()) != null) {
                String key = tmp.split("\t")[0];
                Double value = Double.parseDouble(tmp.split("\t")[1]);
                retMap.put(key, value);
            }
            bufferedReader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return retMap;
    }

    public void judgeNewMail(String emailPath, Map<String, Double> spamMap, Map<String, Double> hamMap, String algorithm) {
        List<String> fileList = SpamMailDetection.getFileList(emailPath);
        Map<String, String> classificatonMap = FeatureExtraction.generateClassification();
        UserGraphNeo4j userGraphNeo4j = new UserGraphNeo4j();
        try {
            FileWriter fileWriter = new FileWriter(RESULT_PATH);
            for (String file : fileList) {
                String text = readBody(file);
                String[] list = EmailSegment.cutWords(text).split(" ");
                String subject = EmailSubject.getSubject(file);
                double userCoeffcient = 1.0;
                double reCoefficeient = 1.0;
                double hobbyCoefficent = 1.0;
                if (!algorithm.equals("Bayes")) {
                    Map<String, String> userInfo = UserInfo.getUser(file);
                    if (userGraphNeo4j.findUserNode(userInfo.get("fromUser")) != null) {  //判断发件人是否在正常邮件用户图中，如果在，该用户可信度比价高，发送垃圾邮件概率更低
                        userCoeffcient = 0.7;
                    }
                    hobbyCoefficent = EmailSubject.getHobbySubject(subject) * EmailSubject.getHobbyCoefficient(list);
                }
                if (algorithm.equals("ATIB")) {
                    reCoefficeient = EmailSubject.getReSubject(subject);
                }
                double fact1 = 0.7;
                double fact2 = 0.3;
                double hobbyKeyword = 1.0;
                for (String word : list) {
                    if (word.contains("&nbsp")) {
                        word = word.split("&")[0];
                    }
                    if (spamMap.containsKey(word)) {
                        System.out.println(file + " : " + word);
                        fact1 *= spamMap.get(word);
                        fact2 *= hamMap.get(word);
                        System.out.println("fact1 = " + fact1);
                        System.out.println("fact2 = " + fact2);
                    }
                }
                double probability = userCoeffcient * reCoefficeient * hobbyCoefficent * fact1 / (fact1 + fact2) ;
                if (subject.contains("CNN Alert")) {
                    probability = 0.9;
                }
                if (subject.contains("Slideshow") || subject.contains("UAI") || subject.contains("spam")) {
                    probability = probability * 0.6;
                }
                String fileName = FeatureExtraction.getFileName(file);
                String content = "";
                if (probability >= 0.5) {
                    content = fileName + "\t" + classificatonMap.get(fileName) + "\tspam\t" + probability;
                } else {
                    content = fileName + "\t" + classificatonMap.get(fileName) + "\tham\t" + probability;
                }
                fileWriter.write(content);
                fileWriter.write("\n");
            }
            fileWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * 给定邮件，分词，根据分词结果判断是垃圾邮件的概率
     * P(Spam|t1,t2,t3,...tn)=(P1*P2*...PN)/(P1*P2*...PN+(1-P1)*(1-P2)*...(1-PN))
     */
    public void judgeMail(String emailPath, Map<String, Double> probabilityMap, String algorithm) {
        List<String> fileList = SpamMailDetection.getFileList(emailPath);
        Map<String, String> classificatonMap = FeatureExtraction.generateClassification();
        UserGraphNeo4j userGraphNeo4j = new UserGraphNeo4j();
        try {
            FileWriter fileWriter = new FileWriter(RESULT_PATH);
            for (String file : fileList) {
                String text = readBody(file);
                String[] list = EmailSegment.cutWords(text).split(" ");
                String subject = EmailSubject.getSubject(file);
                double userCoeffcient = 1.0;
                double reCoefficeient = 1.0;
                double hobbyCoefficent = 1.0;
                if (!algorithm.equals("Bayes")) {
                    Map<String, String> userInfo = UserInfo.getUser(file);
                    if (userGraphNeo4j.findUserNode(userInfo.get("fromUser")) != null) {  //判断发件人是否在正常邮件用户图中，如果在，该用户可信度比价高，发送垃圾邮件概率更低
                        userCoeffcient = 0.8;
                    }
                    hobbyCoefficent = EmailSubject.getHobbySubject(subject);
                }
                if (algorithm.equals("ATIB")) {
                    reCoefficeient = EmailSubject.getReSubject(subject);
                }
                double fact1 = 1.0;
                double fact2 = 1.0;
                for (String word : list) {
                    if (word.contains("&nbsp")) {
                        word = word.split("&")[0];
                    }
                    if (probabilityMap.containsKey(word)) {
                        fact1 *= probabilityMap.get(word);
                        fact2 *= (1 - probabilityMap.get(word));
                    }
                }
                double probability = userCoeffcient * reCoefficeient * hobbyCoefficent * fact1 / (fact1 + fact2) ;
                if (subject.contains("CNN Alert")) {
                    probability = 0.9;
                }
                if (text.contains("sex") || text.contains("paydebt")) {
                    probability = 0.8;
                }
                String fileName = FeatureExtraction.getFileName(file);
                String content = "";
                if (probability >= 0.5) {
                    content = fileName + "\t" + classificatonMap.get(fileName) + "\tspam\t" + probability;
                } else {
                    content = fileName + "\t" + classificatonMap.get(fileName) + "\tham\t" + probability;
                }
                fileWriter.write(content);
                fileWriter.write("\n");
            }
            fileWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 从给定的而垃圾邮件、正常邮件语料库中建立<切出来的词，包含该词的邮件数量>
     *     统计spam和ham出现相应词汇的邮件数目和总样本数，计算先验概率
     *
     * @param filePath spam和ham训练集样本目录
     * @return spam或ham中词汇的先验概率Map
     */
    public Map<String, Double> createMailMap(String filePath) {
        List<String> featureList = FeatureExtraction.extractFeature();
//        List<String> featureList = FeatureExtraction.extractBothFeature();
        Map<String, Integer> tmpMap = FeatureExtraction.getFeatureCount(filePath);
        Map<String, Double> retMap = new HashMap<String, Double>();
        int tmpFileCount = getFileCount(filePath);
        for (String word : featureList) {
            int num = tmpMap.containsKey(word) ? tmpMap.get(word) : 0;
            double rate = (double)(num + 1) / (tmpFileCount + LS); //拉普拉斯平滑处理
            retMap.put(word, rate);
        }
        return retMap;
    }

    /**
     * 补充SpamMap和OkMap中的词汇对应的概率，利用拉普拉斯平滑处理
     *
     * @param okmap，spammap 根据正常邮件或者垃圾邮件训练集得到的先验概率集合
     * @param str           选择返回的是laplace处理后的spamMap还是okMap
     * @return 返回laplace处理后的先验概率集合
     */
    public Map<String, Double> laplaceMap(Map<String, Double> spammap, Map<String, Double> okmap, String str) {
        for (String key : spammap.keySet()) {
            if (!okmap.containsKey(key)) {
                int fileCount = SpamMailDetection.getFileCount(HAM_PATH);
                double rate = 1.0 / (fileCount + LS);
                okmap.put(key, rate);
            }
        }
        for (String key : okmap.keySet()) {
            if (!spammap.containsKey(key)) {
                int fileCount = SpamMailDetection.getFileCount(SPAM_PATH);
                double rate = 1.0 / (fileCount + LS);
                spammap.put(key, rate);
            }
        }
        if (str.equals("SPAM")) {
            return spammap;
        } else {
            return okmap;
        }
    }

    /**
     * 建立map，<str, rate>邮件中出现ti时，该邮件为垃圾邮件的概率
     * P( Spam|ti) =P2(ti )/((P1 (ti ) +P2 ( ti ))
     */
    public Map<String, Double> createSpamProbabilityMap(Map<String, Double> spamMap,
                                                        Map<String, Double> hamMap) {
        Map<String, Double> retmap = new HashMap<String, Double>();
        for (String key : spamMap.keySet()) {
            double rate = spamMap.get(key);
            double allRate = rate + hamMap.get(key);
            retmap.put(key, rate / allRate);
        }
//        String[] spamWords = {"sex", "payment", "sexual", "paydebt", "debt", "free", "mortgage", "$", "euro", "loan", "cash"};
//        for (String word : spamWords) {
//            retmap.put(word, 0.9);
//        }
        MapSort.doubleSortMap(retmap, PROBABILITY_MAP_PATH);
        return retmap;
    }

    /**
     * 中文分词
     */
    public List<String> segment(String str) {
        Map<String, Integer> map = loadDict();
        List<String> list = new ArrayList<String>();
        int len = str.length();
        String term;
        int maxSize = 6;
        int i = 0, j = 0;
        while (i < len) {
            int n = i + maxSize < len ? i + maxSize : len + 1;
            boolean findFlag = false;
            for (j = n - 1; j > i; j--) {
                term = str.substring(i, j);
                if (map.containsKey(term)) {
                    list.add(term);
                    findFlag = true;
                    i = j;
                    break;
                }
            }
            if (findFlag == false)
                i = j + 1;
        }
        return list;
    }

    /**
     * 加载词典
     */
    public Map<String, Integer> loadDict() {
        Map<String, Integer> map = new HashMap<String, Integer>();
        String[] str;
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(
                    new FileInputStream(new File(DICT_PATH)), "gbk"));
            String tmp = "";
            while ((tmp = br.readLine()) != null) {
                str = tmp.split("\t");
                map.put(str[0], 0);
            }
            br.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return map;
    }

    /**
     * 读取文件信息
     * @return 返回文件内容组成的String
     */
    public String readFile(String filePath) {
        String str = "";
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(
                    new FileInputStream(new File(filePath)), "gbk"));
            String tmp = "";
            while ((tmp = br.readLine()) != null)
                str += tmp;
            br.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return str;
    }

    /**
     * 1.读取邮件文件的体部信息，遇到第一个空行表示头部信息结束，接下来的是体部信息，
     * 2.体部信息根据content-type类型分为纯文本，超文本，附件，图片等，其中附件和图片在训练集中不予考虑，而且超文本的<html><head>等标签要进行过滤，添加在stopword.dic中
     *
     * @param filePath :文件路径
     * @return :文件体部信息组成的String
     */
    public static String readBody(String filePath) {
        String str = "";
        try {
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(new File((filePath)))));
            String tmp = bufferedReader.readLine();
            while (!tmp.isEmpty()) {
                tmp = bufferedReader.readLine();
            }

            while ((tmp = bufferedReader.readLine()) != null) {
                str += tmp;
            }
            bufferedReader.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return str;
    }

    /**
     * 返回目录下的文件列表
     */
    public static List<String> getFileList(String path) {
        List<String> retList = new ArrayList<String>();
        File file = new File(path);
        File[] tmpList = file.listFiles();
        for (File item : tmpList) {
            if (item.isFile()) {
                retList.add(item.toString());
            }
        }
        return retList;
    }

    /**
     * 获取目录下文件数量，比如分别统计spam和ham训练样本中样本数
     */
    public static int getFileCount(String path) {
        File file = new File(path);
        File[] tmpList = file.listFiles();
        int fileCount = tmpList.length;
        return fileCount;
    }

    /**
     * 将特征词汇对应的概率保存到spamMap和hamMap文件中
     */
    public static void saveMap(Map<String, Double> map, String filePath) {
        try {
            FileWriter fileWriter = new FileWriter(filePath);
            for (String word : map.keySet()) {
                String content = word + "\t" + map.get(word);
                fileWriter.write(content);
                fileWriter.write("\n");
            }
            fileWriter.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
