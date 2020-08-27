package com.gitee.carloshuang.utils;

import com.gitee.carloshuang.model.MethodSqlParam;
import com.gitee.carloshuang.model.Param;
import com.gitee.carloshuang.model.SqlParamModel;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Sql语句工具.
 *
 * @author: Carlos Huang
 * @Date: 2020-8-24
 */
public class SqlUtils {

    /**
     * 解析sql语句中需要传递的参数
     * @param sql sql语句
     * @return
     */
    public static SqlParamModel parserSqlParam(String sql) {
        // TODO 需整理优化代码
        SqlParamModel model = new SqlParamModel();
        // 是否在字符串常量中
        boolean isInStr = false;
        int size = 0;
        Map<Integer, String> map = new HashMap<>();
        int start = 0, end = 0;
        // 是否是使用${占位符
        char[] chars = sql.toCharArray();
        int len = chars.length;
        for (int i = 0; i < len; i++) {
            if (start > 0) end++;
            if (chars[i] == '\'') {
                isInStr = !isInStr;
                continue;
            }
            // 查询到别名使用的占位符
            if (!isInStr && chars[i] == '$') {
                // 判断下一个字符是否为 {
                if (i < len - 1) {
                    if (chars[i + 1] == '{') {
                        start = i;
                        end = start;
                        continue;
                    }
                }
            }
            // 发现占位符后，查找到 } 或到最后一个字符
            if (!isInStr && start > 0 && (end > start + 1)) {
                if (i == len - 1 && chars[i] != '}') continue;
                if (chars[i] == '}') {
                    // 是否为最后一个字符
                    if (i + 1 >= len || chars[i + 1] == ' ' || chars[i + 1] == ')' || chars[i + 1] == ',') {
                        char[] alias = Arrays.copyOfRange(chars, start + 2, end);
                        changeAliasToMark(chars, start, end);
                        map.put(++size, new String(alias));
                        continue;
                    }
                }
            }
            // 出现?
            if (!isInStr && chars[i] == '?') {
                if (i == len - 1) {
                    if (chars[i - 1] == '=' || chars[i - 1] == ' ') size++;
                    continue;
                }
                if (chars[i - 1] == '=' || chars[i - 1] == ' ' || chars[i - 1] == ',' || chars[i - 1] == '(') size++;
            }
        }
        model.setSql(new String(chars));
        model.setAliasMap(map);
        model.setSize(size);
        return model;
    }

    /**
     * 将指定范围内的字符转换为?和空格
     * @param chars 字符
     * @param start 起始点
     * @param end 结束点
     */
    public static void changeAliasToMark(char[] chars, int start, int end) {
        int index = start;
        while (index <= end) {
            char ch = index == start ? '?' : ' ';
            chars[index] = ch;
            index++;
        }
    }

    /**
     * 解析方法参数
     * @param method
     * @param aliasMap Sql语句中的别名占位符
     * @return
     */
    public static MethodSqlParam parserParams(Method method, Map<Integer, String> aliasMap) {
        MethodSqlParam methodSqlParam = new MethodSqlParam();
        Parameter[] parameters = method.getParameters();
        List<Param> params = new ArrayList<>(parameters.length);
        for (Parameter parameter : parameters) {
            Param param = new Param();
            // 是否为数组，集合
            Class<?> clazz = parameter.getType();
            param.setType(clazz);
            if (clazz.isArray() || Collection.class.isAssignableFrom(clazz)) {
                param.setPlural(true);
            }
            param.setName(parameter.getName());
            com.gitee.carloshuang.annotation.Param anno = parameter.getAnnotation(com.gitee.carloshuang.annotation.Param.class);
            if (anno != null) {
                param.setAlias(anno.value());
            }
            params.add(param);
        }
        Map<String, Param> paramMap = params.stream()
                .filter(v -> StringUtils.isNotEmpty(v.getAlias()))
                .collect(Collectors.toMap(Param::getAlias, v -> v, (v1, v2) -> v2));
        Map<String, Param> map =  parserParamToSqlParam(aliasMap, paramMap);
        methodSqlParam.setAliasMap(map);
        methodSqlParam.setParams(params);
        return methodSqlParam;
    }

    /**
     * 将方法别名参数解析成sql可用的参数
     * @param aliasMap
     * @param paramMap
     * @return
     */
    @SneakyThrows
    public static Map<String, Param> parserParamToSqlParam(Map<Integer, String> aliasMap, Map<String, Param> paramMap) {
        Map<String, Param> map = new HashMap<>();
        for (String value : aliasMap.values()) {
            Param param = new Param();
            // 是否为多级
            String[] alias = value.split("\\.");
            // 长度为一时，则表示为参数对象本身
            if (alias.length == 1) {
                Param par = paramMap.get(value);
                if (par == null) throw new RuntimeException("参数 ${" + value + "} 无对应的参数");
                param.setAlias(par.getAlias());
                param.setName(par.getName());
                param.setPlural(par.isPlural());
                param.setWay(par.getName());
                param.setType(par.getType());
                map.put(value, param);
                continue;
            }
            // 当存在多级时
            // 第一级为参数名
            String parName = alias[0];
            Param par = paramMap.get(parName);
            if (par == null) throw new RuntimeException("参数 ${" + value + "} 无对应的参数");
            Class<?> type = par.getType();
            StringBuilder sb = new StringBuilder(par.getName()).append(".");
            for (int i = 1; i < alias.length; i++) {
                PropertyDescriptor descriptor = new PropertyDescriptor(alias[i], type);
                Method readMethod = descriptor.getReadMethod();
                sb.append(readMethod.getName()).append("()").append(".");
                type = descriptor.getPropertyType();
            }
            int len = sb.length();
            char[] chars = new char[len - 1];
            sb.getChars(0, len - 1, chars, 0);
            String way = new String(chars);
            param.setWay(way);
            param.setType(type);
            param.setName(par.getName());
            param.setAlias(value);
            if (type.isArray() || Collection.class.isAssignableFrom(type)) {
                param.setPlural(true);
            }
            map.put(value, param);
        }
        return map;
    }

    public static void main(String[] args) {
        String sql = "select * from user where id= ${id} and name in ( ${names}) and password = ${pwd}";
        SqlParamModel paramModel = parserSqlParam(sql);
        System.out.println(paramModel);
    }

}
