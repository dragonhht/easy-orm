package com.gitee.carloshuang.template;

import com.esotericsoftware.reflectasm.MethodAccess;
import com.gitee.carloshuang.model.ResultFieldMessage;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.Map;

/**
 * Mapper模板接口.
 *
 * @author: Carlos Huang
 * @Date: 2020-8-19
 */
public interface MapperTemplate {

    /**
     * 查询结果填充
     * @param returnType
     * @param fieldMap
     * @param resultSet
     * @return
     * @throws Exception
     */
    default Object fillData(Class<?> returnType, Map<String, ResultFieldMessage> fieldMap, ResultSet resultSet) throws Exception {
        ResultSetMetaData resultSetMetaData = resultSet.getMetaData();
        int count = resultSetMetaData.getColumnCount();
        while (resultSet.next()) {
            Object target = returnType.newInstance();
            for (int i = 1; i < count; i++) {
                String name = resultSetMetaData.getColumnLabel(i);
                if (fieldMap.containsKey(name)) {
                    ResultFieldMessage fieldMessage = fieldMap.get(name);
                    setValue(target, fieldMessage, resultSet.getObject(name));
                }
            }
            System.out.println("结果: " + target);
        }
        resultSet.close();
        return null;
    }

    /**
     * 设置值到对象中
     * @param target 目标对象
     * @param fieldMessage 目标属性设置信息
     * @param value 值
     */
    default void setValue(Object target, ResultFieldMessage fieldMessage, Object value) throws IllegalAccessException, InstantiationException {
        MethodAccess methodAccess = MethodAccess.get(target.getClass());
        String field = fieldMessage.getProperty();
        String[] fields = field.split("\\.");
        // 一级时直接调用方法设置
        if (fields.length < 2) {
            methodAccess.invoke(target, field, value);
            return;
        }
        int last = fields.length - 1;
        Object targetField = null;
        for (int i = 0; i < fields.length; i++) {
            // 非最后的属性都是get操作
            if (i == 0) {
                targetField = methodAccess.invoke(target, fields[i]);
                if (targetField == null) {
                    // 创建实例
                    targetField = fieldMessage.getReadTypeMap().get(fields[i]).newInstance();
                }
                methodAccess.invoke(target, fieldMessage.getReadWriteMap().get(fields[i]), targetField);
                continue;
            }
            if (i < last) {
                methodAccess = MethodAccess.get(targetField.getClass());
                targetField = methodAccess.invoke(targetField, fields[i]);
                // 目标属性为空时自动初始化
                if (targetField == null) {
                    targetField = fieldMessage.getReadTypeMap().get(fields[i]).newInstance();
                }
                methodAccess.invoke(target, fieldMessage.getReadWriteMap().get(fields[i]), targetField);
                continue;
            }
            methodAccess = MethodAccess.get(targetField.getClass());
            methodAccess.invoke(targetField, fields[i], value);
        }
    }
}
