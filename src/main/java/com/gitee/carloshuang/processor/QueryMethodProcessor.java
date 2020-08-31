package com.gitee.carloshuang.processor;

import com.gitee.carloshuang.annotation.Query;
import com.gitee.carloshuang.annotation.Result;
import com.gitee.carloshuang.annotation.Results;
import com.gitee.carloshuang.model.*;
import com.gitee.carloshuang.storage.ConnectionHolder;
import com.gitee.carloshuang.storage.QueryResultHolder;
import com.gitee.carloshuang.storage.SqlContainer;
import com.gitee.carloshuang.utils.SqlUtils;
import com.squareup.javapoet.MethodSpec;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;

import javax.lang.model.element.Modifier;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 查询方法处理器.
 *
 * @author: Carlos Huang
 * @Date: 2020-8-22
 */
class QueryMethodProcessor {

    private static QueryMethodProcessor PROCESSOR;

    public static QueryMethodProcessor getProcessor() {
        if (PROCESSOR == null) {
            synchronized (QueryMethodProcessor.class) {
                if (PROCESSOR == null) {
                    PROCESSOR = new QueryMethodProcessor();
                }
            }
        }
        return PROCESSOR;
    }

    /**
     * 解析 @Query 注解标记的方法
     * @param namespace 命名空间
     * @param method
     * @return
     */
    public MethodSpec parserQueryMethod(String namespace, Method method) {
        // 解析查询结果字段映射
        String methodId = parserResultMap(namespace, method);
        // 解析 Query
        Query anno = method.getDeclaredAnnotation(Query.class);
        String sql = anno.value();
        // 存放sql语句
        SqlContainer.getInstance().put(methodId, sql);
        // 解析SQL
        SqlParamModel sqlForParam = SqlUtils.parserSqlParam(sql);
        sql = sqlForParam.getSql();
        // 解析方法参数
        MethodSqlParam methodSqlParam = SqlUtils.parserParams(method, sqlForParam.getAliasMap());
        // 解析拼接参数
        Map<String, String> splicingParamMap = SqlUtils.parserSplicingParams(sqlForParam.getSplicingAlias(), methodSqlParam.getParams());
        // 创建实现类方法源代码
        Parameter[] params = method.getParameters();
        MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder(method.getName())
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .returns(method.getReturnType());
        for (Parameter param : params) {
            methodBuilder.addParameter(param.getType(), param.getName());
        }
        // 主要代码逻辑
        methodBuilder.addCode("$T connection = null;\n" +
                        "$T statement = null;\n" +
                        "$T result = null;\n" +
                        "try {\n",
                Connection.class, PreparedStatement.class, method.getReturnType());
        methodBuilder.addStatement("connection = $T.getInstance().getConnection()", ConnectionHolder.class);
        methodBuilder.addStatement("String sql = $S", sql);

        // 替换拼接参数
        for (Map.Entry<String, String> entry : splicingParamMap.entrySet()) {
            // TODO 需处理含有空格的情况
            methodBuilder.addStatement("sql = sql.replace($S, $N)",
                    entry.getKey(), "String.valueOf(" + entry.getValue() + ")");
        }
        methodBuilder.addStatement("statement = connection.prepareStatement(sql)");
        // sql传参
        int paramSize = sqlForParam.getSize();
        // 判断使用别名的参数是否为空
        Map<Integer, String> aliasMap = sqlForParam.getAliasMap();
        // 无别名时传参
        if (aliasMap == null || aliasMap.size() == 0) {
            List<Param> sqlParam = methodSqlParam.getParams();
            for (int i = 1; i <= paramSize; i++) {
                methodBuilder.addStatement("statement.setObject($L, $N)", i, sqlParam.get(i - 1).getName());
            }
        } else {
            Map<String, Param> sqlParamMap = methodSqlParam.getAliasMap();
            // 有别名时传参
            for (int i = 1; i <= paramSize; i++) {
                // 获取别名参数
                String alias = aliasMap.get(i);
                // 对应的获取方法
                String paramName = sqlParamMap.get(alias).getWay();
                methodBuilder.addStatement("statement.setObject($L, $N)", i, paramName);
            }
        }
        methodBuilder.addStatement("$T resultSet = statement.executeQuery()", ResultSet.class);
        methodBuilder.addStatement("$T<String, $T> resultFieldMessageMap = $T.getInstance().getResultMap($S)",
                Map.class, ResultFieldMessage.class, QueryResultHolder.class, methodId);
        methodBuilder.addStatement("$T resultType = QueryResultHolder.getInstance().getResultType($S)",
                QueryResultType.class, methodId);
        methodBuilder.addStatement("result = ($T) fillData(resultType, resultFieldMessageMap, resultSet)", method.getReturnType());
        methodBuilder.addCode("}catch ($T e) {\n" +
                        "            throw new $T(e);\n" +
                        "        } finally {\n" +
                        "            if (statement != null) {\n" +
                        "                try {\n" +
                        "                    statement.close();\n" +
                        "                } catch ($T throwables) {\n" +
                        "                    throw new RuntimeException(throwables);\n" +
                        "                }\n" +
                        "            }\n" +
                        "            ConnectionHolder.getInstance().closeConnection();\n" +
                        "        }\n",
                Exception.class, RuntimeException.class, SQLException.class);
        methodBuilder.addStatement("return result");
        return methodBuilder.build();
    }

