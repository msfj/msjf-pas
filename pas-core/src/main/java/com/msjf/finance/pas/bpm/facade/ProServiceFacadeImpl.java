package com.msjf.finance.pas.bpm.facade;

import com.msjf.finance.msjf.core.response.Response;
import com.msjf.finance.pas.bpm.service.ProService;
import com.msjf.finance.pas.facade.act.ProServiceFacade;
import com.msjf.finance.pas.facade.act.domain.ProServiceDomain;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

@Service("proServiceFacade")
public class ProServiceFacadeImpl implements ProServiceFacade {

    @Resource
    ProService proService;

    @Override
    public Response queryProServiceList() throws RuntimeException {
        Map<String,Object> map =new HashMap<>();
        return proService.queryProServiceList(map);
    }

    @Override
    public Response updateProService(ProServiceDomain proServiceDomain) throws RuntimeException {
        Map<String,Object> map =new HashMap<>();
        map.put("proDefKey",proServiceDomain.getProDefKey());
        map.put("serviceFlag",proServiceDomain.getServiceFlag());
        return proService.updateProService(map);
    }
}
