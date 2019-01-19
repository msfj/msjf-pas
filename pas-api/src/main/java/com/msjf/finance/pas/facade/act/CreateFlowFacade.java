package com.msjf.finance.pas.facade.act;

import com.msjf.finance.msjf.core.response.Response;
import com.msjf.finance.pas.facade.act.domain.CreateFlowDomain;

public interface CreateFlowFacade {

    Response createFlow(CreateFlowDomain createFlowDomain) throws Exception;

}
