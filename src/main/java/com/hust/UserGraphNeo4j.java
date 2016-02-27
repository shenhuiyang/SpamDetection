package com.hust;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.neo4j.graphdb.*;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.graphdb.schema.IndexDefinition;
import org.neo4j.graphdb.schema.Schema;
import org.neo4j.io.fs.FileUtils;

public class UserGraphNeo4j {
    private static final String DB_PATH = "D:\\Neo4j\\UserGraphDB\\";
    public static final Label userLabel = DynamicLabel.label("User");
    public static final Label fileLabel = DynamicLabel.label("File");

    GraphDatabaseService graphDb;
    Node head;
    Node firstNode;
    Node secondNode;
    Node fileNode;
    Relationship relationship;
    long headNodeId;

    private static enum RelTypes implements RelationshipType {
        NEO_NODE, SEND, DELETE, RECOVER, SHARE, OWN
    }

    public static void main(String[] args) throws IOException {
        UserGraphNeo4j userGraphNeo4j = new UserGraphNeo4j();
        String filePath = "H:\\Experiment\\Experiment3\\Train\\spam\\0020_0947";
        String hamFile = "H:\\Experiment\\Experiment3\\Train\\spam\\0005_0049";
        Map<String, String> userMap = UserInfo.getUser(hamFile);
        String addr = "young@iworld.de";
        userGraphNeo4j.setUp(filePath);
        userGraphNeo4j.addNode(userMap);
        Node res = userGraphNeo4j.findUserNode(addr);
        System.out.println(res);
        userGraphNeo4j.shutDown();
    }

    /**
     * 根据邮件的路径得到的用户关系HashMap创建关系图
     */
    public void setUp(String filePath) throws IOException{
        FileUtils.deleteRecursively(new File(DB_PATH));
        graphDb = new GraphDatabaseFactory().newEmbeddedDatabase(DB_PATH);
        registerShutdownHook(graphDb);
        creatIndex();
        Map<String, String> userMap = UserInfo.getUser(filePath);
        createNodespace(userMap);
    }

    /**
     * 根据UserInfo.getUser(filePath)返回的HashMap构建用户关系图
     * Map中的fromUser，toUser，fileName创建对应节点（用户节点和文件节点），
     * hobby字段作为toUser的属性
     * 创建fromUser到toUser的发送关系，toUser到fileName的拥有关系
     * 设置一个headNode，作为graph的起点，通过NEO_NODE关系指向第一个节点，类似于链表的head指针
     * @throws IOException
     */
    void createNodespace(Map<String, String> userMap) throws IOException {
        // START SNIPPET: transaction
        try (Transaction tx = graphDb.beginTx()) {
            // Database operations go here
            head = graphDb.createNode();
            headNodeId = head.getId();

            for(String item : userMap.keySet()) {  //hashMap中的hash顺序依次是toUser，fileName，fromUser，hobby
                switch (item) {
                    case "toUser":
                        secondNode = graphDb.createNode(userLabel);
                        secondNode.setProperty("Addr", userMap.get(item));
                        break;
                    case "fileName":
                        fileNode = graphDb.createNode(fileLabel);
                        fileNode.setProperty("fileName",userMap.get(item));
                        secondNode.createRelationshipTo(fileNode,RelTypes.OWN);
                        break;
                    case "fromUser":
                        firstNode = graphDb.createNode(userLabel);
                        firstNode.setProperty("Addr",userMap.get(item));
                        head.createRelationshipTo(firstNode, RelTypes.NEO_NODE);
                        relationship = firstNode.createRelationshipTo(secondNode, RelTypes.SEND);
                        int sendTime = 1;
                        relationship.setProperty("sendTime", sendTime);
                        break;
                    case "hobby":
                        firstNode.setProperty("hobby", userMap.get(item));
                        secondNode.setProperty("hobby", userMap.get(item));
                        firstNode.createRelationshipTo(secondNode,RelTypes.SHARE);
                        break;
                }
            }
            tx.success();
        }
    }

