package com.gitee.carloshuang.model;

import lombok.Data;

import java.util.Map;

/**
 * SQL语句参数.
 *
 * @author: Carlos Huang
 * @Date: 2020-8-22
 */
@Data
public class SqlParamModel {
    /** 需要参数数量. */
    private int size;
    /** 别名的索引. */
    private Map<Integer, String> aliasMap;
    /** 最终sql. */
    private String sql;
}
