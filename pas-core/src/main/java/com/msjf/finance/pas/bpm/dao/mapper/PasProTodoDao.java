package com.msjf.finance.pas.bpm.dao.mapper;

import com.msjf.finance.msjf.core.dao.MyBatisDao;
import com.msjf.finance.pas.bpm.entity.PasProTodoEntity;

import java.util.List;
import java.util.Map;

/**
 * Created by Jsan on 2019/1/4.
 */

@MyBatisDao
public interface PasProTodoDao {

    List<PasProTodoEntity> queryPasProTodoList(Map<String, Object> mapParams);

    void addPasProTodoList(Map<String, Object> mapParams);

    void delPasProTodoList(Map<String, Object> mapParams);
}
