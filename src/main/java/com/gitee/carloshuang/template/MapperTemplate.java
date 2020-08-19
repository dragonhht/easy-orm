package com.gitee.carloshuang.template;

import com.esotericsoftware.reflectasm.MethodAccess;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.HashMap;
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
    default Object fillData(Class<?> returnType, Map<String, String> fieldMap, ResultSet resultSet) throws Exception {
        MethodAccess methodAccess = MethodAccess.get(returnType);
        ResultSetMetaData resultSetMetaData = resultSet.getMetaData();
        int count = resultSetMetaData.getColumnCount();
        while (resultSet.next()) {
            Object target = returnType.newInstance();
            for (int i = 1; i < count; i++) {
                String name = resultSetMetaData.getColumnLabel(i);
                if (fieldMap.containsKey(name)) {
                    String valueField = fieldMap.get(name);
                    methodAccess.invoke(target, valueField, resultSet.getObject(name));
                }
            }
            System.out.println("结果: " + target);
        }
        resultSet.close();
        return null;
    }

    /**
     * 组装查询结果字段映射关系
     * @param returnType 返回类型
     * @param resultMapSettings 用户自定义的映射关系
     * @return
     * @throws IntrospectionException
     */
    default Map<String, String> resultMap(Class<?> returnType, Map<String, String> resultMapSettings) throws IntrospectionException {
        // TODO 结果组装字段映射
        Field[] fields = returnType.getDeclaredFields();
        Map<String, String> fieldMap = new HashMap<>();
        for (Field field : fields) {
            PropertyDescriptor descriptor = new PropertyDescriptor(field.getName(), returnType);
            Method writeMethod = descriptor.getWriteMethod();
            fieldMap.put(field.getName(), writeMethod.getName());
        }
        return fieldMap;
    }

}
