package com.gitee.carloshuang.utils;

import java.io.File;
import java.io.IOException;

/**
 * 文件工具类.
 *
 * @author: Carlos Huang
 * @Date: 2020-8-22
 */
public final class FileUtils {

    /**
     * 删除目录或文件.
     * @param file
     */
    public static void delDir(File file) {
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            for (File f : files) {
                delDir(f);
            }
            if (!file.delete()) throw new RuntimeException(file.getAbsolutePath() + " 目录删除失败");
        } else {
            if (!file.delete()) throw new RuntimeException(file.getAbsoluteFile() + " 文件删除失败");
        }
    }

}
