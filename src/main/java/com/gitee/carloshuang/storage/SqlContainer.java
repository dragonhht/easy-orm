package com.gitee.carloshuang.storage;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Sql语句容器.
 * 存有所有方法对应的sql
 *
 * @author: Carlos Huang
 * @Date: 2020-8-31
 */
public final class SqlContainer {
    private static SqlContainer CONTAINER;
    /** 存放sql的map, methodId -> sql语句. */
    private Map<String, String> sqlMap = new ConcurrentHashMap<>();

    public static SqlContainer getInstance() {
        if (CONTAINER == null) {
            synchronized (SqlContainer.class) {
                if (CONTAINER == null) {
                    CONTAINER = new SqlContainer();
                }
            }
        }
        return CONTAINER;
    }

    /**
     * 存放sql语句
     * @param methodId
     * @param sql
     */
    public void put(String methodId, String sql) {
        sqlMap.put(methodId, sql);
    }

    /**
     * 获取方法对应的sql.
     * @param methodId
     * @return
     */
    public String get(String methodId) {
        return sqlMap.get(methodId);
    }
}