    /**
     * 往graph中添加节点,添加之前先检查要添加的是否已经在graph中，如果在就不用新建，直接添加或修改属性和关系
     * 如果fromUser和toUser都在graph中，那么只需要修改发送邮件次数
     */
    void addNode(Map<String, String> userMap) throws IOException{
        boolean flag = false;
        Node tmp;
        try (Transaction tx = graphDb.beginTx()){
            for (String item : userMap.keySet()) {
                switch (item) {
                    case "toUser":
                        tmp = findUserNode(userMap.get(item));
                        if (tmp != null) {
                            secondNode = tmp;
                            flag = true;
                        } else {
                            secondNode = graphDb.createNode(userLabel);
                            secondNode.setProperty("Addr", userMap.get(item));
                        }
                        break;
                    case "fileName":
                        fileNode = graphDb.createNode(fileLabel);
                        fileNode.setProperty("fileName",userMap.get(item));
                        secondNode.createRelationshipTo(fileNode, RelTypes.OWN);
                        break;
                    case "fromUser":
                        System.out.println(userMap.get(item));
                        tmp = findUserNode(userMap.get(item));
                        if (tmp != null) {     //表示fromUser已经存在graph中
                            firstNode = tmp;
                            if (flag) {        //表示fromUser和toUser都在graph中，那么只需要修改发件人给收件人发送邮件的次数
                                relationship = firstNode.getSingleRelationship(RelTypes.SEND,Direction.OUTGOING);
                                int sendTime = (int)relationship.getProperty("sendTime") + 1;
                                relationship.setProperty("sendTime",sendTime);
                            }
                        } else {
                            firstNode = graphDb.createNode(userLabel);
                            firstNode.setProperty("Addr", userMap.get(item));
                            relationship = firstNode.createRelationshipTo(secondNode, RelTypes.SEND);
                            relationship.setProperty("sendTime", 1);
                        }
                        break;
                    case "hobby":
                        firstNode.setProperty("hobby", userMap.get(item));
                        secondNode.setProperty("hobby", userMap.get(item));
                        firstNode.createRelationshipTo(secondNode,RelTypes.SHARE);
                        break;
                }
            }
        }

    }

    private Node getNeoNode() {
        return graphDb.getNodeById(headNodeId)
                .getSingleRelationship(RelTypes.NEO_NODE, Direction.OUTGOING)
                .getEndNode();
    }

    /**
     * 根据邮箱名查找用户节点是否存在graph中
     */
    Node findUserNode(String addr) {
        Node node = null;
        Label userLabel = DynamicLabel.label("User");
        try (Transaction tx = graphDb.beginTx()){
            node = graphDb.findNode(userLabel,"Addr",addr);
            return node;
        }
    }

    /**
     * 创建user类型节点的对应Addr属性的索引index
     */
    void creatIndex() {
        // START SNIPPET: createIndex
        IndexDefinition indexDefinition;
        try ( Transaction tx = graphDb.beginTx() )
        {
            Schema schema = graphDb.schema();
            indexDefinition = schema.indexFor( DynamicLabel.label( "User" ) )
                    .on( "Addr" )
                    .create();
            tx.success();
        }
        // END SNIPPET: createIndex
        // START SNIPPET: wait
        try ( Transaction tx = graphDb.beginTx() )
        {
            Schema schema = graphDb.schema();
            schema.awaitIndexOnline( indexDefinition, 10, TimeUnit.SECONDS );
        }
        // END SNIPPET: wait
    }

    void shutDown() {
        System.out.println();
        System.out.println("Shutting down database ...");
        // START SNIPPET: shutdownServer
        graphDb.shutdown();
        // END SNIPPET: shutdownServer
    }

    private static void registerShutdownHook(final GraphDatabaseService graphDb) {
        // Registers a shutdown hook for the Neo4j instance so that it
        // shuts down nicely when the VM exits (even if you "Ctrl-C" the
        // running application).
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                graphDb.shutdown();
            }
        });
    }
}
