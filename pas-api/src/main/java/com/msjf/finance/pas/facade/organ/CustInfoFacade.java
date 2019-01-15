package com.msjf.finance.pas.facade.organ;

import com.msjf.finance.msjf.core.response.Response;
import com.msjf.finance.pas.facade.organ.domain.CustInfoDomain;

import java.util.List;

public interface CustInfoFacade {
    public Response<List<CustInfoDomain>> queryCustInfoList();
}
