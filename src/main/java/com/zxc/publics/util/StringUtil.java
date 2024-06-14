package com.zxc.publics.util;

public class StringUtil {

    /**
     * 对象转字符串（避免 null pointer exception & "null"）
     * @param value
     * @return
     * @param <T>
     */
    public static <T> String valueToStr(T value) {
        return value == null ? "" : value.toString();
    }

    /**
     * 判断字符串是否为空
     * @param str
     * @return
     */
    public static boolean isEmpty(String str) {
        return str == null || str.trim().isEmpty();
    }

}
