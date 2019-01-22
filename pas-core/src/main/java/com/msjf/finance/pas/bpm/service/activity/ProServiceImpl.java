package com.msjf.finance.pas.bpm.service.activity;

import com.msjf.finance.msjf.core.response.Response;
import com.msjf.finance.pas.bpm.dao.mapper.ProServiceDao;
import com.msjf.finance.pas.bpm.entity.ProServiceEntity;
import com.msjf.finance.pas.bpm.service.ProService;
import com.msjf.finance.pas.common.StringUtil;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

@Service("proService")
public class ProServiceImpl implements ProService {

    @Resource
    ProServiceDao proServiceDao;

    @Override
    public Response queryProServiceList(Map<String, Object> mapParams) {
        Response response = new Response();
        try{
            List<Map> list = proServiceDao.queryProServiceList(mapParams);
            response.success("1","查询成功",list);
        }catch (Exception e){
            e.printStackTrace();
            response.fail("0","查询失败"+e.getMessage());
        }
        return response;
    }

    @Override
    public Response updateProService(Map<String, Object> mapParams) {
        Response response = new Response();
        if(StringUtil.isNull(mapParams.get("proDefKey"))){
           return response.fail("0","proDefKey不能为空");
        }
        if(StringUtil.isNull(mapParams.get("serviceFlag"))){
            return response.fail("0","serviceFlag不能为空");
        }
        try{
             proServiceDao.updateServicePro(mapParams);
             response.success("1","设置成功",null);
        }catch (Exception e){
            e.printStackTrace();
            response.fail("0","设置失败"+e.getMessage());
        }
        return  response;
    }
}
