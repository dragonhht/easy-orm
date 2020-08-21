package com.gitee.carloshuang.storage;

import java.util.HashMap;
import java.util.Map;

/**
 * 存储所有 Mapper 标记的接口的实现类.
 *
 * @author: Carlos Huang
 * @Date: 2020-8-17
 */
public final class MapperInstanceStorage {
    private static final MapperInstanceStorage STORAGE = new MapperInstanceStorage();
    /** 保存Mapper接口与实现类实例的关系. */
    private static final Map<Class<?>, Object> map = new HashMap<>();

    private MapperInstanceStorage() {
    }

    public static MapperInstanceStorage getInstance() {
        return STORAGE;
    }

    /**
     * 保存 Mapper 接口与实现类实例的映射
     * @param mapperInterface
     * @param instance
     */
    public void put(Class<?> mapperInterface, Object instance) {
        map.put(mapperInterface, instance);
    }

    /**
     * 获取 Mapper 标记的接口对应的实例
     * @param mapperInterface
     * @return
     */
    public <T> T get(Class<T> mapperInterface) {
        return (T) map.get(mapperInterface);
    }
}
