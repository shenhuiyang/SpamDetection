package com.hust;

import java.io.*;
import java.util.*;

import static com.hust.SpamMailDetection.*;

public class FeatureExtraction {

    public static int FEATURE_NUM = 2000;
    /**
     * 利用卡方检验进行特征提取，筛选卡方统计值前m的特征词汇
     * 当前只处理spam训练集中的特征词汇
     * @return 返回spam训练集中特征词汇前m的词汇
     */
    public static List<String> extractFeature() {
        List<String> featureList = new ArrayList<String>();
        Map<String, Double> featureMap = new HashMap<String, Double>();
        Map<String, Integer> spamMap = getFeatureCount(SPAM_PATH);
        Map<String, Integer> hamMap = getFeatureCount(OK_PATH);
        int spamFileCount = getFileCount(SPAM_PATH);
        int hamFileCount = getFileCount(OK_PATH);

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
        List<String> fileList = getFileList(filePath);
        for (String file : fileList) {
            String res = SpamMailDetection.readBody(file);
            String[] wordList = EmailSegment.cutWords(res).split(" ");
            for (String word : wordList) {
                if (word.contains("&nbsp")) {
                    word = word.split("&")[0];
                }
                if(tmpmap.containsKey(word)) {
                    tmpmap.put(word, tmpmap.get(word) + 1);
                } else {
                    tmpmap.put(word, 1);
                }
            }
        }
        return tmpmap;
    }

    /**
     * 根据文件路径从index中得到该文件是spam还是ham
     */
    public static String getFileName(String file) {
        String[] fileList = file.split("\\\\");
        String fileName = fileList[fileList.length - 1];
        return fileName;
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
        } catch (FileNotFoundException e) {
            e.printStackTrace();
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
