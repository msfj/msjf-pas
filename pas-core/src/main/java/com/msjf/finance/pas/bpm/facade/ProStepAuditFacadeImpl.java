package com.msjf.finance.pas.bpm.facade;

import com.msjf.finance.msjf.core.response.Response;
import com.msjf.finance.pas.bpm.service.ProStepAuditService;
import com.msjf.finance.pas.facade.act.ProStepAuditFacade;
import com.msjf.finance.pas.facade.act.domain.ProStepAuDomain;
import com.msjf.finance.pas.facade.act.domain.ProStepAuditDomain;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

@Service("proStepAuditFacade")
public class ProStepAuditFacadeImpl implements ProStepAuditFacade {

    @Resource
    ProStepAuditService stepAuditService;

    @Override
    public Response setSetAudit(ProStepAuditDomain stepAuditDomain) throws RuntimeException {
        Map<String,Object> map =new HashMap<>();
        map.put("list",stepAuditDomain.getList());
        return stepAuditService.updateAuditorList(map);
    }

    @Override
    public Response queryAudit(ProStepAuDomain proStepAuDomain) throws RuntimeException {
        Map<String,Object> map =new HashMap<>();
        map.put("areaNo",proStepAuDomain.getAreaNo());
        map.put("proDefKey",proStepAuDomain.getProDefKey());
        map.put("stepId",proStepAuDomain.getStepId());
        return stepAuditService.queryAuditorList(map);
    }
}
