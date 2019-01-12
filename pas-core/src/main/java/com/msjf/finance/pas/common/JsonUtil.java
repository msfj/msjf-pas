package com.msjf.finance.pas.common;

import com.alibaba.fastjson.JSON;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by chengjunping on 2019/1/10.
 */
public class JsonUtil {

    /**
     * json转HashMap
     * @param jsonString
     * @return
     */
    public static HashMap<String,Object> jsonToHashMap(String jsonString)throws Exception{
        return JSON.parseObject(jsonString,HashMap.class);
    }

    /**
     * Json转ListHashMap
     * @param jsonString
     * @return
     */
    public static List<HashMap> jsonToListHashMap(String jsonString){
        List<HashMap> list = new ArrayList<HashMap>();
        list =  JSON.parseArray(jsonString, HashMap.class);
        return list;
    }
    /**
     * Json转ListString
     * @param jsonString
     * @return
     * @throws Exception
     */
    public static List<String> jsonToListString(String jsonString)throws Exception{
        List<String> list = new ArrayList<String>();
        list =  JSON.parseArray(jsonString, String.class);
        return list;
    }

    /**
     * Json转ListHashMap
     * @param jsonString
     * @return
     */
    public static List<Map> jsonToListMap(String jsonString){
        return  JSON.parseArray(jsonString, Map.class);
    }
}
