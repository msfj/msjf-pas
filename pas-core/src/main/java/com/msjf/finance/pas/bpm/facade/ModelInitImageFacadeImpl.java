package com.msjf.finance.pas.bpm.facade;

import com.msjf.finance.msjf.core.response.Response;
import com.msjf.finance.pas.bpm.service.ModelService;
import com.msjf.finance.pas.facade.act.ModelInitImageFacade;
import com.msjf.finance.pas.facade.act.domain.ModelIdDomain;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

@Service("modelInitImageFacade")
public class ModelInitImageFacadeImpl implements ModelInitImageFacade {

    @Resource
    ModelService modelService;

    @Override
    public Response initImage(ModelIdDomain modelIdDomain) {
        Map<String,Object> map =new HashMap<>();
        map.put("modelId",modelIdDomain.getModelId());
        return modelService.initImage(map);
    }
}
