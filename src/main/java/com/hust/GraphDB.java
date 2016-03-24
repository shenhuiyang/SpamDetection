package com.hust;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;

/**
 * 采用单例模式新建graphDb实例，其他类要调用该实例时只需要获得UserGraphNeo4j的对象即可，再利用该对象调用相应的方法
 */
public class GraphDB {
    private static GraphDatabaseService single=null;
    private static final String DB_PATH = "D:\\Neo4j\\UserGraphDB\\";
    public static synchronized GraphDatabaseService getInstance() {
        if (single == null) {
            single = new GraphDatabaseFactory().newEmbeddedDatabase(DB_PATH);;
        }
        return single;
    }
}
