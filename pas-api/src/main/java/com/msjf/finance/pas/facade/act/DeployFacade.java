package com.msjf.finance.pas.facade.act;

import com.msjf.finance.pas.common.response.Response;
import com.msjf.finance.pas.facade.act.domain.ModelIdDomain;

public interface DeployFacade {
    Response deployFacade(ModelIdDomain modelIdDomain);
}
