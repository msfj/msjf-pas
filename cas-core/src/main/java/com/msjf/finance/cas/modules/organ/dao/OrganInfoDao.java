package com.msjf.finance.cas.modules.organ.dao;
import com.msjf.finance.cas.common.dao.MyBatisDao;
import com.msjf.finance.cas.modules.organ.entity.OrganInfoEntity;
import java.util.List;

/**
 * Created by 11509 on 2018/12/18.
 */
@MyBatisDao
public interface OrganInfoDao{
    List<OrganInfoEntity> queryOrganInfoList();
}
