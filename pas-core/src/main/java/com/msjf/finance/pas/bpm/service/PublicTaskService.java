package com.msjf.finance.pas.bpm.service;



import com.msjf.finance.msjf.core.response.Response;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by chengjunping on 2018/12/28.
 */
public interface PublicTaskService {

    /**
     * 发起流程
     * @param mapParam
     */
    public Response createFlow(Map<String, Object> mapParam) throws RuntimeException;


    /**
     * 跳转下一节点
     * @param mapParam
     * @throws RuntimeException
     */
    public Response executeNextStep(Map<String, Object> mapParam) throws Exception;

}
