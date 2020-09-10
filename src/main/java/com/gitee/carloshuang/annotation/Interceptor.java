package com.gitee.carloshuang.annotation;

import java.lang.annotation.*;

/**
 * 拦截器注解.
 *
 * @author: Carlos Huang
 * @Date: 2020-9-10
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface Interceptor {

    /** 全限定方法名. */
    String method() default "";
    /** 方法参数. */
    Class<?>[] args() default {};

}
