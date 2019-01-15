package com.msjf.finance.pas.bpm.facade;

import com.msjf.finance.pas.bpm.service.ModelService;
import com.msjf.finance.pas.common.response.Response;
import com.msjf.finance.pas.facade.act.SaveModelFacade;
import com.msjf.finance.pas.facade.act.domain.SaveModelDomain;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;


@Service("saveModelFacade")
public class SaveModelFacadeImpl  implements SaveModelFacade {

    @Resource
    ModelService modelService;

    @Override
    public Response save(SaveModelDomain saveModelDomain) {
        Map<String,Object> map =new HashMap<>();
        map.put("modelId",saveModelDomain.getModelId());
        map.put("name",saveModelDomain.getName());
        map.put("description",saveModelDomain.getDescription());
        map.put("json_xml",saveModelDomain.getJson_xml());
        map.put("svg_xml",saveModelDomain.getSvg_xml());
        return modelService.saveModel(map);
    }
}
