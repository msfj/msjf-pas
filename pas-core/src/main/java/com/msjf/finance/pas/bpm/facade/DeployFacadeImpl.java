package com.msjf.finance.pas.bpm.facade;

import com.msjf.finance.pas.bpm.service.ModelService;
import com.msjf.finance.pas.common.response.Response;
import com.msjf.finance.pas.facade.act.DeployFacade;
import com.msjf.finance.pas.facade.act.domain.ModelIdDomain;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

@Service("deployFacade")
public class DeployFacadeImpl  implements DeployFacade {

    @Resource
    ModelService modelService;

    @Override
    public Response deployFacade(ModelIdDomain modelIdDomain) {
        Map<String,Object> map =new HashMap<>();
        map.put("modelId",modelIdDomain.getModelId());
        return modelService.deploy(map);
    }
}
