package com.msjf.finance.pas.bpm.service;

import com.msjf.finance.msjf.core.response.Response;
import com.msjf.finance.pas.bpm.entity.ProStepAuditEntity;

import java.util.List;
import java.util.Map;

public interface ProStepAuditService {
    /**
     * 查询流程节点审核人
     * @param mapParams
     * @return
     */
    Response queryAuditorList(Map<String, Object> mapParams) throws RuntimeException;

    Response updateAuditorList(Map<String, Object> mapParams) throws RuntimeException;
}
