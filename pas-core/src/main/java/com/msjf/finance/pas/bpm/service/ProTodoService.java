package com.msjf.finance.pas.bpm.service;

import com.msjf.finance.msjf.core.response.Response;

import java.util.Map;

public interface ProTodoService {

    /**
     * 待审核查询
     *
     * @param mapParams
     */
    public Response queryProTodoServiceList(Map<String, Object> mapParams) throws RuntimeException;

}
