package com.gitee.carloshuang.model;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 方法传入的参数.
 *
 * @author: Carlos Huang
 * @Date: 2020-8-23
 */
@Data
@NoArgsConstructor
public class Param {
    /** 是否为数组、集合. */
    private boolean plural;
    /** 参数类型. */
    private Class<?> type;
    /** 别名. */
    private String alias;
    /** 参数名. */
    private String name;
    /** 获取方法. */
    private String way;
}
