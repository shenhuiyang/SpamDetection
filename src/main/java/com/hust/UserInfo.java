package com.hust;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class UserInfo {

    /**
     * 从邮件的From域和To域提取发件人和收件人用户，Subject域提取职业或者兴趣属性
     * 用户先进行分词处理，提取包含@的字段，即为邮箱地址
     * @return 返回邮件的发件人，收件人，邮件名，收件人爱好信息的HashMap
     */
    public static Map<String, String> getUser(String filePath) {
        Map<String, String> userMap = new HashMap<String, String>();
        String fromUser = "";
        String toUser = "";
        String hobby = EmailSubject.getHobby(filePath);
        try {
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(new File(filePath))));
            String tmp = bufferedReader.readLine();
            while (!tmp.isEmpty()) {
                if (tmp.startsWith("From:")) {
                    String[] fromList = EmailSegment.cutWords(tmp).split(" ");
                    for (String item : fromList) {
                        if (item.contains("@")) {
                            fromUser = item;
                        }
                    }
                }
                if (tmp.startsWith("To:")) {
                    String[] toList = EmailSegment.cutWords(tmp).split(" ");
                    for (String item : toList) {
                        if (item.contains("@")) {
                            toUser = item;
                        }
                    }
                }
                tmp = bufferedReader.readLine();
            }
            bufferedReader.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        userMap.put("fromUser", fromUser);
        userMap.put("toUser", toUser);
        userMap.put("email", new File(filePath).getName());
        userMap.put("Hobby", hobby);
        return userMap;
    }
}
