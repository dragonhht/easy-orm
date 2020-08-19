package com.gitee.carloshuang.storage;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.sql.Connection;

/**
 * 数据库连接持有.
 *
 * @author: Carlos Huang
 * @Date: 2020-8-19
 */
@Slf4j
public class ConnectionHolder {
    private static ConnectionHolder HOLDER;
    private ThreadLocal<Connection> connectionThreadLocal = new ThreadLocal<>();

    private ConnectionHolder() {
    }

    public static ConnectionHolder getInstance() {
        if (HOLDER == null) {
            synchronized (ConnectionHolder.class) {
                if (HOLDER == null) {
                    HOLDER = new ConnectionHolder();
                }
            }
        }
        return HOLDER;
    }

    /**
     * 获取数据库连接
     * @return
     */
    @SneakyThrows
    public Connection getConnection() {
        Connection connection = connectionThreadLocal.get();
        if (connection == null || connection.isClosed()) {
            connection = DataSourceHolder.getInstance().getDataSource().getConnection();
            connectionThreadLocal.set(connection);
        }
        return connection;
    }

    /**
     * 关闭数据库连接
     */
    @SneakyThrows
    public void closeConnection() {
        Connection connection = connectionThreadLocal.get();
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    }

}
