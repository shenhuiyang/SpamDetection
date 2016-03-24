package com.hust;

import java.sql.*;

public class SQLHelper {
    public static final String url = "jdbc:mysql://127.0.0.1/email?useSSL=false";
    public static final String name = "com.mysql.jdbc.Driver";
    public static final String user = "root";
    public static final String password = "123456";
    private static ResultSet resultSet = null;

    public static Connection conn = null;
    public static PreparedStatement pst = null;

    static  {
        try {
            Class.forName(name);//指定连接类型
            conn = DriverManager.getConnection(url, user, password);//获取连接
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void close() {
        try {
            conn.close();
            pst.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * 查询数据库操作
     */
    public static ResultSet queryData(String sql) {
        try {
            pst = conn.prepareStatement(sql);
            resultSet = pst.executeQuery();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return resultSet;
    }

    /**
     * 更新数据库操作，插入，删除，更新
     */
    public static void updateData(String sql, String[] parameters) {
        try {
            pst = conn.prepareStatement(sql);
            if (parameters != null) {
                for (int i = 0; i < parameters.length; i++) {
                    pst.setString(i+1, parameters[i]);
                }
            }
            pst.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
