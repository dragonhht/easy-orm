package com.gitee.carloshuang.utils;

import com.gitee.carloshuang.model.JdbcConnectionModel;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import javax.sql.DataSource;

/**
 * 数据源工具类.
 *
 * @author: Carlos Huang
 * @Date: 2020-8-19
 */
public final class DataSourceUtils {

    /**
     * 创建数据源
     * @param jdbc JDBC连接信息
     * @return
     */
    public static DataSource createDataSource(JdbcConnectionModel jdbc) {
        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setJdbcUrl(jdbc.getUrl());
        hikariConfig.setDriverClassName(jdbc.getDriverClassName());
        hikariConfig.setUsername(jdbc.getUserName());
        hikariConfig.setPassword(jdbc.getPassword());
        return new HikariDataSource(hikariConfig);
    }

}
