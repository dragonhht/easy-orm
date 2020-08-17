package com.gitee.carloshuang.annotation;

import java.lang.annotation.*;

/**
 * 数据库新增操作注解.
 *
 * @author: Carlos Huang
 * @Date: 2020-8-17
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface Insert {
    String value();
}
