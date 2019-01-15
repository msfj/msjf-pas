package com.msjf.finance.pas.bpm.dao.mapper;

import com.msjf.finance.msjf.core.dao.MyBatisDao;
import com.msjf.finance.pas.bpm.entity.ProStepAuditEntity;

import java.util.List;
import java.util.Map;

@MyBatisDao
public interface ProStepAuditDao {

    List<ProStepAuditEntity> queryAuditorList(Map<String, Object> mapParams);

    void addAuditorList(Map<String, Object> mapParams);

    void delAuditorList(Map<String, Object> mapParams);
}
