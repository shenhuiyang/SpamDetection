package com.hust;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.neo4j.graphdb.*;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.graphdb.schema.IndexDefinition;
import org.neo4j.graphdb.schema.Schema;
import org.neo4j.graphdb.traversal.Evaluators;
import org.neo4j.graphdb.traversal.Traverser;
import org.neo4j.graphdb.traversal.TraversalDescription;
import org.neo4j.io.fs.FileUtils;

public class UserGraphNeo4j {
    private static final String DB_PATH = "D:\\Neo4j\\UserGraphDB\\";
    public static final Label userLabel = DynamicLabel.label("User");
    public static final Label fileLabel = DynamicLabel.label("File");

    GraphDatabaseService graphDb;
    Node head;
    Node fromNode;
    Node toNode;
    Node fileNode;
    Relationship relationship;
    long headNodeId;

    public UserGraphNeo4j() {
        try {
            setUp();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private static enum RelTypes implements RelationshipType {
        NEO_NODE, SEND, DELETE, RECOVER, SHARE, OWN
    }

    public static void main(String[] args) throws IOException {
        UserGraphNeo4j userGraphNeo4j = new UserGraphNeo4j();
//        userGraphNeo4j.setUp();
        userGraphNeo4j.addNode(SpamMailDetection.HAM_PATH);
        userGraphNeo4j.shutDown();
//        userGraphNeo4j.creatIndex();
    }

    public GraphDatabaseService getGraphDb() {
        return graphDb;
    }

    /**
     * 启动数据库
     */
    public void setUp() throws IOException {
//        FileUtils.deleteRecursively(new File(DB_PATH));
        graphDb = GraphDB.getInstance();
        registerShutdownHook(graphDb);
    }

    /**
     * 往graph中添加节点,添加之前先检查要添加的是否已经在graph中，如果在就不用新建，直接添加或修改属性和关系
     * 如果fromUser和toUser都在graph中，那么只需要修改发送邮件次数
     */
    void addNode(String filePath) throws IOException {
        List<String> fileList = SpamMailDetection.getFileList(filePath);
        Map<String, String> userMap = new HashMap<String, String>();
        Node tmp;
        try (Transaction tx = graphDb.beginTx()) {
            for (String file : fileList) {
                userMap = UserInfo.getUser(file);
                boolean flag = false;
                for (String item : userMap.keySet()) {
                    switch (item) {
                        case "toUser":
                            tmp = findUserNode(userMap.get(item));
                            if (tmp != null) {
                                toNode = tmp;
                                flag = true;
                            } else {
                                toNode = graphDb.createNode(userLabel);
                                toNode.setProperty("Addr", userMap.get(item));
                            }
                            break;
                        case "email":
                            fileNode = graphDb.createNode(fileLabel);
                            fileNode.setProperty("fileName", userMap.get(item));
                            toNode.createRelationshipTo(fileNode, RelTypes.OWN);
                            break;
                        case "fromUser":
                            tmp = findUserNode(userMap.get(item));
                            if (tmp != null) {     //表示fromUser已经存在graph中
                                fromNode = tmp;
                                if (flag && !fromNode.getProperty("Addr").equals(toNode.getProperty("Addr"))) {        //表示fromUser和toUser都在graph中，那么只需要修改发件人给收件人发送邮件的次数
                                    for (Relationship rel : fromNode.getRelationships(RelTypes.SEND)) {
                                        Node endNode = rel.getOtherNode(fromNode);
                                        if (endNode.getProperty("Addr").equals(toNode.getProperty("Addr"))) {
                                            int sendTime = (int) rel.getProperty("sendTime") + 1;
                                            rel.setProperty("sendTime", sendTime);
                                        }
                                    }
                                }
                            } else {
                                fromNode = graphDb.createNode(userLabel);
                                fromNode.setProperty("Addr", userMap.get(item));
                                relationship = fromNode.createRelationshipTo(toNode, RelTypes.SEND);
                                relationship.setProperty("sendTime", 1);
                            }
                            break;
                        case "hobby":
                            if (!userMap.get(item).equals("other")) {
                                fromNode.setProperty("hobby", userMap.get(item));
                                toNode.setProperty("hobby", userMap.get(item));
//                                relationship = fromNode.createRelationshipTo(toNode, RelTypes.SHARE);
//                                relationship.setProperty("hobby", userMap.get(item));
                            }
                            break;
                    }
                }
            }
            tx.success();
        }

    }

    /**
     * 计算两个用户节点之间的亲近度
     */
    public double calCloseness(String user1, String user2) {
        double closeness = 1.0;
        if (findUserNode(user1) != null && findUserNode(user2) != null) {
            Node fromNode = findUserNode(user1);
            Traverser friendIn3Hops = getIn3HopsFriends(fromNode);
            try (Transaction tx = graphDb.beginTx()) {
                for (Path friendPath : friendIn3Hops) {
                    if (friendPath.endNode().getProperty("Addr").equals(user2)) {
                        int depth = friendPath.length();
                        double tmp = Math.pow(1.3, depth);
                        closeness /= tmp;
                        break;
                    }
                }
                tx.success();
            }
        }
        return closeness;
    }

    /**
     * 获取3跳之内的朋友
     */
    public Traverser getIn3HopsFriends(Node startNode) {
        TraversalDescription td = graphDb.traversalDescription()
                .breadthFirst()
                .relationships(RelTypes.SEND, Direction.OUTGOING)
                .evaluator(Evaluators.excludeStartPosition())
                .evaluator(Evaluators.toDepth(3));
        return td.traverse(startNode);
    }

    /**
     * 获取3跳之内的朋友
     */
    public Traverser getAt3HopsFriends(Node startNode) {
        TraversalDescription td = graphDb.traversalDescription()
                .breadthFirst()
                .relationships(RelTypes.SEND, Direction.OUTGOING)
                .evaluator(Evaluators.excludeStartPosition())
                .evaluator(Evaluators.atDepth(2));
        return td.traverse(startNode);
    }


    /**
     * 根据邮箱名查找用户节点是否存在graph中
     */
    Node findUserNode(String addr) {
        Node node = null;
        Label userLabel = DynamicLabel.label("User");
        try (Transaction tx = graphDb.beginTx()) {
            node = graphDb.findNode(userLabel, "Addr", addr);
            tx.success();  //提交事务
        }
        return node;
    }

    /**
     * 创建user类型节点的对应Addr属性的索引index
     */
    void creatIndex() {
        IndexDefinition indexDefinition;
        try (Transaction tx = graphDb.beginTx()) {
            Schema schema = graphDb.schema();
            Iterable<IndexDefinition> indexList = schema.getIndexes(DynamicLabel.label("User"));
            indexDefinition = schema.indexFor(DynamicLabel.label("User"))
                    .on("Addr")
                    .create();
            tx.success();
        }

        try (Transaction tx = graphDb.beginTx()) {
            Schema schema = graphDb.schema();
            schema.awaitIndexOnline(indexDefinition, 10, TimeUnit.SECONDS);
        }
    }

    private Node getNeoNode() {
        return graphDb.getNodeById(headNodeId)
                .getSingleRelationship(RelTypes.NEO_NODE, Direction.OUTGOING)
                .getEndNode();
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
