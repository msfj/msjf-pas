package com.msjf.finance.cas.common.response;

import com.alibaba.fastjson.JSON;

import java.io.Serializable;

/**
 * Created by 11509 on 2018/12/19.
 * 公共方法，所有的domain都需要继承BaseDomain
 */
public class BaseDomain implements Serializable {
    @Override
    public String toString() {
        return JSON.toJSONString(this);
    }
}
