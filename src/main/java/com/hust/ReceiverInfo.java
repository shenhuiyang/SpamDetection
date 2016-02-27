package com.hust;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.*;

public class ReceiverInfo {

    static Map<String, Integer> hashmap = new HashMap<String, Integer>();

    public static void main(String[] args) {
        String receiver_file = "F:\\spam filter\\about dataset\\enron_dataset\\receiverSA.json";
        getMostRec(receiver_file);
        printRec();
    }

    //计算每个收件人出现的次数
    public static void getMostRec(String fileName) {
        String line = "";
        try {
            BufferedReader bufferedReader = new BufferedReader(new FileReader(fileName));
            line = bufferedReader.readLine();
            while (line != null) {
                if (hashmap.containsKey(line)) {
                    int count = hashmap.get(line);
                    count += 1;
                    hashmap.put(line, count);
                } else
                    hashmap.put(line, 1);
                line = bufferedReader.readLine();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //按照次数从多到少输出收件人的具体出现次数
    public static void printRec() {
        List<Map.Entry<String, Integer>> list = new ArrayList<Map.Entry<String, Integer>>();
        list.addAll(hashmap.entrySet());
        ReceiverInfo.ValueComparator valueComparator = new ValueComparator();
        Collections.sort(list, valueComparator);
        for (Map.Entry<String, Integer> aList : list) {
            System.out.println(aList);
        }

    }

    //实现Comparator接口，实现排序结果是从大到小
    private static class ValueComparator implements Comparator<Map.Entry<String,Integer>>
    {
        public int compare(Map.Entry<String,Integer> m,Map.Entry<String,Integer> n)
        {
            return n.getValue()-m.getValue();
        }
    }


}
