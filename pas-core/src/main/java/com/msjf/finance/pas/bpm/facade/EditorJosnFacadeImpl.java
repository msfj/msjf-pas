package com.msjf.finance.pas.bpm.facade;

import com.msjf.finance.msjf.core.response.Response;
import com.msjf.finance.pas.bpm.service.ModelService;
import com.msjf.finance.pas.facade.act.EditorJosnFacade;
import com.msjf.finance.pas.facade.act.domain.ModelIdDomain;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

@Service("editorJosnFacade")
public class EditorJosnFacadeImpl implements EditorJosnFacade {

    @Resource
    ModelService modelService;

    @Override
    public Response editorJosnFacade(ModelIdDomain modelIdDomain) {
        Map<String,Object> map =new HashMap<>();
        map.put("modelId",modelIdDomain.getModelId());
        return modelService.editorJson(map);
    }
}
