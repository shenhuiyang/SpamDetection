package com.hust;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class MapSort {

    /**
     * HashMap按照value值进行排序,int
     */
    public static void intSortMap(Map<String,Integer> map, String filePath) {
        Map<String, Integer> retMap = new HashMap<String, Integer>();
        List<Map.Entry<String, Integer>> list = new ArrayList<Map.Entry<String, Integer>>();
        list.addAll(map.entrySet());
        ValueComparator valueComparator = new ValueComparator();
        Collections.sort(list, valueComparator);
        try {
            FileWriter fileWriter = new FileWriter(new File(filePath));
            for (Map.Entry<String, Integer> alist : list) {
                String key = alist.getKey();
                int value = map.get(key);
                String content = key + "\t" + value;
                fileWriter.write(content);
                fileWriter.write("\n");
            }
            fileWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * HashMap按照value值进行排序,value为double
     */
    public static void doubleSortMap(Map<String,Double> map,String filePath) {
        Map<String, Double> retMap = new HashMap<String, Double>();
        List<Map.Entry<String, Double>> list = new ArrayList<Map.Entry<String, Double>>();
        list.addAll(map.entrySet());
        DoubleValueComparator doubleValueComparator = new DoubleValueComparator();
        Collections.sort(list, doubleValueComparator);
        try {
            FileWriter fileWriter = new FileWriter(new File(filePath));
            for (Map.Entry<String, Double> alist : list) {
                String key = alist.getKey();
                double value = map.get(key);
                String content = key + "\t" + value;
                fileWriter.write(content);
                fileWriter.write("\n");
            }
            fileWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    //实现Comparator接口，实现int排序结果从大到小
    private static class ValueComparator implements Comparator<Map.Entry<String,Integer>>
    {
        public int compare(Map.Entry<String, Integer> m, Map.Entry<String, Integer> n)
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

    //实现Comparator借口，实现double排序结果从大到小
    private static class DoubleValueComparator implements Comparator<Map.Entry<String,Double>>
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
