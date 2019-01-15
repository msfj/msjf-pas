package com.msjf.finance.pas.modules.organ.dao;



import com.msjf.finance.msjf.core.dao.MyBatisDao;
import com.msjf.finance.pas.modules.organ.entity.CustEntity;

import java.util.List;

@MyBatisDao
public interface CustInfoDao {

    List<CustEntity> queryCustInfoList();
}
