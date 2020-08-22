package com.gitee.carloshuang.storage;

import com.gitee.carloshuang.model.QueryResultType;
import com.gitee.carloshuang.model.ResultFieldMessage;

import java.util.HashMap;
import java.util.Map;

/**
 * Query方法结果字段映射持有.
 *
 * @author: Carlos Huang
 * @Date: 2020-8-20
 */
public class QueryResultHolder {
    private static QueryResultHolder HOLDER;
    /** 存放方法查询结果字段与返回实体字段的关系. */
    private static final Map<String, Map<String, ResultFieldMessage>> map = new HashMap<>();
    /** 存放方法返回类型. */
    private static final Map<String, QueryResultType> typeMap = new HashMap<>();

    private QueryResultHolder() {}

    public static QueryResultHolder getInstance() {
        if (HOLDER == null) {
            synchronized (QueryResultHolder.class) {
                if (HOLDER == null) {
                    HOLDER = new QueryResultHolder();
                }
            }
        }
        return HOLDER;
    }

    /**
     * 保存方法查询字段映射关系
     * @param id 方法id
     * @param resultMap 字段映射关系
     */
    public void putFieldMap(String id, Map<String, ResultFieldMessage> resultMap) {
        map.put(id, resultMap);
    }

    /**
     * 获取方法查询字段映射关系
     * @param id 方法id
     * @return 字段映射关系
     */
    public Map<String, ResultFieldMessage> getResultMap(String id) {
        return map.get(id);
    }

    /**
     * 保存方法返回类型
     * @param id
     * @param type
     */
    public void putResultType(String id, QueryResultType type) {
        typeMap.put(id, type);
    }

    /**
     * 获取方法返回类型
     * @param id
     * @return
     */
    public QueryResultType getResultType(String id) {
        return typeMap.get(id);
    }
}
