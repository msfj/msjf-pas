package com.msjf.finance.pas.facade.act;

import com.msjf.finance.msjf.core.response.Response;
import com.msjf.finance.pas.facade.act.domain.ProServiceDomain;

public interface ProServiceFacade {

    Response queryProServiceList() throws RuntimeException;

    Response updateProService(ProServiceDomain proServiceDomain) throws  RuntimeException;
}
