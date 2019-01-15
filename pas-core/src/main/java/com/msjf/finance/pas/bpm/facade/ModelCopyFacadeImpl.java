package com.msjf.finance.pas.bpm.facade;

import com.msjf.finance.msjf.core.response.Response;
import com.msjf.finance.pas.bpm.service.ModelService;
import com.msjf.finance.pas.facade.act.ModelCopyFacade;
import com.msjf.finance.pas.facade.act.domain.ModelCopyDomain;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

@Service("modelCopyFacade")
public class ModelCopyFacadeImpl implements ModelCopyFacade {

    @Resource
    ModelService modelService;

    @Override
    public Response copy(ModelCopyDomain modelCopyDomain) {
        Map<String,Object> map =new HashMap<>();
        map.put("description",modelCopyDomain.getDescription());
        map.put("name",modelCopyDomain.getName());
        map.put("oldModelId",modelCopyDomain.getOldModelId());
        return modelService.copy(map);
    }
}
