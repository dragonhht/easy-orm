package com.gitee.carloshuang.model;

import com.gitee.carloshuang.exception.CanNotCreateJdbcUrlException;
import com.gitee.carloshuang.exception.CanNotGetPlatformException;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;

import static com.gitee.carloshuang.constant.PlatformType.*;

/**
 * Jdbc连接信息.
 *
 * @author: Carlos Huang
 * @Date: 2020-8-18
 */
@Data
public class JdbcConnectionModel {

    private String host;
    private int port;
    private String dataBase;
    private String userName;
    private String password;
    private String url;
    private String driverClassName;
    private String platform;
    /** oracle 使用. */
    private String sid;

    public String getUrl() {
        if (StringUtils.isNotEmpty(this.url)) return url;
        switch (platform.toLowerCase()) {
            case MYSQL:
                return "jdbc:mysql://" + host + ":" + port + "/" + dataBase + "?useUnicode=true&characterEncoding=utf-8&useSSL=false&serverTimezone=UTC";
            case ORACLE:
                return "jdbc:oracle:thin:@" + host + ":" + port + ":" + sid;
            case SQL_SERVER:
                return "jdbc:microsoft:sqlserver://" + host + ":" + port + ";DatabaseName=" + dataBase;
            case POSTGRESQL:
                return "jdbc:postgresql://" + host + ":" + port + "/" + dataBase;
            default: throw new CanNotCreateJdbcUrlException();
        }
    }

    public String getPlatform() {
        if (StringUtils.isNotEmpty(platform)) return platform;
        if (StringUtils.isNotEmpty(url)) {
            platform = getPlatformFromUrl();
            return platform;
        }
        throw new CanNotGetPlatformException();
    }

    /**
     * 从url内获取数据库类型
     * @return
     */
    private String getPlatformFromUrl() {
        char[] chars = url.toCharArray();
        int start = 0, end = 0;
        for (int i = 0; i < chars.length; i++) {
            if (start == 0 && chars[i] == ':') {
                start = i;
                continue;
            }
            if (end == 0 && start != 0 && chars[i] == ':') {
                end = i;
                break;
            }
        }
        char[] result = Arrays.copyOfRange(chars, start + 1, end);
        String str =  new String(result).toLowerCase();
        if ("microsoft".equals(str)) {
            return SQL_SERVER;
        }
        return str;
    }
}
