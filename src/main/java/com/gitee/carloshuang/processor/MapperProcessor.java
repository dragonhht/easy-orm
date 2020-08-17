package com.gitee.carloshuang.processor;

import com.gitee.carloshuang.annotation.Mapper;
import org.apache.commons.collections4.CollectionUtils;
import org.reflections.Reflections;

import java.util.Set;

/**
 * Mapper注解处理器.
 *
 * @author: Carlos Huang
 * @Date: 2020-8-17
 */
public class MapperProcessor {

    private static final MapperProcessor PROCESSOR = new MapperProcessor();
    /** 包含该注解的类. */
    private Set<Class<?>> classSet;

    private MapperProcessor() {
        init();
    }

    /**
     * 初始化方法
     */
    private void init() {
        // 获取有包含Mapper注解的类
        Reflections f = new Reflections("");
        classSet = f.getTypesAnnotatedWith(Mapper.class);
    }

    /**
     * 处理带Mapper注解的接口
     */
    private void process() {
        if (CollectionUtils.isEmpty(classSet)) return;
        for (Class<?> aClass : classSet) {

        }
    }

    /**
     * 解析使用 Mapper注解标记的接口
     * @param aClass
     */
    private void parser(Class<?> aClass) {
        if (!aClass.isInterface()) return;
        // TODO 开始解析接口，由接口生成实现类代码
    }

}