    /**
     * 解析 @Results 注解
     * @param namespace 命名空间
     * @param method
     * @return 方法id
     */
    private String parserResultMap(String namespace, Method method) {
        Map<String, String> map = new HashMap<>();
        // 默认id为方法名
        String id = method.getName();
        Results annotation = method.getDeclaredAnnotation(Results.class);
        if (annotation != null) {
            String nameId = annotation.id();
            // 如果用户自定义id，则使用用户自定义id
            if (StringUtils.isNotEmpty(nameId)) id = nameId;
            Result[] results = annotation.value();
            for (Result result : results) {
                map.put(result.column(), result.property());
            }
        }
        // 保存
        String methodId = namespace + "." + id;
        // 解析方法返回类型
        QueryResultType queryResultType = parserQueryMethodReturn(method.getReturnType(), method.getGenericReturnType());
        // 保存方法返回类型
        QueryResultHolder.getInstance().putResultType(methodId, queryResultType);
        // 解析查询结果字段与实体对应关系
        Map<String, ResultFieldMessage> resultMap = resultMap(queryResultType, map);
        QueryResultHolder.getInstance().putFieldMap(methodId, resultMap);
        return methodId;
    }

    /**
     * 解析方法返回类型信息
     * @return
     */
    private QueryResultType parserQueryMethodReturn(Class<?> returnType, Type type) {
        QueryResultType resultType = new QueryResultType();
        resultType.setReturnType(returnType);
        // 是否为基本类型
        if (resultType.isPrimitive()) {
            resultType.setPrimitive(true);
            return resultType;
        } else {
            // 判断是否为字符串、Date或和转为基本类型
            if (returnType.equals(String.class) || returnType.equals(Date.class) || returnType.equals(Double.class)
                    || returnType.equals(Float.class) || returnType.equals(Long.class) || returnType.equals(Integer.class)
                    || returnType.equals(Boolean.class)) {
                resultType.setPrimitive(true);
                return resultType;
            }
        }
        // 集合类型
        if (Collection.class.isAssignableFrom(returnType)) {
            if (!List.class.isAssignableFrom(returnType) && !Set.class.isAssignableFrom(returnType))
                throw new RuntimeException("暂时只支持List或Set");
            resultType.setCollection(true);
            // 查询泛型类型
            parserGenericType(type, resultType);
        }
        // 数组类型
        if (returnType.isArray()) {
            resultType.setArray(true);
        }
        // Map类型
        if (Map.class.isAssignableFrom(returnType)) {
            resultType.setMap(true);
            parserMapType(type, resultType);
        }
        return resultType;
    }

    /**
     * 处理Map类型
     * @param type
     * @param resultType
     */
    private void parserMapType(Type type, QueryResultType resultType) {
        Type[] subTypes = ((ParameterizedType) type).getActualTypeArguments();
        Class<?>[] subTypeClasses = new Class<?>[subTypes.length];
        for (int i = 0; i < subTypes.length; i++) {
            Type subType = subTypes[i];
            if (subType instanceof ParameterizedType) throw new RuntimeException("Map不支持多级泛型");
            Class<?> clazz = (Class<?>) subType;
            subTypeClasses[i] = clazz;
        }
        resultType.setSubType(subTypeClasses);
    }

    /**
     * 处理数组类型
     * @param resultType
     */
    private void parserArrayType(Class<?> type, QueryResultType resultType) {
        Class<?> clazz = type.getComponentType();
        if (clazz.isArray()) throw new RuntimeException("暂不支持二维数组");
        resultType.setReturnType(clazz);
    }

    /**
     * 处理集合和Map类型
     * @param type
     */
    private void parserGenericType(Type type, QueryResultType resultType) {
        Type[] subTypes = ((ParameterizedType) type).getActualTypeArguments();
        Class<?>[] subTypeClasses = new Class<?>[subTypes.length];
        for (int i = 0; i < subTypes.length; i++) {
            Type subType = subTypes[i];
            // 判断是否为参数化类型
            if (subType instanceof ParameterizedType) {
                // 获取类型
                Class<?> clazz = (Class<?>) ((ParameterizedType) subType).getRawType();
                // 是否为Map类型
                if (!Map.class.isAssignableFrom(clazz)) throw new RuntimeException("暂时只支持Collection<Map>多级泛型参数");
                QueryResultType subResultType = new QueryResultType();
                subResultType.setReturnType(clazz);
                subResultType.setMap(true);
                resultType.setSubResultType(subResultType);
                subTypeClasses[i] = clazz;
                continue;
            }
            Class<?> clazz = (Class<?>) subType;
            if (!clazz.isArray()) {
                subTypeClasses[i] = clazz;
                QueryResultType subResultType = parserQueryMethodReturn(clazz, null);
                resultType.setSubResultType(subResultType);
                continue;
            }
            QueryResultType subResultType = new QueryResultType();
            subResultType.setArray(true);
            parserArrayType(clazz, subResultType);
            resultType.setSubResultType(subResultType);
        }
        resultType.setSubType(subTypeClasses);
    }

