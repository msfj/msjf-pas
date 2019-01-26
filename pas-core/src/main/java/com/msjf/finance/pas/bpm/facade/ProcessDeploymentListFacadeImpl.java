package com.msjf.finance.pas.bpm.facade;

import com.msjf.finance.msjf.core.response.Response;
import com.msjf.finance.pas.bpm.service.ProcessDefinitionService;
import com.msjf.finance.pas.facade.act.ProcessDeploymentListFacade;
import com.msjf.finance.pas.facade.act.domain.ProDeployPageDomain;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

@Service("processDeploymentListFacade")
public class ProcessDeploymentListFacadeImpl implements ProcessDeploymentListFacade {

    @Resource
    ProcessDefinitionService processDefinitionService;


    @Override
    public Response processDeploymentList(ProDeployPageDomain proDeployPageDomain) {
        Map<String,Object> map =new HashMap<>();
        map.put("lastVersion",proDeployPageDomain.getLastVersion());
        map.put("name",proDeployPageDomain.getName());
        map.put("pageNumber",proDeployPageDomain.getPageNum());
        map.put("pageSize",proDeployPageDomain.getPageSize());
        map.put("state",proDeployPageDomain.getState());
        return processDefinitionService.listProcessDefinitionByStateAndPage(map);
    }
}
