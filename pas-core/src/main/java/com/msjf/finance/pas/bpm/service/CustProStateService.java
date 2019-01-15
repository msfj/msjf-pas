package com.msjf.finance.pas.bpm.service;
import com.msjf.finance.msjf.core.response.Response;

import java.util.List;
import java.util.Map;

/**
 * Created by Jsan on 2019/1/4.
 */
public interface CustProStateService {

    /**
     * 查询企业流程基本信息
     * @return
     */
    List queryCustProStateList(Map<String, Object> mapParams);


    /**
     * 增加企业流程基本信息
     * @return
     */
    void addCustProStateList(Map<String, Object> mapParams, Response rs);

    /**
     * 修改企业流程基本信息
     * @return
     */
    void updateCustProStateList(Map<String, Object> mapParams, Response rs);
}
