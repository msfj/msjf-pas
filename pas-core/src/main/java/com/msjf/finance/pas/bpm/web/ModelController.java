package com.msjf.finance.pas.bpm.web;


import com.msjf.finance.pas.bpm.service.ModelService;
import com.msjf.finance.pas.common.response.Response;

import org.activiti.engine.repository.Model;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class ModelController {
    @Autowired
    ModelService modelService;

    @RequestMapping(value="/model/modelList",method= {RequestMethod.GET,RequestMethod.POST})
    @ResponseBody
    public Response getModel(HttpServletRequest request, HttpServletResponse response){
        Map<String, Object> mapParams = new HashMap<>();
        mapParams.put("pageSize",request.getParameter("pageSize"));
        mapParams.put("pageNumber",request.getParameter("pageNumber"));
        Response rs = new Response();
        List<Model> list = modelService.modelListPage(mapParams,rs);
            return rs;
    }
}
