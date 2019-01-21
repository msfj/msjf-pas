package com.msjf.finance.pas.bpm.service;

import com.msjf.finance.msjf.core.response.Response;

import java.util.Map;

public interface PasHisService {

    /**
     * 待审核查询
     *
     * @param mapParams
     */
    public Response queryPasHisServiceList(Map<String, Object> mapParams) throws RuntimeException;
}
