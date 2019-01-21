package com.msjf.finance.pas.bpm.service.activity;

import com.msjf.finance.msjf.core.response.Response;
import com.msjf.finance.pas.bpm.dao.mapper.PasHisProcessinstanceDao;
import com.msjf.finance.pas.bpm.entity.PasHisProcessinstanceEntity;
import com.msjf.finance.pas.bpm.service.PasHisService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

@Service("pasHisService")
public class PasHisServiceImpl implements PasHisService {

    @Resource
    PasHisProcessinstanceDao pasHisProcessinstanceDao;


    @Override
    public Response queryPasHisServiceList(Map<String, Object> mapParams) throws RuntimeException {
        Response re = new Response();
        try{
            List<PasHisProcessinstanceEntity> list = pasHisProcessinstanceDao.queryPasHisProcessinstance(mapParams);
            re.success("1","查询成功",list);
        }catch (Exception e){
            e.printStackTrace();
            re.fail("0","查询失败");
        }

        return re;
    }
}
