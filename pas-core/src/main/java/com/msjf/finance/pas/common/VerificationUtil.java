package com.msjf.finance.pas.common;

import com.msjf.finance.pas.common.response.Response;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by chengjunping on 2018/12/27.
 */
public class VerificationUtil {
    // 每位加权因子
    private static int power[] = { 7, 9, 10, 5, 8, 4, 2, 1, 6, 3, 7, 9, 10, 5, 8, 4, 2 };

    // 第18位校检码
    private static String verifyCode[] = { "1", "0", "X", "9", "8", "7", "6", "5",
            "4", "3", "2" };

    static int[] DAYS = { 0, 31, 29, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31 };

    public static boolean isValidate18Idcard(String idcard) {
        // 非18位为假
        if (idcard.length() != 18) {
            return false;
        }
        // 获取前17位
        String idcard17 = idcard.substring(0, 17);
        // 获取第18位
        String idcard18Code = idcard.substring(17, 18);
        char c[] = null;
        String checkCode = "";
        // 是否都为数字
        if (isDigital(idcard17)) {
            c = idcard17.toCharArray();
        } else {
            return false;
        }

        if (null != c) {
            int bit[] = new int[idcard17.length()];

            bit = converCharToInt(c);

            int sum17 = 0;

            sum17 = getPowerSum(bit);

            // 将和值与11取模得到余数进行校验码判断
            checkCode = getCheckCodeBySum(sum17);
            if (null == checkCode) {
                return false;
            }
            // 将身份证的第18位与算出来的校码进行匹配，不相等就为假
            if (!idcard18Code.equalsIgnoreCase(checkCode)) {
                return false;
            }
        }
        return true;
    }
    /**
     * 将和值与11取模得到余数进行校验码判断
     *
     * @param sum17
     * @return 校验位
     */
    public static String getCheckCodeBySum(int sum17) {
        String checkCode = null;
        switch (sum17 % 11) {
            case 10:
                checkCode = "2";
                break;
            case 9:
                checkCode = "3";
                break;
            case 8:
                checkCode = "4";
                break;
            case 7:
                checkCode = "5";
                break;
            case 6:
                checkCode = "6";
                break;
            case 5:
                checkCode = "7";
                break;
            case 4:
                checkCode = "8";
                break;
            case 3:
                checkCode = "9";
                break;
            case 2:
                checkCode = "x";
                break;
            case 1:
                checkCode = "0";
                break;
            case 0:
                checkCode = "1";
                break;
        }
        return checkCode;
    }

    /**
     * 将身份证的每位和对应位的加权因子相乘之后，再得到和值
     *
     * @param bit
     * @return
     */
    public static int getPowerSum(int[] bit) {

        int sum = 0;

        if (power.length != bit.length) {
            return sum;
        }

        for (int i = 0; i < bit.length; i++) {
            for (int j = 0; j < power.length; j++) {
                if (i == j) {
                    sum = sum + bit[i] * power[j];
                }
            }
        }
        return sum;
    }


    /**
     * 将字符数组转为整型数组
     *
     * @param c
     * @return
     * @throws NumberFormatException
     */
    public static int[] converCharToInt(char[] c) throws NumberFormatException {
        int[] a = new int[c.length];
        int k = 0;
        for (char temp : c) {
            a[k++] = Integer.parseInt(String.valueOf(temp));
        }
        return a;
    }
    /**
     * 数字验证
     *
     * @param str
     * @return
     */
    public static boolean isDigital(String str) {
        return str == null || "".equals(str) ? false : str.matches("^[0-9]*$");
    }


    /**
     * 检验是否为纯数字的字符串
     * @param str
     * @return
     */
    public static boolean onlyNumStr(String str){
        if(isEmpty(str)){
            return false;
        }
        String reg = "^([0-9]+)|((-?\\d+)(\\.\\d+)?)$";
        Pattern p = Pattern.compile(reg);
        Matcher m = p.matcher(str);
        return m.matches();
    }

