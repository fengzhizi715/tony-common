package com.safframework.tony.common.utils;

import java.io.Closeable;
import java.io.IOException;

/**
 * Created by tony on 2017/10/16.
 */
public class IOUtils {

    /**
     * 安全关闭io流
     * @param closeable
     */
    public static void closeQuietly(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
