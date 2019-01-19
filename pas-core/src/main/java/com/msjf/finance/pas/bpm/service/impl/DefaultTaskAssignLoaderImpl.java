package com.msjf.finance.pas.bpm.service.impl;

import com.msjf.finance.pas.bpm.service.TaskAssignLoader;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by Jsan on 2019/1/3.
 */
public class DefaultTaskAssignLoaderImpl implements TaskAssignLoader {


    @Override
    public List<String> taskAssignLoad(String taskDefinitionKey, String processDefinitionId, Map mapParam) throws RuntimeException {
        List assignersList = new ArrayList();
        assignersList.add("111|张三");
        assignersList.add("222|李四");
        return assignersList;
    }
}
