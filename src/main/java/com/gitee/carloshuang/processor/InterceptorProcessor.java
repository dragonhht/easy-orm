package com.gitee.carloshuang.processor;

import com.gitee.carloshuang.annotation.Interceptor;
import com.gitee.carloshuang.interceptor.HandlerInterceptor;
import com.gitee.carloshuang.storage.InterceptorContainer;
import com.gitee.carloshuang.utils.MethodIdAnnotationUtils;
import lombok.SneakyThrows;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
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
        new InterceptorProcessor().process();
    }

    /**
     * 加载并配置拦截器
     */
    public void process() {
        if (CollectionUtils.isEmpty(classSet)) return;
        // TODO 加载并配置拦截器
        for (Class<?> aClass : classSet) {
            initInterceptor(aClass);
        }
    }

    /**
     * 初始化拦截器
     * @param clazz
     */
    @SneakyThrows
    private void initInterceptor(Class<?> clazz) {
        Interceptor anno = clazz.getAnnotation(Interceptor.class);
        String method = anno.method();
        HandlerInterceptor interceptor = (HandlerInterceptor) clazz.newInstance();
        if (StringUtils.isNotEmpty(method)) {
            Class<?>[] argTypes = anno.args();
            String methodId = MethodIdAnnotationUtils.interceptorMethod(method, argTypes);
            InterceptorContainer.getInstance().putMethodInterceptor(methodId, interceptor);
            return;
        }
        InterceptorContainer.getInstance().addGlobalInterceptor(interceptor);
    }

}
