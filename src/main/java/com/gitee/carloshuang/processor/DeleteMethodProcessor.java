package com.gitee.carloshuang.processor;

import com.gitee.carloshuang.annotation.Delete;
import com.gitee.carloshuang.annotation.Update;
import com.gitee.carloshuang.model.MethodSqlParam;
import com.gitee.carloshuang.model.Param;
import com.gitee.carloshuang.model.SqlParamModel;
import com.gitee.carloshuang.storage.ConnectionHolder;
import com.gitee.carloshuang.utils.SqlUtils;
import com.squareup.javapoet.MethodSpec;

import javax.lang.model.element.Modifier;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * 删除方法处理器.
 *
 * @author: Carlos Huang
 * @Date: 2020-8-24
 */
class DeleteMethodProcessor {

    /**
     * 解析 @Delete 注解标记的方法
     * @param method
     * @return
     */
    public static MethodSpec parserDeleteMethod(Method method) {
        // 解析SQL
        Delete anno = method.getDeclaredAnnotation(Delete.class);
        String sql = anno.value();
        // 解析SQL参数
        SqlParamModel sqlForParam = SqlUtils.parserSqlParam(sql);
        sql = sqlForParam.getSql();
        // 解析方法参数
        MethodSqlParam methodSqlParam = SqlUtils.parserParams(method, sqlForParam.getAliasMap());
        // 创建实现类方法源代码
        Parameter[] params = method.getParameters();
        MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder(method.getName())
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .returns(method.getReturnType());
        for (Parameter param : params) {
            methodBuilder.addParameter(param.getType(), param.getName());
        }
        Class<?> returnType = method.getReturnType();
        // 主要代码逻辑
        methodBuilder.addCode("$T connection = null;\n" +
                        "$T statement = null;\n" +
                        "try {\n",
                Connection.class, PreparedStatement.class);
        methodBuilder.addStatement("connection = $T.getInstance().getConnection()", ConnectionHolder.class);
        methodBuilder.addStatement("statement = connection.prepareStatement($S)", sql);
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
        methodBuilder.addStatement("int size = statement.executeUpdate()", int.class);
        // 目前只能返回布尔类型或整型
        if (isBooleanReturn(returnType)) {
            methodBuilder.addStatement("return size > 0");
        }else if (isIntegerReturn(returnType)) {
            methodBuilder.addStatement("return size");
        } else if (hasReturn(returnType)) {
            methodBuilder.addStatement("return null");
        }
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
        return methodBuilder.build();
    }

    /**
     * 是否有返回值
     * @param returnType
     * @return
     */
    private static boolean hasReturn(Class<?> returnType) {
        return returnType != Void.class && returnType != void.class;
    }

    /**
     * 返回值是否为布尔类型
     * @param returnType
     * @return
     */
    private static boolean isBooleanReturn(Class<?> returnType) {
        return returnType == Boolean.class || returnType == boolean.class;
    }

    /**
     * 返回值是否为整型
     * @param returnType
     * @return
     */
    private static boolean isIntegerReturn(Class<?> returnType) {
        return returnType == Integer.class || returnType == int.class;
    }

}
