package com.msjf.finance.pas.modules.organ.dao;
import com.msjf.finance.msjf.core.dao.MyBatisDao;
import com.msjf.finance.pas.modules.organ.entity.OrganInfoEntity;

import java.util.List;

/**
 * Created by 11509 on 2018/12/18.
 */
@MyBatisDao
public interface OrganInfoDao{
    List<OrganInfoEntity> queryOrganInfoList();
}
