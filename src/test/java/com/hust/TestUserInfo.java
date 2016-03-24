package com.hust;

import org.junit.Test;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.traversal.Traverser;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TestUserInfo {

    @Test
    public void testGetUser() {
        UserInfo userInfo = new UserInfo();
        String filePath = "H:\\Experiment\\Experiment3\\Train\\spam\\0005_0049";
        userInfo.getUser(filePath);
    }

    @Test
    public void testHashMap() {
        Map<String, String> map = new HashMap<String, String>();
        map.put("fromUser", "a@qq.com");
        map.put("toUser", "b@qq.com");
        map.put("fileName", "0005_0049");
        map.put("hobby", "os");
        for (String item : map.keySet()) {
            System.out.println(item);
        }
    }

    @Test
    public void testMapContain() {
        Map<String, Integer> map = new HashMap<String, Integer>();
        map.put("hello", 8);
        int res = map.containsKey("world") ? map.get("hello") : 9;
        System.out.println(res);
    }

    @Test
    public void testString() {
        String subject = "Subject:";
        if(subject.substring(8).equals("")) {
            System.out.println("true");
        } else {
            System.out.println("false");
        }
        System.out.println(subject.substring(8));
    }

    @Test
    public void testGetHobby() {
        List<String> fileList = SpamMailDetection.getFileList(SpamMailDetection.HAM_PATH);
        for (String file : fileList) {
            EmailSubject.getHobby(file);
        }
    }

    @Test
    public void testFindUser() {
        String addr = "wkilxloc@opensuse.org";
        UserGraphNeo4j userGraphNeo4j = new UserGraphNeo4j();
        if (userGraphNeo4j.findUserNode(addr) != null) {
            System.out.println("user found");
        } else {
            System.out.println("user is not existed in the graph now");
        }
    }

    @Test
    public void testGetAt3HopsFriend() {
        String addr = "ucgvwr_thdqz@bluestreak.net";
        UserGraphNeo4j userGraphNeo4j = new UserGraphNeo4j();
        Node fromUser = userGraphNeo4j.findUserNode(addr);
        long startTime = System.currentTimeMillis();
        Traverser friendAt3Hops = userGraphNeo4j.getAt3HopsFriends(fromUser);
        long endTime = System.currentTimeMillis();
        System.out.println("get friend at 2 hops in Neo4j costs:" + (endTime - startTime) + "ms");
    }

    @Test
    public void testCalCloseness() {
        List<String> fileList = SpamMailDetection.getFileList(SpamMailDetection.EMAIL_PATH);
        Map<String, String> userMap = new HashMap<String, String>();
        UserGraphNeo4j userGraphNeo4j = new UserGraphNeo4j();
        double startTime = System.currentTimeMillis();
        for (String file : fileList) {
            userMap = UserInfo.getUser(file);
            String fromUser = userMap.get("fromUser");
            String toUser = userMap.get("toUser");
            userGraphNeo4j.calCloseness(fromUser, toUser);
        }
        double endTime = System.currentTimeMillis();
        System.out.println("calculate closeness costs:" + (endTime - startTime) + "ms");
    }

}
