package com.hust;

import java.io.*;
import java.util.List;
import java.util.Map;
import org.apache.commons.io.FileUtils;

import static com.hust.SpamMailDetection.*;

public class Corpus {
    public static void main(String[] args) {
        copyMisjudgeFile();
    }

    public static void copyMisjudgeFile() {
        try {
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(new File(RESULT_PATH))));
            String tmp = "";
            while ((tmp = bufferedReader.readLine()) != null) {
                String[] content = tmp.split("\t");
                if (!content[1].equals(content[2])) {
                    String srcFile = EMAIL_PATH + "\\" + content[0];
                    if (content[1].equals("ham")) {
                        FileUtils.copyFileToDirectory(new File(srcFile), new File(MISJUDGE_HAM));
                    } else {
                        FileUtils.copyFileToDirectory(new File(srcFile), new File(MISJUDGE_SPAM));
                    }
                }
            }
            bufferedReader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
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
