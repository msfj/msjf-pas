package com.msjf.finance.pas.bpm.service;

import com.msjf.finance.pas.common.response.Response;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by chengjunping on 2018/12/28.
 */
public interface PublicTaskService {

    /**
     * 发起流程
     * @param mapParam
     * @param result
     */
    public void createFlow(Map<String, Object> mapParam, Response result);

}
