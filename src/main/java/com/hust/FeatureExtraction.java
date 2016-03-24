package com.hust;

import java.io.*;
import java.util.*;

import static com.hust.SpamMailDetection.*;

public class FeatureExtraction {

    public static List<String> extractBothFeature() {
        List<String> featureList = new ArrayList<String>();
        Map<String, Double> featureMap = new HashMap<String, Double>();
        Map<String, Double> featureSpamMap = new HashMap<String, Double>();
        Map<String, Double> featureHamMap = new HashMap<String, Double>();
        Map<String, Integer> spamMap = getFeatureCount(SPAM_PATH);
        MapSort.intSortMap(spamMap, SPAM_WORD_MAP);
        Map<String, Integer> hamMap = getFeatureCount(HAM_PATH);
        MapSort.intSortMap(hamMap, HAM_WORD_MAP);
        int spamFileCount = getFileCount(SPAM_PATH);
        int hamFileCount = getFileCount(HAM_PATH);

        for (String key : spamMap.keySet()) {
            int A = spamMap.get(key);
            int B = hamMap.containsKey(key) ? hamMap.get(key) : 0;
            int C = spamFileCount - A;
            int D = hamFileCount - B;
            double value = ((double)((A*D - B*C)*(A*D - B*C)))/((A+B)*(C+D));
            featureSpamMap.put(key, value);
        }
        for (String key : hamMap.keySet()) {
            if (!spamMap.containsKey(key)) {
                int A = spamMap.containsKey(key) ? spamMap.get(key) : 0;
                int B = hamMap.get(key);
                int C = spamFileCount - A;
                int D = hamFileCount - B;
                double value = ((double)((A*D - B*C)*(A*D - B*C)))/((A+B)*(C+D));
                featureHamMap.put(key, value);
            }
        }
        List<Map.Entry<String, Double>> listSpam = new ArrayList<Map.Entry<String, Double>>();
        List<Map.Entry<String, Double>> listHam = new ArrayList<Map.Entry<String, Double>>();
        listSpam.addAll(featureSpamMap.entrySet());
        listHam.addAll(featureHamMap.entrySet());
        FeatureExtraction.ValueComparator valueComparator = new ValueComparator();
        Collections.sort(listSpam, valueComparator);
        Collections.sort(listHam, valueComparator);
        int i = 0;
        for (Map.Entry<String, Double> alist : listSpam) {
            if (i < FEATURE_NUM) {
                featureList.add(alist.getKey());
                i++;
            } else {
                break;
            }
        }
        int j = 0;
        for(Map.Entry<String, Double> alist : listHam) {
            if(j < FEATURE_NUM) {
                if (!featureList.contains(alist.getKey())) {
                    featureList.add(alist.getKey());
                    j++;
                }
            } else {
                break;
            }
        }
        return featureList;

    }

    /**
     * 利用卡方检验进行特征提取，筛选卡方统计值前m的特征词汇
     * 当前只处理spam训练集中的特征词汇
     * @return 返回spam训练集中特征词汇前m的词汇
     */
    public static List<String> extractFeature() {
        List<String> featureList = new ArrayList<String>();
        Map<String, Double> featureMap = new HashMap<String, Double>();
        Map<String, Integer> spamMap = getFeatureCount(SPAM_PATH);
        MapSort.intSortMap(spamMap, SPAM_WORD_MAP);
        Map<String, Integer> hamMap = getFeatureCount(HAM_PATH);
        MapSort.intSortMap(hamMap, HAM_WORD_MAP);
        int spamFileCount = getFileCount(SPAM_PATH);
        int hamFileCount = getFileCount(HAM_PATH);

        for (String key : spamMap.keySet()) {
            int A = spamMap.get(key);
            int B = hamMap.containsKey(key) ? hamMap.get(key) : 0;
            int C = spamFileCount - A;
            int D = hamFileCount - B;
            double value = ((double)((A*D - B*C)*(A*D - B*C)))/((A+B)*(C+D));
            featureMap.put(key, value);
        }
        for (String key : hamMap.keySet()) {
            if (!spamMap.containsKey(key)) {
                int A = spamMap.containsKey(key) ? spamMap.get(key) : 0;
                int B = hamMap.get(key);
                int C = spamFileCount - A;
                int D = hamFileCount - B;
                double value = ((double)((A*D - B*C)*(A*D - B*C)))/((A+B)*(C+D));
                featureMap.put(key, value);
            }
        }
        List<Map.Entry<String, Double>> list = new ArrayList<Map.Entry<String, Double>>();
        list.addAll(featureMap.entrySet());
        FeatureExtraction.ValueComparator valueComparator = new ValueComparator();
        Collections.sort(list, valueComparator);
        int i = 0;
        for (Map.Entry<String, Double> alist : list) {
            if (i < FEATURE_NUM) {
                featureList.add(alist.getKey());
                i++;
            } else {
                break;
            }
        }
        return featureList;

    }

