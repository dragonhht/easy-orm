package com.gitee.carloshuang;

import com.gitee.carloshuang.constant.PlatformType;
import com.gitee.carloshuang.model.JdbcConnectionModel;

import java.util.List;
import java.util.Map;

/**
 * .
 *
 * @author: Carlos Huang
 * @Date: 2020-8-22
 */
public class Te {

    public List<Map<String, String>> get() {
        return null;
    }

    public List<String[]> ss() {
        JdbcConnectionModel jdbc = new JdbcConnectionModel();
        jdbc.setHost("localhost");
        jdbc.setPort(3306);
        jdbc.setUserName("root");
        jdbc.setPassword("1234");
        jdbc.setDataBase("test");
        jdbc.setDriverClassName("com.mysql.cj.jdbc.Driver");
        jdbc.setPlatform(PlatformType.MYSQL);
        return null;
    }

}
