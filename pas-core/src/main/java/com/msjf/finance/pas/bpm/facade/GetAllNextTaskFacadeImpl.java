package com.msjf.finance.pas.bpm.facade;

import com.msjf.finance.msjf.core.response.Response;
import com.msjf.finance.pas.bpm.service.KbpmTaskService;
import com.msjf.finance.pas.facade.act.GetAllNextTaskFacade;
import com.msjf.finance.pas.facade.act.domain.TaskIdDomain;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

@Service("getAllNextTaskFacade")
public class GetAllNextTaskFacadeImpl implements GetAllNextTaskFacade {

    @Resource
    KbpmTaskService kbpmTaskService;

    @Override
    public Response getAllNextTask(TaskIdDomain taskIdDomain) {

        Map<String,Object> map =new HashMap<>();
        map.put("taskId",taskIdDomain.getTaskId());

        return kbpmTaskService.getAllNextTask(map);
    }
}
