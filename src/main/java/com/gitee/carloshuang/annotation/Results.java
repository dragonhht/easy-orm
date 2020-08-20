package com.gitee.carloshuang.annotation;

import java.lang.annotation.*;

/**
 * 查询结果与实体字段映射关系.
 *
 * @author: Carlos Huang
 * @Date: 2020-8-20
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface Results {
    String id();

    Result[] value();
}
