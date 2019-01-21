package com.msjf.finance.pas.bpm.facade;


import com.msjf.finance.msjf.core.response.Response;
import com.msjf.finance.pas.bpm.service.ProTodoService;
import com.msjf.finance.pas.facade.act.TodoFacade;
import com.msjf.finance.pas.facade.act.domain.TodoDomain;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

@Service("todoFacade")
public class TodoFacadeImpl implements TodoFacade {
    @Resource
    ProTodoService proTodoService;

    @Override
    public Response queryTodoList(TodoDomain todoDomain) {

        Map<String,Object> map =new HashMap<>();
        map.put("actId",todoDomain.getActId());
        map.put("auditorId",todoDomain.getAuditorId());
        map.put("actName",todoDomain.getActName());
        map.put("proDefName",todoDomain.getProDefName());
        int pageSize = todoDomain.getPageSize();
        int pageNumber = todoDomain.getPageNumber();
        int firstResult = pageSize * (pageNumber - 1);
        map.put("pageSize",pageSize);
        map.put("pageNumber",todoDomain.getPageNumber());
        map.put("currIndex",firstResult);
        return proTodoService.queryProTodoServiceList(map);
    }
}
