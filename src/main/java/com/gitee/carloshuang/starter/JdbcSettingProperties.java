package com.gitee.carloshuang.starter;

import com.gitee.carloshuang.exception.CanNotEmptyJdbcFlageException;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 数据库信息配置类.
 *
 * @author: Carlos Huang
 * @Date: 2020-8-30
 */
@Data
@ConfigurationProperties(prefix = "easy-orm")
public class JdbcSettingProperties {

    /** 数据源标识. */
    private String id;
    private String host;
    private int port;
    private String dataBase;
    private String userName;
    private String password;
    private String url;
    private String driverClassName;
    private String platform;
    private String sid;

    public String getId() {
        if (StringUtils.isEmpty(id)) throw new CanNotEmptyJdbcFlageException();
        return id;
    }
}
