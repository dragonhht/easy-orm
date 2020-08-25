package com.gitee.carloshuang.model;

import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * 方法与sql对应的参数.
 *
 * @author: Carlos Huang
 * @Date: 2020-8-25
 */
@Data
public class MethodSqlParam {
    /** 方法参数列表. */
    private List<Param> params;
    /** 方法别名参数. */
    private Map<String, Param> aliasMap;
}
