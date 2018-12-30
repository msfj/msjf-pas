package com.msjf.finance.pas.bpm.service.activity;

import com.msjf.finance.pas.bpm.dao.mapper.ProServiceDao;
import com.msjf.finance.pas.bpm.entity.ProServiceEntity;
import com.msjf.finance.pas.bpm.service.ProService;
import com.msjf.finance.pas.common.StringUtil;
import com.msjf.finance.pas.common.response.Response;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

@Service("proService")
public class ProServiceImpl implements ProService {

    @Resource
    ProServiceDao proServiceDao;

    @Override
    public void queryProServiceList(Map<String, Object> mapParams, Response rs) {
        try{
            List<Map> list = proServiceDao.queryProServiceList(mapParams);
            System.out.println(list.toString());
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void updateProService(Map<String, Object> mapParams, Response rs) {
        if(StringUtil.isNull(mapParams.get("proDefKey"))){
           return;
        }
        if(StringUtil.isNull(mapParams.get("serviceFlag"))){
            return;
        }
        try{
             proServiceDao.updateServicePro(mapParams);
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
