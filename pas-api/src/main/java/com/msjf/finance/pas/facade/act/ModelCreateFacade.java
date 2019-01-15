package com.msjf.finance.pas.facade.act;

import com.msjf.finance.pas.common.response.Response;
import com.msjf.finance.pas.facade.act.domain.ModelCreateDomain;

public interface ModelCreateFacade {
    
    Response create(ModelCreateDomain modelCreateDomain);
}
