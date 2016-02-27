package com.hust;

import org.junit.Test;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;

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
    public void testFindUserNode() throws IOException {
        UserGraphNeo4j userGraphNeo4j = new UserGraphNeo4j();

        String filePath = "H:\\Experiment\\Experiment3\\Train\\spam\\0020_0947";
        String hamFile = "H:\\Experiment\\Experiment3\\Train\\ham\\0005_0786";
        Map<String, String> userMap = UserInfo.getUser(hamFile);
        String addr = "johvdvu-tqrpvc@aaas-alerts.org";
        userGraphNeo4j.setUp(filePath);
        userGraphNeo4j.addNode(userMap);
        Node res = userGraphNeo4j.findUserNode(addr);
        try(Transaction tx = userGraphNeo4j.graphDb.beginTx()) {
            System.out.println(res.getProperty("Addr"));
        }
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

}
