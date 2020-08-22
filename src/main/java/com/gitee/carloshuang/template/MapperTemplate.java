package com.gitee.carloshuang.template;

import com.esotericsoftware.reflectasm.MethodAccess;
import com.gitee.carloshuang.exception.QueryNotUniqueException;
import com.gitee.carloshuang.model.QueryResultType;
import com.gitee.carloshuang.model.ResultFieldMessage;

import java.lang.reflect.Array;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.*;

/**
 * Mapper模板.
 *
 * @author: Carlos Huang
 * @Date: 2020-8-19
 */
public class MapperTemplate {

    /**
     * 查询结果填充
     * @param resultType
     * @param fieldMap
     * @param resultSet
     * @return
     * @throws Exception
     */
    protected final Object fillData(QueryResultType resultType, Map<String, ResultFieldMessage> fieldMap,
                                    ResultSet resultSet) throws Exception {
        // 方法返回为基本类型
        if (resultType.isPrimitive()) {
            return handlerPrimitive(resultSet);
        }
        // 返回数组集合
        if (resultType.isCollection()) {
            return handlerCollection(resultType, fieldMap, resultSet);
        }
        // Map类型
        if (resultType.isMap()) {
            return handlerMap(resultSet);
        }
        // 数组类型
        if (resultType.isArray()) {
            return handlerArray(resultSet, resultType.getReturnType());
        }
        // 自定义模型
        return handlerCustomerModel(resultType.getReturnType(), fieldMap, resultSet);
    }

    /**
     * 方法返回为集合时处理结果
     * @param resultType
     * @param fieldMap
     * @param resultSet
     * @return
     */
    private Object handlerCollection(QueryResultType resultType, Map<String, ResultFieldMessage> fieldMap,
                                     ResultSet resultSet) throws IllegalAccessException, InstantiationException, SQLException {
        // 创建返回对象
        Class<?> type = resultType.getReturnType();
        Collection result = null;
        // 是否为接口
        if (!type.isInterface()) {
            result = (Collection) type.newInstance();
        } else {
            // 如果为List
            if (List.class.isAssignableFrom(type)) result = new ArrayList();
            if (Set.class.isAssignableFrom(type)) result = new HashSet();
        }
        QueryResultType subType = resultType.getSubResultType();
        // 基本类型、String、Date类型
        if (subType.isPrimitive()) {
            handlerCollectionPrimitive(result, resultSet);
            return result;
        }
        // Map类型
        if (subType.isMap()) {
            handlerCollectionMap(result, resultSet);
            return result;
        }
        // 数组类型
        if (subType.isArray()) {
            handlerCollectionArray(result, resultSet, subType.getReturnType());
            return result;
        }
        // 自定义实体
        handlerCollectionCustomerModel(result, subType.getReturnType(), fieldMap, resultSet);
        return result;
    }

    /**
     * 处理自定义模型的集合
     * @param result
     * @param type
     * @param fieldMap
     * @param resultSet
     * @param <T>
     * @throws SQLException
     * @throws IllegalAccessException
     * @throws InstantiationException
     */
    private  <T> void handlerCollectionCustomerModel(Collection result, Class<T> type,
                                                   Map<String, ResultFieldMessage> fieldMap, ResultSet resultSet)
            throws SQLException, IllegalAccessException, InstantiationException {
        try {
            ResultSetMetaData resultSetMetaData = resultSet.getMetaData();
            int count = resultSetMetaData.getColumnCount();
            while (resultSet.next()) {
                T target = type.newInstance();
                for (int i = 1; i <= count; i++) {
                    String name = resultSetMetaData.getColumnLabel(i);
                    if (fieldMap.containsKey(name)) {
                        ResultFieldMessage fieldMessage = fieldMap.get(name);
                        setValue(target, fieldMessage, resultSet.getObject(name));
                    }
                }
                result.add(target);
            }
        } finally {
            resultSet.close();
        }
    }

    /**
     * 处理填充自定义模型
     * @param type
     * @param fieldMap
     * @param resultSet
     * @param <T>
     * @return
     */
    private <T> T handlerCustomerModel(Class<T> type, Map<String,
            ResultFieldMessage> fieldMap, ResultSet resultSet) throws SQLException, IllegalAccessException, InstantiationException {
        T target = null;
        try {
            ResultSetMetaData resultSetMetaData = resultSet.getMetaData();
            int count = resultSetMetaData.getColumnCount();
            int index = 0;
            while (resultSet.next()) {
                target = type.newInstance();
                for (int i = 1; i <= count; i++) {
                    String name = resultSetMetaData.getColumnLabel(i);
                    if (fieldMap.containsKey(name)) {
                        ResultFieldMessage fieldMessage = fieldMap.get(name);
                        setValue(target, fieldMessage, resultSet.getObject(name));
                    }
                }
                if (index > 0) throw new QueryNotUniqueException();
                index++;
            }
        } finally {
            resultSet.close();
        }
        return target;
    }

