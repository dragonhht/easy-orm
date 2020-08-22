package com.gitee.carloshuang.annotation;

import java.lang.annotation.*;

/**
 * 用于对应sql中的参数.
 *
 * @author: Carlos Huang
 * @Date: 2020-8-22
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.PARAMETER})
public @interface Param {
    String value();
}
