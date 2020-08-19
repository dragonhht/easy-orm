package com.gitee.carloshuang.storage;

import com.gitee.carloshuang.constant.PlatformType;
import com.gitee.carloshuang.model.JdbcConnectionModel;
import com.gitee.carloshuang.utils.DataSourceUtils;
import org.apache.commons.lang3.StringUtils;

import javax.sql.DataSource;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 数据源持有.
 *
 * @author: Carlos Huang
 * @Date: 2020-8-19
 */
public class DataSourceHolder {
    private static DataSourceHolder HOLDER;

    private Map<String, DataSource> dataSourceMap = new ConcurrentHashMap<>();
    private String nowDataSource;

    private DataSourceHolder() {
        init();
    }

    public static DataSourceHolder getInstance() {
        if (HOLDER == null) {
            synchronized (DataSourceHolder.class) {
                if (HOLDER == null) {
                    HOLDER = new DataSourceHolder();
                }
            }
        }
        return HOLDER;
    }

    public void init() {
        // TODO 暂时测试使用
        JdbcConnectionModel jdbc = new JdbcConnectionModel();
        jdbc.setHost("localhost");
        jdbc.setPort(3306);
        jdbc.setUserName("root");
        jdbc.setPassword("1234");
        jdbc.setDataBase("test");
        jdbc.setDriverClassName("com.mysql.cj.jdbc.Driver");
        jdbc.setPlatform(PlatformType.MYSQL);

        String sql = "select * from user";
        DataSource dataSource = DataSourceUtils.createDataSource(jdbc);

        nowDataSource = "test_1";
        dataSourceMap.put(nowDataSource, dataSource);
    }

    public DataSource getDataSource() {
        if (StringUtils.isEmpty(nowDataSource)) throw new RuntimeException("数据源未初始化");
        return dataSourceMap.get(nowDataSource);
    }
}
