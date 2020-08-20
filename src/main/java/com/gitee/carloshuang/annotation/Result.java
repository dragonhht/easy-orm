package com.gitee.carloshuang.annotation;

import java.lang.annotation.*;

/**
 * 查询结果字段与实体字段对应关系.
 *
 * @author: Carlos Huang
 * @Date: 2020-8-20
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface Result {
    /** sql返回结果字段名. */
    String column();
    /** 接收实体字段. */
    String property();
}