    /**
     * 检验是否为手机号码的字符串
     * @param str
     * @return
     */
    public static boolean isMobileStr(String str){
        if(isEmpty(str)){
            return false;
        }
        String reg = "^1[3-8]{1}[0-9]{9}$";
        Pattern p = Pattern.compile(reg);
        Matcher m = p.matcher(str);
        return m.matches();
    }

    /**
     * 检验是否为固定电话的字符串
     * @param str
     * @return
     */
    public static boolean isTelephoneStr(String str){
        if(isEmpty(str)){
            return false;
        }
        String reg = "^((0\\d{2}|0\\d{3})-(\\d{7,8}))$|(0\\d{9,11})$";
        Pattern p = Pattern.compile(reg);
        Matcher m = p.matcher(str);
        return m.matches();
    }

    /**
     * 检验是否为邮箱的字符串
     * @param str
     * @return
     */
    public static boolean isEmailStr(String str){
        if(isEmpty(str)){
            return false;
        }
        String reg = "^\\w+([-+.']\\w+)*@\\w+([-.]\\w+)*\\.\\w+([-.]\\w+)*$";
        Pattern p = Pattern.compile(reg);
        Matcher m = p.matcher(str);
        return m.matches();
    }
    /**
     * 检验是否为网站的字符串
     * @param str
     * @return
     */
    public static boolean isWebStr(String str){
        if(isEmpty(str)){
            return false;
        }
        String reg = "^(http|ftp|https):\\/\\/[\\w\\-_]+(\\.[\\w\\-_]+)+([\\w\\-\\.,@?^=%&:/~\\+#]*[\\w\\-\\@?^=%&/~\\+#])?";
        Pattern p = Pattern.compile(reg);
        Matcher m = p.matcher(str);
        return m.matches();
    }
    /**
     * 检验是否为邮编的字符串
     * @param str
     * @return
     */
    public static boolean isPostcodeStr(String str){
        if(isEmpty(str)){
            return false;
        }
        String reg = "^[0-9]{6}$";
        Pattern p = Pattern.compile(reg);
        Matcher m = p.matcher(str);
        return m.matches();
    }

    /**
     * 字符是否为空或"null"
     * @param str
     * @return
     */
    public static boolean isEmpty(String str){
        if (str == null) return true;
        if ("null".equalsIgnoreCase(str)) return true;
        if (str.isEmpty()) return true;
        return false;
    }

    /**
     * list是否为空
     * @param <T>
     * @param list
     * @return
     */
    public static <T> boolean isEmpty(List<T> list){
        if (list == null) return true;
        if (list.size() < 1) return true;
        if (list.isEmpty()) return true;
        return false;
    }

    public static boolean isEmptyHashMap(HashMap<String, Object> values){
        if (values == null) return true;
        if (values.size()< 1) return true;
        if (values.isEmpty()) return true;
        return false;

    }

    /**
     * 判断字符串是否只有数字及字母
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
     * 判断是否为整数
     * @param str
     * @return
     */
    public static boolean isInteger(String str) {
        try {
            Integer.parseInt(str);
            return true;
        }
        catch (NumberFormatException e) {
        }
        return false;
    }

    /**
     * 转换对象为字符串类型
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
        if ( obj instanceof BigDecimal) {
            BigDecimal bigObj = (BigDecimal)obj;
            return bigObj.toString();
        }

        return obj.toString().trim();
    }


    /**
     * 校验List<Map<String,Object>中字段是否为空
     * @param keyParams 入参名称，字符串数组
     * @param list	传入的参数
     * @return
     */
    public static Response checkParamEmptyList(String[] keyParams, List<Map<String,Object>> list, Response result){
        for (String key : keyParams) {
            if(isEmpty(valueOf(list.get(0).get(key)))){
                result.setFlag("0");
                result.setMsg("数据有误:字段["+key+"]为空");
                return result;
            }
        }
        result.setFlag("1");
        result.setMsg("查询成功");
        return result;
    }

