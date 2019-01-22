package com.msjf.finance.pas.facade.act;

import com.msjf.finance.msjf.core.response.Response;
import com.msjf.finance.pas.facade.act.domain.TaskIdDomain;

public interface GetAllNextTaskFacade {

    Response getAllNextTask(TaskIdDomain taskIdDomain);
}
