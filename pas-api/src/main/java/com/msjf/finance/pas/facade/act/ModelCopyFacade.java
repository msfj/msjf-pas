package com.msjf.finance.pas.facade.act;

import com.msjf.finance.pas.common.response.Response;
import com.msjf.finance.pas.facade.act.domain.ModelCopyDomain;

public interface ModelCopyFacade {

    Response copy(ModelCopyDomain modelCopyDomain);
}
