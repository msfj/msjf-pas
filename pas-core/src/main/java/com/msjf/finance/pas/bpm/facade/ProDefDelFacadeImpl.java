package com.msjf.finance.pas.bpm.facade;

import com.msjf.finance.msjf.core.response.Response;
import com.msjf.finance.pas.bpm.service.ProcessDefinitionService;
import com.msjf.finance.pas.facade.act.ProDefDelFacade;
import com.msjf.finance.pas.facade.act.domain.ProDefDomain;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

@Service("proDefDelFacade")
public class ProDefDelFacadeImpl implements ProDefDelFacade {

    @Resource
    ProcessDefinitionService processDefinitionService;

    @Override
    public Response proDefDel(ProDefDomain proDefDomain) {

        Map<String,Object> map =new HashMap<>();
        map.put("processDefinitionId",proDefDomain.getProcessDefinitionId());
        return processDefinitionService.delete(map);
    }
}
