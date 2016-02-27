package com.hust;

import java.io.*;
import java.util.*;


public class SpamMailDetection {
    public static final String BASE_PATH = "H:\\Experiment\\Experiment4";
    public static final String SPAM_PATH = BASE_PATH + "\\Train\\spam";//spam训练集目录
    public static final String OK_PATH = BASE_PATH + "\\Train\\ham";//ham训练集目录
    public static final String EMAIL_PATH = BASE_PATH + "\\Test";//测试集目录
    public static final String RESULT_PATH = BASE_PATH + "\\Result\\result.txt";
    public static final String DICT_PATH = BASE_PATH + "\\dict.txt";//词典
    public static final String SPAM_MAP_PATH = BASE_PATH + "\\Result\\spamMap.txt";
    public static final String HAM_MAP_PATH = BASE_PATH + "\\Result\\hamMap.txt";
    public static int LS = 2; //laplace系数
    public static double PS = 0.7;
    public static double PH = 0.3;

    public static void main(String[] args) {
        long startTime = System.currentTimeMillis();
        SpamMailDetection smc = new SpamMailDetection();
        //<word,(word/NonSpamCorpus)>
        Map<String, Double> okmap = smc.createMailMap(OK_PATH);
        //<word,(word/SpamCorpus)>
        Map<String, Double> spammap = smc.createMailMap(SPAM_PATH);
        List<String> featureList = FeatureExtraction.extractFeature();
        Map<String, Double> hamTmpMap = smc.laplaceMap(spammap, okmap, "HAM");
        Map<String, Double> spamTmpMap = smc.laplaceMap(spammap, okmap, "SPAM");
        Map<String, Double> hamMap = new HashMap<String, Double>();
        Map<String, Double> spamMap = new HashMap<String, Double>();
        for (String word : featureList) {
            if (hamTmpMap.containsKey(word)) {
                hamMap.put(word, hamTmpMap.get(word));
            }
            if (spamTmpMap.containsKey(word)) {
                spamMap.put(word, spamTmpMap.get(word));
            }
        }
        SpamMailDetection.saveMap(hamMap, HAM_MAP_PATH);
        SpamMailDetection.saveMap(spamMap, SPAM_MAP_PATH);
        Map<String, Double> ratemap = smc.createSpamProbabilityMap(spammap, okmap);
        smc.judgeMail(EMAIL_PATH, spamMap, hamMap);
        long endTime = System.currentTimeMillis();
        System.out.println("time costs:" + (endTime - startTime) + "ms");

    }

    /**
     * 给定邮件，分词，根据分词结果判断是垃圾邮件的概率
     * P(Spam|t1,t2,t3,...tn)=(P1*P2*...PN)/(P1*P2*...PN+(1-P1)*(1-P2)*...(1-PN))
     */
    public void judgeMail(String emailPath, Map<String, Double> spamMap, Map<String, Double> hamMap) {
        List<String> fileList = SpamMailDetection.getFileList(emailPath);
        Map<String, String> classificatonMap = FeatureExtraction.generateClassification();
        try {
            FileWriter fileWriter = new FileWriter(RESULT_PATH);
            for (String file : fileList) {
                String[] list = EmailSegment.cutWords(readBody(file)).split(" ");
                String subject = EmailSubject.getSubject(file);
                double reCoefficeient = EmailSubject.getReSubject(subject);
                double hobbyCoefficent = EmailSubject.getHobbySubject(subject);
                double fact1 = 1.0;
                double fact2 = 1.0;
                for (String word : list) {
                    if (word.contains("&nbsp")) {
                        word = word.split("&")[0];
                    }
                    if (spamMap.containsKey(word) && hamMap.containsKey(word)) {
                        fact1 *= spamMap.get(word);
                        fact2 *= hamMap.get(word);
                    }
                }
                double probability = reCoefficeient * hobbyCoefficent * (fact1 * PS) / (fact1 * PS + fact2 * PH) ;
                String fileName = FeatureExtraction.getFileName(file);
                String content = "";
                if (probability > 0.5) {
                    content = fileName + "\t" + classificatonMap.get(fileName) + "\tspam\t" + probability;
                } else {
                    content = fileName + "\t" + classificatonMap.get(fileName) + "\tham\t" + probability;
                }
                fileWriter.write(content);
                fileWriter.write("\n");
            }
            fileWriter.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
//        List<String> list = segment(readFile(emailPath));
//        double rate = 1.0;
//        double tempRate = 1.0;
//        for (String str : list) {
//            if (ratemap.containsKey(str)) {
//                double tmp = ratemap.get(str);
//                tempRate *= 1 - tmp;
//                rate *= tmp;
//            }
//        }
//        return rate / (rate + tempRate);
    }

    /**
     * 从给定的而垃圾邮件、正常邮件语料库中建立<切出来的词，包含该词的邮件数量>
     *     统计spam和ham出现相应词汇的邮件数目和总样本数，计算先验概率
     *
     * @param filePath spam和ham训练集样本目录
     * @return spam或ham中词汇的先验概率Map
     */
    public Map<String, Double> createMailMap(String filePath) {
        Map<String, Integer> tmpmap = new HashMap<String, Integer>();
        Map<String, Double> retmap = new HashMap<String, Double>();
        List<String> fileList = getFileList(filePath);
        for (String file : fileList) {
            String res = SpamMailDetection.readBody(file);
            String[] wordList = EmailSegment.cutWords(res).split(" ");
            for (String word : wordList) {
                if (word.contains("&nbsp")) {
                    word = word.split("&")[0];
                }
                if (tmpmap.containsKey(word)) {
                    tmpmap.put(word, tmpmap.get(word) + 1);
                } else {
                    tmpmap.put(word, 1);
                }
            }
        }
        int fileCount = SpamMailDetection.getFileCount(filePath);
        double rate = 0.0;
        for (String key : tmpmap.keySet()) {
            rate = tmpmap.get(key) / (double) fileCount;
            retmap.put(key, rate);
        }
        return retmap;
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
                int fileCount = SpamMailDetection.getFileCount(OK_PATH);
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
    public Map<String, Double> createSpamProbabilityMap(Map<String, Double> spammap,
                                                        Map<String, Double> okmap) {
        Map<String, Double> retmap = new HashMap<String, Double>();
        for (String key : spammap.keySet()) {
            double rate = spammap.get(key);
            double allRate = rate;
            if (okmap.containsKey(key)) {
                allRate += okmap.get(key);
            }
            retmap.put(key, rate / allRate);
        }
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