    /**
     * 组装查询结果字段映射关系
     * @param queryResultType 返回类型
     * @param resultMapSettings 用户自定义的映射关系
     * @return
     */
    @SneakyThrows
    private Map<String, ResultFieldMessage> resultMap(QueryResultType queryResultType, Map<String, String> resultMapSettings) {
        Map<String, ResultFieldMessage> resultMap = new HashMap<>();
        Class<?> returnType = null;
        // 是实体模型或集合下为实体模型是才解析
        if (isModel(queryResultType) ) {
            returnType = queryResultType.getReturnType();
            Map<String, ResultFieldMessage> settingResultMap = handlerResultMapSetting(returnType, resultMapSettings);
            resultMap.putAll(settingResultMap);
        } else if ((queryResultType.isCollection() && isModel(queryResultType.getSubResultType()))) {
            returnType = queryResultType.getSubType()[0];
            Map<String, ResultFieldMessage> settingResultMap =
                    handlerResultMapSetting(returnType, resultMapSettings);
            resultMap.putAll(settingResultMap);
        } else {
            // 没有模型则直接返回不进行下一步解析
            return resultMap;
        }
        // 实体其余字段按属性名对应
        Field[] fields = returnType.getDeclaredFields();
        for (Field field : fields) {
            PropertyDescriptor descriptor = new PropertyDescriptor(field.getName(), returnType);
            Method writeMethod = descriptor.getWriteMethod();
            if (!resultMap.containsKey(field.getName())) {
                ResultFieldMessage message = new ResultFieldMessage(writeMethod.getName());
                resultMap.put(field.getName(), message);
            }
        }
        return resultMap;
    }

    /**
     * 判断返回结果类型是否为实体模型
     * @param resultType
     * @return
     */
    private boolean isModel(QueryResultType resultType) {
        if (resultType == null) return false;
        return !resultType.isArray() && !resultType.isCollection() && !resultType.isMap() && !resultType.isPrimitive();
    }

    /**
     * 处理用户自定义的映射关系
     * @param returnType 接收实体类型
     * @param resultMapSettings 自定义字段映射
     */
    private Map<String, ResultFieldMessage> handlerResultMapSetting(Class<?> returnType,
                                                                    Map<String, String> resultMapSettings) {
        Map<String, ResultFieldMessage> map = new HashMap<>();
        for (Map.Entry<String, String> entry : resultMapSettings.entrySet()) {
            String value = entry.getValue();
            String[] fields = value.split("\\.");
            // 一级时
            if (fields.length < 2) {
                try {
                    PropertyDescriptor descriptor = new PropertyDescriptor(value, returnType);
                    Method writeMethod = descriptor.getWriteMethod();
                    ResultFieldMessage message = new ResultFieldMessage(writeMethod.getName());
                    map.put(entry.getKey(), message);
                    continue;
                } catch (IntrospectionException e) {
                    throw new RuntimeException(e);
                }
            }
            // 存放对应的方法
            String[] methods = new String[fields.length];
            Class<?> modelType = returnType;
            int last = fields.length - 1;
            // 读方法与写方法的映射
            Map<String, String> getSetMap = new HashMap<>();
            Map<String, Class<?>> readTypeMap = new HashMap<>();
            for (int i = 0; i < fields.length; i++) {
                try {
                    if (i != last) {
                        // 不是最后一个属性，则获取 get 方法
                        PropertyDescriptor descriptor = new PropertyDescriptor(fields[i], modelType);
                        Method readMethod = descriptor.getReadMethod();
                        modelType = readMethod.getReturnType();
                        methods[i] = readMethod.getName();
                        // 写方法
                        Method writeMethod = descriptor.getWriteMethod();
                        getSetMap.put(methods[i], writeMethod.getName());
                        readTypeMap.put(methods[i], writeMethod.getReturnType());
                        continue;
                    }
                    // 最后一个属性获取 set 方法
                    PropertyDescriptor descriptor = new PropertyDescriptor(fields[i], modelType);
                    Method writeMethod = descriptor.getWriteMethod();
                    methods[i] = writeMethod.getName();
                } catch (IntrospectionException e) {
                    throw new RuntimeException(e);
                }
            }
            value = StringUtils.join(methods, ".");
            ResultFieldMessage message = new ResultFieldMessage(value);
            message.setReadWriteMap(getSetMap);
            message.setReadTypeMap(readTypeMap);
            map.put(entry.getKey(), message);
        }
        return map;
    }

}
