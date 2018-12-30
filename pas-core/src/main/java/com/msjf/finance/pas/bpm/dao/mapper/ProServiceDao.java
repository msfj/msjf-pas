package com.msjf.finance.pas.bpm.dao.mapper;

import com.msjf.finance.pas.bpm.entity.ProServiceEntity;
import com.msjf.finance.pas.common.dao.MyBatisDao;

import java.util.List;
import java.util.Map;

@MyBatisDao
public interface ProServiceDao {

    List<Map> queryProServiceList(Map<String, Object> mapParams);

    void updateServicePro(Map<String, Object> mapParams);
}
