package com.msjf.finance.pas.bpm.facade;

import com.msjf.finance.msjf.core.response.Response;
import com.msjf.finance.pas.bpm.service.ProcessDefinitionService;
import com.msjf.finance.pas.facade.act.ConvertToModelFacade;
import com.msjf.finance.pas.facade.act.domain.ProDefDomain;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

@Service("convertToModelFacade")
public class ConvertToModelFacadeImpl implements ConvertToModelFacade {

    @Resource
    ProcessDefinitionService processDefinitionService;

    @Override
    public Response convertToModel(ProDefDomain proDefDomain) {
        Map<String,Object> map =new HashMap<>();
        map.put("processDefinitionId",proDefDomain.getProcessDefinitionId());
        return processDefinitionService.convertToModel(map);
    }
}
