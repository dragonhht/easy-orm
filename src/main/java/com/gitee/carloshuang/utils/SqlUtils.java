package com.gitee.carloshuang.utils;

import com.gitee.carloshuang.model.SqlParamModel;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

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
                    if (i + 1 >= len) {
                        char[] alias = Arrays.copyOfRange(chars, start + 2, end);
                        changeAliasToMark(chars, start, end);
                        map.put(++size, new String(alias));
                        continue;
                    }
                    // 若后面是空格，则为占位符结束
                    if (chars[i + 1] == ' ') {
                        char[] alias = Arrays.copyOfRange(chars, start + 2, end);
                        changeAliasToMark(chars, start, end);
                        map.put(++size, new String(alias));
                        continue;
                    }
                    // 若后面为右括号，则判断左侧是否除空格外是否为左括号
                    if (chars[i + 1] == ')') {
                        int index = start - 1;
                        while (chars[index] == ' ') index--;
                        if (chars[index] == '(') {
                            char[] alias = Arrays.copyOfRange(chars, start + 2, end);
                            changeAliasToMark(chars, start, end);
                            map.put(++size, new String(alias));
                            continue;
                        }
                    }
                }
            }
            // 出现?
            if (!isInStr && chars[i] == '?') {
                if (i == len - 1) {
                    int index = i - 1;
                    // 查看左侧是否有=
                    while (chars[index] == ' ') index--;
                    if (chars[index] == '=') size++;
                    continue;
                }
                // 是否被()包裹
                int startIndex = i - 1, endIndex = i + 1;
                while (endIndex < len && chars[endIndex] == ' ') endIndex++;
                if (endIndex >= len && (chars[startIndex - 1] == ' ' || chars[startIndex - 1] == '='))  size++;
                if (endIndex < len && chars[endIndex] == ')') {
                    // 查看左侧是否有(
                    while (chars[startIndex] == ' ') startIndex--;
                    if (chars[startIndex] == '(') size++;
                    continue;
                }
                if (endIndex < len && chars[endIndex] != ')' && chars[i + 1] == ' ') {
                    // 查看左侧是否有=
                    while (chars[startIndex] == ' ') startIndex--;
                    if (chars[startIndex] == '=') size++;
                }
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

    public static void main(String[] args) {
        String sql = "select * from user where id= ${id} and name in ( ${names}) and password = ${pwd}";
        SqlParamModel paramModel = parserSqlParam(sql);
        System.out.println(paramModel);
    }

}
