package com.msjf.finance.pas.bpm.dao.mapper;



import com.msjf.finance.msjf.core.dao.MyBatisDao;

import java.util.List;
import java.util.Map;

/**
 * Created by 成俊平 on 2019/1/4.
 */

@MyBatisDao
public interface CustProStateDao {

    List<Map> queryCustProStateList(Map<String, Object> mapParams);

    void addCustProState(Map<String, Object> mapParams);

    void updateCustProState(Map<String, Object> mapParams);
}
