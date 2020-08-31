package com.gitee.carloshuang.starter;

import com.gitee.carloshuang.model.JdbcConnectionModel;
import com.gitee.carloshuang.processor.MapperProcessor;
import com.gitee.carloshuang.storage.DataSourceHolder;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

/**
 * 数据库信息自动装配类.
 *
 * @author: Carlos Huang
 * @Date: 2020-8-30
 */
@EnableConfigurationProperties(JdbcSettingProperties.class)
@Configuration
public class JdbcAutoConfiguration {

    /**
     * 初始化数据源及easy-orm
     * @param properties
     * @return
     */
    @Bean
    public DataSource initDataSource(JdbcSettingProperties properties) {
        // 允许不填写配置，则表示用户后面自动创建
        if (isNull(properties)) {
            MapperProcessor.init();
            return null;
        }
        JdbcConnectionModel jdbc = convertPropertiesToModel(properties);
        DataSource dataSource = DataSourceHolder.getInstance().add(jdbc);
        DataSourceHolder.getInstance().setNowDataSource(jdbc.getId());
        MapperProcessor.init();
        return dataSource;
    }

    /**
     * 判断是否配置数据源
     * @param properties
     * @return
     */
    private boolean isNull(JdbcSettingProperties properties) {
        return StringUtils.isEmpty(properties.getDriverClassName()) && StringUtils.isEmpty(properties.getUrl())
                && StringUtils.isEmpty(properties.getUserName()) && StringUtils.isEmpty(properties.getPassword());
    }

    /**
     * 将配置信息类转换成连接模型
     * @param properties
     * @return
     */
    private JdbcConnectionModel convertPropertiesToModel(JdbcSettingProperties properties) {
        JdbcConnectionModel model = new JdbcConnectionModel();
        model.setPassword(properties.getPassword());
        model.setUserName(properties.getUserName());
        model.setPlatform(properties.getPlatform());
        model.setDriverClassName(properties.getDriverClassName());
        model.setDataBase(properties.getDataBase());
        model.setPort(properties.getPort());
        model.setHost(properties.getHost());
        model.setUrl(properties.getUrl());
        model.setId(properties.getId());
        model.setSid(properties.getSid());
        return model;
    }

}
