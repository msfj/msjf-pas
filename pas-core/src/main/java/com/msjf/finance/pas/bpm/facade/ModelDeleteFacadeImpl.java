package com.msjf.finance.pas.bpm.facade;

import com.msjf.finance.msjf.core.response.Response;
import com.msjf.finance.pas.bpm.service.ModelService;
import com.msjf.finance.pas.facade.act.ModelDeleteFacade;
import com.msjf.finance.pas.facade.act.domain.ModelIdDomain;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

@Service("modelDeleteFacade")
public class ModelDeleteFacadeImpl implements ModelDeleteFacade {

    @Resource
    ModelService modelService;

    @Override
    public Response delete(ModelIdDomain domain) {
        Map<String,Object> map =new HashMap<>();
        map.put("modelId",domain.getModelId());
        return modelService.delete(map);
    }
}
