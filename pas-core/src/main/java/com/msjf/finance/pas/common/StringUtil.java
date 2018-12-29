package com.msjf.finance.pas.common;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import java.math.BigDecimal;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by chengjunping on 2018/12/27.
 */
public class StringUtil {
    /**
     * 转换对象为字符串类型
     *
     * @param obj
     * @return
     */
    public static String valueOf(Object obj) {
        if (obj == null) {
            return "";
        }
        if (obj instanceof String) {
            String strObj = obj.toString().trim();
            if ("null".equalsIgnoreCase(strObj)) {
                return "";
            }
            return strObj;
        }
        if (obj instanceof BigDecimal) {
            BigDecimal bigObj = (BigDecimal) obj;
            return bigObj.toString();
        }

        return obj.toString().trim();
    }

    /**
     *
     * @Title: getMatcherStringByRegex
     * @Description: 根据正则表达式获取第一个匹配的字符串
     * @param regex
     *            正则表达式
     * @param input
     *            输入字符串
     * @return 第一个匹配的字符串
     * @throws
     */
    public static  String getMatcherStringByRegex(String regex, String input) {
        String matcherStr = "";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(input);
        while (matcher.find()) {
            matcherStr = matcher.group();
            return matcherStr;
        }
        return matcherStr;
    }

    /**
     *

     * @Description: 根据传入的json字符串返回对应的Map
     * @param regex 正则表达式
     * @param input 输入字符串
     * @return map
     * @throws
     */
    @SuppressWarnings("unchecked")
    public static Map<String,Object> getMapByString(String input) {
        Map<String,Object> map = new HashMap<String,Object>();
        map = (Map<String, Object>) JSON.parse(input);
        return map;
    }

    public static HashMap<String, Object> fromJson2Map(String jsonString) {
        HashMap jsonMap = JSON.parseObject(jsonString, HashMap.class);

        HashMap<String, Object> resultMap = new HashMap<String, Object>();
        for(Iterator iter = jsonMap.keySet().iterator(); iter.hasNext();){
            String key = (String)iter.next();
            if(jsonMap.get(key) instanceof JSONArray){
                JSONArray jsonArray = (JSONArray)jsonMap.get(key);
                List list = handleJSONArray(jsonArray);
                resultMap.put(key, list);
            }else{
                resultMap.put(key, jsonMap.get(key));
            }
        }
        return resultMap;
    }


    public static  List<HashMap<String, Object>> handleJSONArray(JSONArray jsonArray){
        List list = new ArrayList();
        for (Object object : jsonArray) {
            JSONObject jsonObject = (JSONObject) object;
            HashMap map = new HashMap<String, Object>();
            for (Map.Entry entry : jsonObject.entrySet()) {
                if(entry.getValue() instanceof  JSONArray){
                    map.put((String)entry.getKey(), handleJSONArray((JSONArray)entry.getValue()));
                }else{
                    map.put((String)entry.getKey(), entry.getValue());
                }
            }
            list.add(map);
        }
        return list;
    }

    // 判断是否为空
    public static boolean isNull(Object b) {
        if (b == null) {
            return true;
        }

        if (b instanceof String) {
            String strObj = StringUtil.valueOf(b);
            return "".equals(strObj);
        }
        return false;
    }
}
