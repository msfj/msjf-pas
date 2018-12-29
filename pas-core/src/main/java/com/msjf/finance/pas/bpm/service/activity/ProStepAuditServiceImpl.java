package com.msjf.finance.pas.bpm.service.activity;


import com.msjf.finance.pas.bpm.dao.mapper.ProStepAuditDao;
import com.msjf.finance.pas.bpm.entity.ProStepAuditEntity;
import com.msjf.finance.pas.bpm.service.ProStepAuditService;
import com.msjf.finance.pas.common.StringUtil;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service("proStepAuditService")
public class ProStepAuditServiceImpl implements ProStepAuditService {

    @Resource
    ProStepAuditDao proStepAuditDao;

    @Override
    public void queryAuditorList(Map<String, Object> mapParams) {
        if(StringUtil.isNull(mapParams.get("proDefKey"))){
            return;
        }
        if(StringUtil.isNull(mapParams.get("stepId"))){
            return;
        }
        if(StringUtil.isNull(mapParams.get("areaNo"))){
            return;
        }
        try{
            List<ProStepAuditEntity> list = proStepAuditDao.queryAuditorList(mapParams);
            System.out.println(list.toString());
        }catch (Exception e){
            //打印错误日志
            e.printStackTrace();
        }
    }

    @Override
    public void addAuditorList(Map<String, Object> mapParams) {
        try{
            proStepAuditDao.addAuditorList(mapParams);

        }catch (Exception e){
            //打印错误日志
            e.printStackTrace();
        }
    }

    @Override
    public void updateAuditorList(Map<String, Object> mapParams) {
        try{
            List<Map> list = (List<Map>)mapParams.get("list");
            Map<String,Object> parMap = new HashMap<String,Object>();
            parMap.put("proDefKey",list.get(0).get("proDefKey"));
            parMap.put("stepId",list.get(0).get("stepId"));
            parMap.put("areaNo",list.get(0).get("areaNo"));
            proStepAuditDao.delAuditorList(parMap);
            proStepAuditDao.addAuditorList(mapParams);

        }catch (Exception e){
            //打印错误日志
            e.printStackTrace();
        }
    }
}
