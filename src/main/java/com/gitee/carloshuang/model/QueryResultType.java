package com.gitee.carloshuang.model;

import lombok.Data;

/**
 * 查询方法返回类型详情.
 *
 * @author: Carlos Huang
 * @Date: 2020-8-22
 */
@Data
public class QueryResultType {
    /** 返回类型. */
    private Class<?> returnType;
    /** 泛型子类型. */
    private Class<?>[] subType;
    /** 是否为数组. */
    private boolean array;
    /** 是否为基本类型. */
    private boolean primitive;
    /** 是否为集合类型(Collection子类). */
    private boolean collection;
    // 是否为Map. */
    private boolean map;
    /** 子类型. */
    private QueryResultType subResultType;
}
