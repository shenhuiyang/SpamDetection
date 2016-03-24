package com.hust;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class SqlData {
    public static void main(String[] args) {
        SqlData sqlData = new SqlData();
        sqlData.getFriendAt3Hops();
//        long startTime = System.currentTimeMillis();
//        sqlData.insertData(SpamMailDetection.HAM_PATH);
//        long endTime = System.currentTimeMillis();
//        System.out.println("insert 6000 records into mysql costs:" + (endTime - startTime) + "ms");
    }

    public void getFriendAt3Hops() {
        String sql = "select a.from_user as A,c.to_user as B from email_file a,email_file b,email_file c \n" +
                "where a.from_user='ucgvwr_thdqz@bluestreak.net' and a.to_user=b.from_user and b.to_user=c.from_user;";

        long startTime = System.currentTimeMillis();
        ResultSet resultSet = SQLHelper.queryData(sql);
//            while (resultSet.next()) {
//                String userA = resultSet.getString(1);
//                String userB = resultSet.getString(2);
//            }
        long endTime = System.currentTimeMillis();
        System.out.println("query friends at 3 hops costs:" + (endTime - startTime) + "ms");

    }

    public void getHobby() {
        String sql = "select * from metadata";
        try {
            ResultSet resultSet = SQLHelper.queryData(sql);
            while (resultSet.next()) {
                String user = resultSet.getString(1);
                String hobby = resultSet.getString(2);
                System.out.println(user);
                System.out.println(hobby);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void insertData(String filePath) {
        List<String> fileList = SpamMailDetection.getFileList(filePath);
        Map<String, String> userMap = new HashMap<String, String>();
        String sql = "insert into email_file(file_name,from_user,to_user,send_time) values(?,?,?,?)";
        try {
            for (String file : fileList) {
                userMap = UserInfo.getUser(file);
                String[] para = {userMap.get("email"), userMap.get("fromUser"), userMap.get("toUser"), "1"};
                SQLHelper.updateData(sql, para);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
