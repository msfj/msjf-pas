package com.msjf.finance.pas.bpm.facade;

import com.msjf.finance.msjf.core.response.Response;
import com.msjf.finance.pas.bpm.service.ProcessDefinitionService;
import com.msjf.finance.pas.facade.act.GetDiagramFacade;
import com.msjf.finance.pas.facade.act.domain.ProDefProInsDomain;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

@Service("getDiagramFacade")
public class GetDiagramFacadeImpl implements GetDiagramFacade {

    @Resource
    ProcessDefinitionService processDefinitionService;

    @Override
    public Response getDiagram(ProDefProInsDomain proDefProInsDomain) {
        Map<String,Object> map =new HashMap<>();
        map.put("processDefinitionId",proDefProInsDomain.getProcessDefinitionId());
        map.put("processInstanceId",proDefProInsDomain.getProcessInstanceId());
        return processDefinitionService.getDiagram(map);
    }
}
