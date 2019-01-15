package com.msjf.finance.pas.bpm.dao.mapper;

import com.msjf.finance.msjf.core.dao.MyBatisDao;
import com.msjf.finance.pas.bpm.entity.PasHisProcessinstanceEntity;

import java.util.List;
import java.util.Map;

/**
 * Created by Jsan on 2019/1/4.
 */
@MyBatisDao
public interface PasHisProcessinstanceDao {

    List<PasHisProcessinstanceEntity> queryPasHisProcessinstance(Map<String, Object> mapParams);

    void addPasHisProcessinstance(Map<String, Object> mapParams);
}
