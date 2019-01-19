package com.msjf.finance.pas.facade.act;

import com.msjf.finance.msjf.core.response.Response;
import com.msjf.finance.pas.facade.act.domain.ExcuteFlowDomain;

public interface ExcuteFlowFacade {

    Response excuteFlow(ExcuteFlowDomain excuteFlowDomain) throws Exception;
}