    /**
     * 获取spam和ham训练集中的特征词汇，计算出现的次数
     * 内容中有很多包含"&nbsp"的词汇，分词器无法分离，"&nbsp"代表一个空格，需要额外处理，
     * 判断词汇是否包含"&nbsp"，是就去除，比如分词后得到的if&nbsp处理得到if
     * @param filePath Spam和ham训练集的目录
     * @return 返回spam和ham的特征词汇出现次数集合Map
     */
    public static Map<String, Integer> getFeatureCount(String filePath) {
        Map<String, Integer> tmpmap = new HashMap<String, Integer>();
        Map<String, Integer> retmap = new HashMap<String, Integer>();
        List<String> fileList = getFileList(filePath);
        for (String file : fileList) {
            String res = SpamMailDetection.readBody(file);
            String[] wordList = EmailSegment.cutWords(res).split(" ");
            List<String> handledWord = new ArrayList<String>();
            for (String word : wordList) {
                if (word.contains("&nbsp")) {
                    word = word.split("&")[0];
                }
                if (!handledWord.contains(word)) {
                    if(tmpmap.containsKey(word)) {
                        tmpmap.put(word, tmpmap.get(word) + 1);
                    } else {
                        tmpmap.put(word, 1);
                    }
                    handledWord.add(word);
                }
            }
        }
        for (String key : tmpmap.keySet()) {  //过滤掉出现次数少于5次的词汇
            if (tmpmap.get(key) > 5) {
                retmap.put(key, tmpmap.get(key));
            }
        }
        return retmap;
    }

    /**
     * 将训练集中的词汇出现次数Map写入文件
     */
    public static void writeIntoFile(Map<String, Integer> map, String filePath){
        try {
            FileWriter fileWriter = new FileWriter(new File(filePath));
            for (String word : map.keySet()) {
                String content = word + "\t" + map.get(word);
                fileWriter.write(content);
                fileWriter.write("\n");
            }
            fileWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 根据文件路径从index中得到该文件是spam还是ham
     */
    public static String getFileName(String file) {
        String[] fileList = file.split("\\\\");
        return fileList[fileList.length - 1];
    }

    /**
     * 根据index生成所有文件的spam和ham分类信息
     */
    public static Map<String, String> generateClassification() {
        Map<String, String> classificatonMap = new HashMap<String, String>();
        String index = "H:\\data\\ceas08-1\\ceas08-1\\full-immediate\\index";
        try {
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(new File(index))));
            String tmp = " ";
            while ((tmp = bufferedReader.readLine()) != null) {
                String key = tmp.substring(tmp.length() - 9);
                String value = tmp.substring(0, tmp.length() - 18);
                classificatonMap.put(key, value);
            }
            bufferedReader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return classificatonMap;
    }

    //实现Comparator借口，实现double排序结果从大到小
    private static class ValueComparator implements Comparator<Map.Entry<String,Double>>
    {
        public int compare(Map.Entry<String, Double> m, Map.Entry<String, Double> n)
        {
            if (n.getValue() - m.getValue() > 0) {
                return 1;
            } else if (n.getValue() - m.getValue() == 0) {
                return 0;
            } else {
                return -1;
            }
        }
    }
}