    // 判断是否为空
    public static boolean isNull(Object obj) {
        if(obj == null) return true;

        if(obj instanceof String){
            String strObj = valueOf(obj);
            return "".equals(strObj);
        }
        return false;
    }
    /**
     * @param date yyyy-MM-dd HH:mm:ss
     * @return
     */
    public static boolean isValidDate(String date) {
        try {
            int year = Integer.parseInt(date.substring(0, 4));
            if (year <= 0)
                return false;
            int month = Integer.parseInt(date.substring(4, 6));
            if (month <= 0 || month > 12)
                return false;
            int day = Integer.parseInt(date.substring(6, 8));
            if (day <= 0 || day > DAYS[month])
                return false;
            if (month == 2 && day == 29 && !isGregorianLeapYear(year)) {
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public static final boolean isGregorianLeapYear(int year) {
        return year % 4 == 0 && (year % 100 != 0 || year % 400 == 0);
    }
  /*  *//**
     * 功能：将页面js加密的密码转换为金证公司的加密方式
     * @param cifApplyMap
     * @return
     * @throws Exception
     *//*
    public static String jsPwd2KdPwd(Map<String, Object> cifApplyMap) throws Exception {
        if(cifApplyMap.get("password")==null || String.valueOf(cifApplyMap.get("password")).trim().equals(""))
            return null;
        String password = String.valueOf(cifApplyMap.get("password"));

        String customerno=String.valueOf(cifApplyMap.get("customerno"));

        //解密
        password =DESEncrypt.strDec(password, null, null, null);

        //金证加密
        HashMap<String, Object> paramMap = new HashMap<String, Object>();
        paramMap.put("srcdata", password);
        paramMap.put("keydata", customerno);
        IResult rs = new Result();
        encode(paramMap,rs);
        if(rs.getErrorCode().equals(Constant.WS_ERROR_FAILURE)) {
            throw new WsException(customerno+"用户密码加密失败！"+rs.getErrorMessage());
        }
        String kingdomPwd = String.valueOf(((Map) ((List)rs.getResult()).get(0)).get("destdata"));
        return kingdomPwd;
    }*/
   /* *//**
     * 金证加密
     * @param paramMap
     * @param rs
     *//*
    public static void encode(Map<String,Object> paramMap,IResult rs){
        if(paramMap.get("srcdata")==null||"".equals(paramMap.get("srcdata").toString())||paramMap.get("keydata")==null||"".equals(paramMap.get("keydata").toString())){
            rs.failed("入参错误、请检查srcdata和keydata是否正确");
            return;
        }
        KDEncodeCli cli = new KDEncodeCli();
        String srcdata=paramMap.get("srcdata").toString();
        String keydata=paramMap.get("keydata").toString();
        String destdata = cli.KDEncode(6, srcdata, keydata);
        rs.setErrorCode(Constant.WS_ERROR_SUCCESS);
        rs.setErrorMessage("加密成功");
        rs.setResType(Constant.WS_TYPE_LISTMAP);
        List<Map<String,Object>> rsList=new ArrayList<Map<String,Object>>();
        Map<String,Object> rsMap=new HashMap<String,Object>();
        rsMap.put("destdata", destdata);
        rsList.add(rsMap);
        rs.setResult(rsList);
    }*/
    /**
     * 必填项校验
     * @param mapParam
     * @param array 必填项key数组
     * @param rs
     */
    public static void cheakMustParams(HashMap<String, Object> mapParam,
                                       String[] array, Response rs) {
        if (mapParam.size() <= 0) {
            /*ResultUtil.makerErsResults("参数异常!", rs);*/
            return;
        }

        for (int i = 0; i < array.length; i++) {
            if (VerificationUtil.isEmpty(valueOf(mapParam.get(array[i])))) {
                /*ResultUtil.makerErsResults("参数" + array[i] + "为空", rs);*/
                return;
            }
        }
        /*ResultUtil.makerSusResults("必填项验证通过!", rs);*/
        return;

    }

}
