package com.msjf.finance.pas.bpm.facade;

import com.msjf.finance.msjf.core.response.Response;
import com.msjf.finance.pas.bpm.service.PublicTaskService;
import com.msjf.finance.pas.facade.act.CreateFlowFacade;
import com.msjf.finance.pas.facade.act.domain.CreateFlowDomain;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;


@Service("createFlowFacade")
public class CreateFlowFacadeImpl implements CreateFlowFacade {

    @Resource
    PublicTaskService publicTaskService;

    @Override
    public Response createFlow(CreateFlowDomain createFlowDomain) throws Exception {
        Map<String,Object> map =new HashMap<>();
        map.put("processDefinitionId",createFlowDomain.getProcessDefinitionId());
        map.put("userId",createFlowDomain.getUserId());
        map.put("userName",createFlowDomain.getUserName());
        map.put("custName",createFlowDomain.getCustName());
        map.put("custNo",createFlowDomain.getCustNo());

        return publicTaskService.createFlow(map);
    }
}
