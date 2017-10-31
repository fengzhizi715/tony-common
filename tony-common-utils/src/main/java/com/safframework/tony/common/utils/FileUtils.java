package com.safframework.tony.common.utils;

import java.io.File;

/**
 * Created by tony on 2017/11/1.
 */
public class FileUtils {

    public static boolean exists(File file) {
        return file!=null && file.exists();
    }

    /**
     * 判断是否文件
     * @param file
     * @return
     */
    public static boolean isFile(File file) {
        return exists(file) && file.isFile();
    }

    /**
     * 判断是否目录
     * @param file
     * @return
     */
    public static boolean isDirectory(File file) {
        return exists(file) && file.isDirectory();
    }
}
