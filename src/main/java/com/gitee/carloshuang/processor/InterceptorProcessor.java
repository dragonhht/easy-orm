package com.gitee.carloshuang.processor;

import com.gitee.carloshuang.annotation.Interceptor;
import org.apache.commons.collections4.CollectionUtils;
import org.reflections.Reflections;

import java.util.Set;

/**
 * 拦截器处理器.
 *
 * @author: Carlos Huang
 * @Date: 2020-9-10
 */
public class InterceptorProcessor {

    /** 包含该注解的类. */
    private Set<Class<?>> classSet;

    private InterceptorProcessor() {
        // 获取有包含Interceptor注解的类
        Reflections f = new Reflections("");
        classSet = f.getTypesAnnotatedWith(Interceptor.class);
    }

    /**
     * 初始化加载设置拦截器
     */
    public static void init() {
        new InterceptorProcessor();
    }

    /**
     * 加载并配置拦截器
     */
    public void process() {
        if (CollectionUtils.isEmpty(classSet)) return;
        // TODO 加载并配置拦截器
    }

}
