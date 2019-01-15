package com.msjf.finance.pas.bpm.facade;

import com.msjf.finance.pas.bpm.service.ModelService;
import com.msjf.finance.pas.common.response.Response;
import com.msjf.finance.pas.facade.act.ModelCreateFacade;
import com.msjf.finance.pas.facade.act.domain.ModelCreateDomain;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

@Service("modelCreateFacade")
public class ModelCreateFacadeImpl implements ModelCreateFacade {

    @Resource
    ModelService modelService;

    @Override
    public Response create(ModelCreateDomain modelCreateDomain) {
        Map<String,Object> map =new HashMap<>();
        map.put("description",modelCreateDomain.getDescription());
        map.put("name",modelCreateDomain.getName());
        return modelService.create(map);
    }
}
