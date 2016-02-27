package com.hust;

import java.io.*;
import java.util.List;
import java.util.Map;

public class Corpus {
    public static void main(String[] args) {
        countkoi8Email();
    }

    public static void copyFile() {
        Map<String, String> classificatonMap = FeatureExtraction.generateClassification();
    }

    public static void countkoi8Email() {
        String spamPath = "H:\\data\\ceas08-1\\ceas08-1\\spam";
        String hamPath = "H:\\data\\ceas08-1\\ceas08-1\\ham";
        String koi8Path = "H:\\data\\ceas08-1\\ceas08-1\\koi8-rEmail";
        List<String> spamPathList = SpamMailDetection.getFileList(spamPath);
        List<String> hamPathList = SpamMailDetection.getFileList(hamPath);
        int count = 0;
        for (String item : hamPathList) {
            boolean flag = false;
            try {
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(new File(item))));
                String tmp = "";
                while ((tmp = bufferedReader.readLine()) != null) {
                    if (tmp.contains("koi8-r")) {
                        flag = true;
                        count++;
                        break;
                    }
                }
                bufferedReader.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (flag) {
                System.out.println(item);
//                File old = new File(item);
//                File newFile = new File(koi8Path + old.getName());
//                old.renameTo(newFile);
            }
        }
        System.out.println("koi8Email : " + count);
    }
}
