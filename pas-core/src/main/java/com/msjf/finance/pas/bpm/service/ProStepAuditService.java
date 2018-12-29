package com.msjf.finance.pas.bpm.service;

import com.msjf.finance.pas.bpm.entity.ProStepAuditEntity;

import java.util.List;
import java.util.Map;

public interface ProStepAuditService {
    /**
     * 查询流程节点审核人
     * @param mapParams
     * @return
     */
    void queryAuditorList(Map<String, Object> mapParams);

    void addAuditorList(Map<String, Object> mapParams);

    void updateAuditorList(Map<String, Object> mapParams);
}
