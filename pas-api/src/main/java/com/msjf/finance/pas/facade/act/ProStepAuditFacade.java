package com.msjf.finance.pas.facade.act;

import com.msjf.finance.msjf.core.response.Response;
import com.msjf.finance.pas.facade.act.domain.ProStepAuDomain;
import com.msjf.finance.pas.facade.act.domain.ProStepAuditDomain;

public interface ProStepAuditFacade {

    Response setSetAudit(ProStepAuditDomain stepAuditDomain) throws RuntimeException;

    Response queryAudit(ProStepAuDomain proStepAuDomain) throws RuntimeException;
}
