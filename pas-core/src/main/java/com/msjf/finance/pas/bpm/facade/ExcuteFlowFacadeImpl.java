package com.msjf.finance.pas.bpm.facade;

import com.msjf.finance.msjf.core.response.Response;
import com.msjf.finance.pas.bpm.service.PublicTaskService;
import com.msjf.finance.pas.facade.act.ExcuteFlowFacade;
import com.msjf.finance.pas.facade.act.domain.ExcuteFlowDomain;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

@Service("excuteFlowFacade")
public class ExcuteFlowFacadeImpl implements ExcuteFlowFacade {

    @Resource
    PublicTaskService publicTaskService;

    @Override
    public Response excuteFlow(ExcuteFlowDomain excuteFlowDomain) throws Exception {

        Map<String,Object> map =new HashMap<>();
        map.put("processInstanceId",excuteFlowDomain.getProcessInstanceId());
        map.put("userId",excuteFlowDomain.getUserId());
        map.put("userName",excuteFlowDomain.getUserName());
        map.put("custName",excuteFlowDomain.getCustName());
        map.put("custNo",excuteFlowDomain.getCustNo());
        map.put("comment",excuteFlowDomain.getComment());
        map.put("taskId",excuteFlowDomain.getTaskId());
        map.put("approve",excuteFlowDomain.getApprove());
        map.put("fileUrls",excuteFlowDomain.getFileUrls());
        map.put("taskDefinitionKey",excuteFlowDomain.getTaskDefinitionKey());
        return publicTaskService.executeNextStep(map);
    }
}
