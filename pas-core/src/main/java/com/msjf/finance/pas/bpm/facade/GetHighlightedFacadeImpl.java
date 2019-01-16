package com.msjf.finance.pas.bpm.facade;

import com.msjf.finance.msjf.core.response.Response;
import com.msjf.finance.pas.bpm.service.ProcessDefinitionService;
import com.msjf.finance.pas.facade.act.GetHighlightedFacade;
import com.msjf.finance.pas.facade.act.domain.ProInsDomain;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

@Service("getHighlightedFacade")
public class GetHighlightedFacadeImpl implements GetHighlightedFacade {

    @Resource
    ProcessDefinitionService processDefinitionService;

    @Override
    public Response getHighlighted(ProInsDomain proInsDomain) {
        Map<String,Object> map =new HashMap<>();
        map.put("processInstanceId",proInsDomain.getProcessInstanceId());
        return processDefinitionService.getHighlighted(map);
    }
}
