package com.msjf.finance.pas.facade.act;


import com.msjf.finance.msjf.core.response.Response;
import com.msjf.finance.pas.facade.act.domain.ModelCopyDomain;

public interface ModelCopyFacade {

    Response copy(ModelCopyDomain modelCopyDomain);
}
