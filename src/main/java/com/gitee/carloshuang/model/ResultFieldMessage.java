package com.gitee.carloshuang.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * 查询结果字段映射，字段详情信息.
 *
 * @author: Carlos Huang
 * @Date: 2020-8-20
 */
@Data
@NoArgsConstructor
public class ResultFieldMessage {
    /** 字段
     * 若只有一级则直接为字段的写方法，如: setName
     * 若为多级则为完全调用路径，如：getUser.setName
     */
    private String property;
    /** 读方法对应的写方法, 如 getName -> setName. */
    private Map<String, String> readWriteMap;
    /** 读方法返回的类型. */
    private Map<String, Class<?>> readTypeMap;

    public ResultFieldMessage(String property) {
        this.property = property;
    }
}
