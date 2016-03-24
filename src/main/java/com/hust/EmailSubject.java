package com.hust;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class EmailSubject {
    public static final double RE_COEFFICIENT = 0.6;
    public static final double HOBBY_COEFFICIENT = 0.8;

    /**
     * 判断主题有无RE,如果有则代表为回复邮件，极大可能是正常邮件，原先概率乘以一个回复概率系数
     *
     * @return 返回一个主题回复系数
     */
    public static double getReSubject(String subject) {
        double reCoefficient = 1.0;
        if (subject.contains("re:") || subject.contains("RE:") || subject.contains("Re:")) {
            reCoefficient = RE_COEFFICIENT;
        }
        if (subject.split(":").length == 1) {
            reCoefficient = 1.0;
        }
        return reCoefficient;
    }

    /**
     * 判断邮件内容有无用户兴趣爱好关键词，得到内容关键词因子
     */
    public static double getHobbyCoefficient(String[] list) {
        Map<String, String> hobbyMap = EmailSubject.getHobbyMap();
        double hobbyCoefficient = 1.0;
        for (String word : list) {
            if (hobbyMap.containsKey(word)) {
                hobbyCoefficient *= 0.8;
            }
        }
        return hobbyCoefficient;
    }

    /**
     * 判断主题有无用户爱好，职业相关关键词，有则有更多可能是正常邮件，得到爱好修正概率系数
     *
     * @return 返回一个爱好修正系数
     */
    public static double getHobbySubject(String subject) {
        Map<String, String> hobbyMap = EmailSubject.getHobbyMap();
        double hobbyCoefficient = 1.0;
        if (!subject.equals("")) {
            String[] subjectWord = EmailSegment.cutWords(subject).split(" ");
            for (String word : subjectWord) {
                if (hobbyMap.containsKey(word)) {
                    hobbyCoefficient *= HOBBY_COEFFICIENT;
                }
            }
        }
        return hobbyCoefficient;
    }

    /**
     * 根据邮件的subjuect域提取用户的兴趣
     */
    public static String getHobby(String filePath) {
        String subject = EmailSubject.getSubject(filePath).trim();
        String hobby = "other";
        if (!subject.equals("")) {
//            System.out.println(filePath);
            String[] subjectList = EmailSegment.cutWords(subject).split(" ");
            Map<String, String> hobbyMap = EmailSubject.getHobbyMap();
            for (String word : subjectList) {
                if (hobbyMap.containsKey(word)) {
                    hobby = hobbyMap.get(word);
                }
            }
        }
        return hobby;
    }

    /**
     * 生成用户兴趣或者职业的HashMap
     *
     * @return 返回用户兴趣或者职业的HashMap
     */
    public static Map<String, String> getHobbyMap() {
        Map<String, String> hobbyMap = new HashMap<String, String>();
        String[] language = {"python", "java", "perl", "php", "matlab"};
        String[] os = {"opensuse", "ubuntu", "fedora", "vista", "linux", "unix", "bsd", "gnu"};
        String[] data = {"weka", "spark", "database"};
        String[] tech = {"bug", "svn", "mysql", "oracle", "patch", "python-dev", "slideshow", "unbranded", "carrot",
                "uai", "curl", "bayesian", "bayes", "log", "error", "ldap", "prefork", "amazon.com", "jira",
                "webby", "vlan", "google", "pownceapi", "ipr", "triptracker"};
        String[] email = {"postfix", "spam", "ham", "spamexperts", "spamassasin", "spambayes", "ndr", "squirrelmail"};

        for (String item : language) {
            hobbyMap.put(item, "language");
        }
        for (String item : os) {
            hobbyMap.put(item, "os");
        }
        for (String item : data) {
            hobbyMap.put(item, "data");
        }
        for (String item : tech) {
            hobbyMap.put(item, "tech");
        }
        for (String item : email) {
            hobbyMap.put(item, "email");
        }
        return hobbyMap;
    }

    /**
     * 获取邮件的主题
     */
    public static String getSubject(String filePath) {
        String res = "";
        try {
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(new File(filePath))));
            String tmp = "";
            while ((tmp = bufferedReader.readLine()) != null) {
                if (tmp.startsWith("Subject:")) {
                    res = tmp.substring(8);
                }
            }
            bufferedReader.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return res;
    }
}
