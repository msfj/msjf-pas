package com.msjf.finance.pas.bpm.service;



import com.msjf.finance.msjf.core.response.Response;

import java.util.Map;

public interface ProService {

    /**
     * 查询业务绑定流程信息列表
     *
     * @param mapParams
     */
    public Response queryProServiceList(Map<String, Object> mapParams) throws RuntimeException;



    public Response updateProService(Map<String, Object> mapParams)throws RuntimeException;
}
