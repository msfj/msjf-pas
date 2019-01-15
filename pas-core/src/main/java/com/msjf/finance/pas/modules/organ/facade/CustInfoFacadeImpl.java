package com.msjf.finance.pas.modules.organ.facade;




import com.msjf.finance.msjf.core.response.Response;
import com.msjf.finance.pas.facade.organ.CustInfoFacade;
import com.msjf.finance.pas.facade.organ.domain.CustInfoDomain;
import com.msjf.finance.pas.modules.organ.service.CustInfoService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service("custInfoFacade")
public class CustInfoFacadeImpl implements CustInfoFacade {

    @Resource
    CustInfoService custInfoService ;



    @Override
    public Response<List<CustInfoDomain>> queryCustInfoList() {
        try {
            List<CustInfoDomain> organInfoDomainList = custInfoService.queryCustInfoList();
            return new Response<>().success(organInfoDomainList);
        } catch (Exception e) {
            e.printStackTrace();
            return new Response<>().fail();
        }
    }
}
