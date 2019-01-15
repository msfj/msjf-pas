package com.msjf.finance.pas.bpm.service.activity;

import com.msjf.finance.msjf.core.response.Response;
import com.msjf.finance.pas.bpm.dao.mapper.CustProStateDao;
import com.msjf.finance.pas.bpm.service.CustProStateService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

/**
 * Created by Jsan on 2019/1/4.
 */

@Service("custProStateService")
public class CustProStateServiceImpl implements CustProStateService {

    @Resource
    CustProStateDao custProStateDao;

    @Override
    public List queryCustProStateList(Map<String, Object> mapParams) {
        try {
            List<Map> custEntityList = custProStateDao.queryCustProStateList(mapParams);

            return custEntityList;
        }catch (Exception e){
            //打印错误日志
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public void addCustProStateList(Map<String, Object> mapParams, Response rs) {

    }

    @Override
    public void updateCustProStateList(Map<String, Object> mapParams, Response rs) {

    }
}
