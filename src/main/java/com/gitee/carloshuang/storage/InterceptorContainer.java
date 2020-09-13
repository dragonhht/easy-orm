package com.gitee.carloshuang.storage;

import com.gitee.carloshuang.interceptor.HandlerInterceptor;

import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 拦截器容器.
 *
 * @author: Carlos Huang
 * @Date: 2020-9-10
 */
public class InterceptorContainer {

    private static InterceptorContainer CONTAINER;
    /** 指定了方法的拦截器. */
    private Map<String, HandlerInterceptor> methodInterceptorMap = new ConcurrentHashMap<>();
    /** 全局拦截器. */
    private List<HandlerInterceptor> globalInterceptors = new Vector<>();

    public static InterceptorContainer getInstance() {
        if (CONTAINER == null) {
            synchronized (InterceptorContainer.class) {
                if (CONTAINER == null) {
                    CONTAINER = new InterceptorContainer();
                }
            }
        }
        return CONTAINER;
    }

    /**
     * 保存方法拦截器
     * @param methodId
     * @param interceptor
     */
    public void putMethodInterceptor(String methodId, HandlerInterceptor interceptor) {
        methodInterceptorMap.put(methodId, interceptor);
    }

    /**
     * 获取方法拦截器
     * @param methodId
     * @return
     */
    public HandlerInterceptor getMethodInterceptor(String methodId) {
        return methodInterceptorMap.get(methodId);
    }

    /**
     * 添加全局拦截器
     * @param interceptor
     */
    public void addGlobalInterceptor(HandlerInterceptor interceptor) {
        globalInterceptors.add(interceptor);
    }

}
