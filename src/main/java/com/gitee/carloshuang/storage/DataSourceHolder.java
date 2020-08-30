package com.gitee.carloshuang.storage;

import com.gitee.carloshuang.constant.PlatformType;
import com.gitee.carloshuang.exception.CanNotEmptyJdbcFlageException;
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

    /**
     * 添加新数据源
     * @param jdbc
     */
    public DataSource add(JdbcConnectionModel jdbc) {
        if (StringUtils.isEmpty(jdbc.getId())) throw new CanNotEmptyJdbcFlageException();
        DataSource dataSource = DataSourceUtils.createDataSource(jdbc);
        String id = jdbc.getId();
        dataSourceMap.put(id, dataSource);
        return dataSource;
    }

    /**
     * 设置当前数据源
     * @param key
     */
    public void setNowDataSource(String key) {
        nowDataSource = key;
    }

    /**
     * 获取当前数据源
     * @return
     */
    public DataSource getDataSource() {
        if (StringUtils.isEmpty(nowDataSource)) throw new RuntimeException("数据源未初始化");
        return dataSourceMap.get(nowDataSource);
    }
}
