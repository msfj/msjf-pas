package com.msjf.finance.pas.bpm.dao.mapper;

import com.msjf.finance.msjf.core.dao.MyBatisDao;
import com.msjf.finance.pas.bpm.entity.ProServiceEntity;

import java.util.List;
import java.util.Map;

@MyBatisDao
public interface ProServiceDao {

    List<Map> queryProServiceList(Map<String, Object> mapParams);

    void updateServicePro(Map<String, Object> mapParams);
}
