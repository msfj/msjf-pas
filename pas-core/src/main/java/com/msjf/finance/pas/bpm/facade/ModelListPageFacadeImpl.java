package com.msjf.finance.pas.bpm.facade;


import com.msjf.finance.msjf.core.response.Response;
import com.msjf.finance.pas.bpm.service.ModelService;
import com.msjf.finance.pas.facade.act.ModelListPageFacade;
import com.msjf.finance.pas.facade.act.domain.PagDomain;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

@Service("modelListPageFacade")
public class ModelListPageFacadeImpl implements ModelListPageFacade {

    @Resource
    ModelService modelService;

    @Override
    public Response ModelListPage(PagDomain pagDomain) {
        Map<String,Object> map =new HashMap<>();
        map.put("pageSize",pagDomain.getPageSize());
        map.put("pageNumber",pagDomain.getPageNumber());
        return modelService.modelListPage(map);
    }
}
