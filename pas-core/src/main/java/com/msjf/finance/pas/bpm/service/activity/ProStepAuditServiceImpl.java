package com.msjf.finance.pas.bpm.service.activity;


import com.msjf.finance.msjf.core.response.Response;
import com.msjf.finance.pas.bpm.dao.mapper.ProStepAuditDao;
import com.msjf.finance.pas.bpm.entity.ProStepAuditEntity;
import com.msjf.finance.pas.bpm.service.ProStepAuditService;
import com.msjf.finance.pas.common.StringUtil;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service("proStepAuditService")
public class ProStepAuditServiceImpl implements ProStepAuditService {

    @Resource
    ProStepAuditDao proStepAuditDao;

    @Override
    public Response queryAuditorList(Map<String, Object> mapParams) throws RuntimeException{
        Response response = new Response();
        if(StringUtil.isNull(mapParams.get("proDefKey"))){
            return response.fail("0","proDefKey不能为空");
        }
        if(StringUtil.isNull(mapParams.get("stepId"))){
            return response.fail("0","stepId不能为空");
        }
        if(StringUtil.isNull(mapParams.get("areaNo"))){
            return response.fail("0","areaNo不能为空");
        }
        try{
            List<ProStepAuditEntity> list = proStepAuditDao.queryAuditorList(mapParams);
            response.success("1","查询成功",list);
        }catch (Exception e){
            //打印错误日志
            e.printStackTrace();
            response.fail("0","查询失败");
        }
        return response;
    }


    @Override
    public Response updateAuditorList(Map<String, Object> mapParams) throws RuntimeException{
        Response response =new Response();
        try{
            List<Map> list = (List<Map>)mapParams.get("list");
            if(ObjectUtils.isEmpty(list)){
                return response.fail("0","审核人参数不能为空");
            }
            if(StringUtil.isNull(list.get(0).get("proDefKey"))){
                return response.fail("0","proDefKey不能为空");
            }
            if(StringUtil.isNull(list.get(0).get("stepId"))){
                return response.fail("0","stepId不能为空");
            }
            if(StringUtil.isNull(list.get(0).get("areaNo"))){
                return response.fail("0","areaNo不能为空");
            }
            Map<String,Object> parMap = new HashMap<String,Object>();
            parMap.put("proDefKey",list.get(0).get("proDefKey"));
            parMap.put("stepId",list.get(0).get("stepId"));
            parMap.put("areaNo",list.get(0).get("areaNo"));
            proStepAuditDao.delAuditorList(parMap);
            proStepAuditDao.addAuditorList(mapParams);
            response.success("1","设置审核人成功",list.get(0).get("proDefKey"));
        }catch (Exception e){
            //打印错误日志
            e.printStackTrace();
            response.fail("0","设置审核人失败");
        }
        return  response;
    }
}
