package com.msjf.finance.pas.bpm.service.activity;

import com.msjf.finance.msjf.core.response.Response;
import com.msjf.finance.pas.bpm.dao.mapper.PasProTodoDao;
import com.msjf.finance.pas.bpm.entity.PasProTodoEntity;
import com.msjf.finance.pas.bpm.service.ProTodoService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;


/**
 * Created by cehngjunping on 2018/01/21.
 */
@Service("proTodoService")
public class ProTodoServiceImpl implements ProTodoService {

    @Resource
    PasProTodoDao todoDao;

    @Override
    public Response queryProTodoServiceList(Map<String, Object> mapParams) throws RuntimeException{
        Response response = new Response();
        try{
            List<PasProTodoEntity> list = todoDao.queryPasProTodoList(mapParams);
            response.success("1","查询成功",list);
        }catch (Exception e){
            e.printStackTrace();
            response.fail("0","查询失败"+e.getMessage());
        }
        return response;
    }
}
