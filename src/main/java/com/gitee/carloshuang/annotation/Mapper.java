package com.gitee.carloshuang.annotation;

import java.lang.annotation.*;

/**
 * 标记接口为数据库操作.
 *
 * @author: Carlos Huang
 * @Date: 2020-8-17
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface Mapper {
}
