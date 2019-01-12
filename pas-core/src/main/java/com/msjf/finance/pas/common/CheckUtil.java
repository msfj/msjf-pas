package com.msjf.finance.pas.common;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.regex.Pattern.compile;

/**
 * Created by chengjunping on 2019/1/10.
 */
public class CheckUtil {
    // 判断是否为空
    public static boolean isNull(Object b) {
        if (b == null) {
            return true;
        }
        return false;
    }

    /**
     * 字符是否为空或"null"
     *
     * @param str
     * @return
     */
    public static boolean isNull(String str) {
        if (str == null) {
            return true;
        }
        if ("null".equalsIgnoreCase(str)) {
            return true;
        }
        return "".equals(str);
    }

    // 判断Map是否为空
    public static boolean isNull(Map<?, ?> mp) {
        return mp == null || mp.isEmpty() || mp.size() <= 0;
    }

    /**
     * 判断list 是否为null，size<=0
     *
     * @param list
     * @return
     */
    public static boolean isNull(List<?> list) {
        return list == null || list.isEmpty() || list.size() <= 0;
    }

    /**
     * 判断字符串是否只有数字及字母
     *
     * @param str
     * @return
     */
    public static boolean isNumChar(String str) {
        int len = str.length();
        for (int i = 0; i < len; i++) {
            char c = str.charAt(i);
            if ((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z')) {
                continue;
            }
            if (Character.isDigit(c)) {
                continue;
            }
            return false;
        }
        return true;
    }

    /**
     * 判断是否为正整数
     *
     * @param str
     * @return
     */
    public static boolean isInteger(String str) {
        Pattern pattern = compile("[0-9]+(\\.0+)?");
        Matcher matcher = pattern.matcher(str);
        return matcher.matches();
    }

    /**
     * 判断是否为整数（包含正整数、负整数、零）
     * @param str
     * @return
     */
    public static boolean isNumInteger(String str) {
        Pattern pattern = compile("^[-\\+]?[\\d]*$");
        return pattern.matcher(str).matches();
    }

    /**
     * 判断是否为浮点数
     *
     * @param str
     * @return
     */
    public static boolean isDouble(String str) {
        boolean flag = false;
        try {
            Double.parseDouble(str);
            flag = true;
        } catch (NumberFormatException e) {
            throw new RuntimeException(e.getMessage(),e);
        }
        return flag;
    }

    /**
     * 判断是否为正确日期,
     *
     * @param str，格式为：yyyyMMdd
     * @return
     */
    public static boolean isDate(String str) {
        return isDate(str, "yyyyMMdd");
    }

    /**
     * 判断是否为正确日期
     *
     * @param str        日期字符串
     * @param dateFormat 日期格式
     * @return
     */
    public static boolean isDate(String str, String dateFormat) {
        try {
            SimpleDateFormat f = new SimpleDateFormat(dateFormat);
            Date d = f.parse(str);
            String s = f.format(d);
            return s.equals(str);
        } catch (Exception e) {
        }
        return false;
    }
}
