package com.msjf.finance.pas.bpm.service;

import com.msjf.finance.pas.common.response.Response;

import java.util.Map;

public interface ProService {

    /**
     * 查询业务绑定流程信息列表
     *
     * @param mapParams
     * @param rs
     */
    public void queryProServiceList(Map<String, Object> mapParams, Response rs);



    public void updateProService(Map<String, Object> mapParams, Response rs);
}
