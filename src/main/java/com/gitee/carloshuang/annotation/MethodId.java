package com.gitee.carloshuang.annotation;

import java.lang.annotation.*;

/**
 * 标记方法Id.
 *
 * @author: Carlos Huang
 * @Date: 2020-9-08
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface MethodId {
    String id();
}