    /**
     * 处理可接收基本类型、String、Date类型的集合
     * @param result
     * @param resultSet
     */
    private void handlerCollectionPrimitive(Collection result, ResultSet resultSet) throws SQLException {
        try {
            ResultSetMetaData resultSetMetaData = resultSet.getMetaData();
            int count = resultSetMetaData.getColumnCount();
            while (resultSet.next()) {
                for (int i = 1; i <= count; i++) {
                    result.add(resultSet.getObject(i));
                }
            }
        } finally {
            resultSet.close();
        }
    }

    /**
     * 处理泛型为Map类型的集合
     * @param result
     * @param resultSet
     */
    private void handlerCollectionMap(Collection result, ResultSet resultSet) throws SQLException {
        try {
            ResultSetMetaData resultSetMetaData = resultSet.getMetaData();
            int count = resultSetMetaData.getColumnCount();
            while (resultSet.next()) {
                Map<Object, Object> map = new HashMap<>();
                for (int i = 1; i <= count; i++) {
                    String name = resultSetMetaData.getColumnLabel(i);
                    map.put(name, resultSet.getObject(name));
                }
                result.add(map);
            }
        } finally {
            resultSet.close();
        }
    }

    /**
     * 处理Map类型
     * @param resultSet
     * @return
     */
    public Map handlerMap(ResultSet resultSet) throws SQLException {
        Map<Object, Object> map = new HashMap<>();
        try {
            ResultSetMetaData resultSetMetaData = resultSet.getMetaData();
            int count = resultSetMetaData.getColumnCount();
            int index = 0;
            while (resultSet.next()) {
                for (int i = 1; i <= count; i++) {
                    String name = resultSetMetaData.getColumnLabel(i);
                    map.put(name, resultSet.getObject(name));
                }
                if (index > 0) throw new QueryNotUniqueException();
                index++;
            }
        } finally {
            resultSet.close();
        }
        return map;
    }

    /**
     * 处理泛型为 数组类型的集合
     * @param result
     * @param resultSet
     */
    private <T> void handlerCollectionArray(Collection result, ResultSet resultSet, Class<T> type) throws SQLException {
        try {
            ResultSetMetaData resultSetMetaData = resultSet.getMetaData();
            int count = resultSetMetaData.getColumnCount();
            while (resultSet.next()) {
                // 创建数组
                T[] array = (T[]) Array.newInstance(type, count);
                for (int i = 1; i <= count; i++) {
                    array[i - 1] = resultSet.getObject(i, type);
                }
                result.add(array);
            }
        } finally {
            resultSet.close();
        }
    }

    /**
     * 处理数组类型
     * @param resultSet
     * @param type
     * @param <T>
     * @return
     * @throws SQLException
     */
    private <T> T[] handlerArray(ResultSet resultSet, Class<T> type) throws SQLException {
        T[] array = (T[]) Array.newInstance(type, 0);
        try {
            ResultSetMetaData resultSetMetaData = resultSet.getMetaData();
            int count = resultSetMetaData.getColumnCount();
            int index = 0;
            while (resultSet.next()) {
                // 创建数组
                array = (T[]) Array.newInstance(type, count);
                for (int i = 1; i <= count; i++) {
                    array[i - 1] = resultSet.getObject(i, type);
                }
                if (index > 1) throw new QueryNotUniqueException();
                index++;
            }
        } finally {
            resultSet.close();
        }
        return array;
    }

    /**
     * 方法返回为基本类型时处理结果
     * @param resultSet
     * @return
     */
    private Object handlerPrimitive(ResultSet resultSet) throws SQLException {
        Object result = null;
        try {
            int count = 0;
            while (resultSet.next()) {
                result = resultSet.getObject(1);
                if (count > 0) throw new QueryNotUniqueException();
                count++;
            }
        } finally {
            resultSet.close();
        }
        return result;
    }

    /**
     * 设置值到对象中
     * @param target 目标对象
     * @param fieldMessage 目标属性设置信息
     * @param value 值
     */
    protected final void setValue(Object target, ResultFieldMessage fieldMessage, Object value) throws IllegalAccessException, InstantiationException {
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
