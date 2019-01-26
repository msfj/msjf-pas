package com.msjf.finance.pas.bpm.facade;

import com.msjf.finance.msjf.core.response.Response;
import com.msjf.finance.pas.bpm.service.PasHisService;
import com.msjf.finance.pas.facade.act.PasHisFacade;
import com.msjf.finance.pas.facade.act.domain.PasHisDomain;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

import static com.msjf.finance.pas.bpm.common.ParametersConstant.PAGE_NUMBER;
import static com.msjf.finance.pas.bpm.common.ParametersConstant.PAGE_SIZE;

@Service("pasHisFacade")
public class PasHisFacadeImpl implements PasHisFacade {

    @Resource
    PasHisService pasHisService;

    @Override
    public Response queryPasHisList(PasHisDomain pasHisDomain) {
        Map<String,Object> map =new HashMap<>();
        map.put("auditorId",pasHisDomain.getAuditorId());
        map.put("proDefName",pasHisDomain.getProDefName());
        int pageSize = pasHisDomain.getPageSize();
        int pageNumber = pasHisDomain.getPageNum();
        int firstResult = pageSize * (pageNumber - 1);
        map.put("pageSize",pageSize);
        map.put("pageNumber",pasHisDomain.getPageNum());
        map.put("currIndex",firstResult);
        return pasHisService.queryPasHisServiceList(map);
    }
}
