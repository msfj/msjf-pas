package com.msjf.finance.pas.facade.act;

import com.msjf.finance.pas.common.response.Response;
import com.msjf.finance.pas.facade.act.domain.PagDomain;

public interface ModelListPageFacade {

    Response ModelListPage(PagDomain pagDomain);
}
