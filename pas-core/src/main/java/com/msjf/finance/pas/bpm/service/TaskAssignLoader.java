package com.msjf.finance.pas.bpm.service;

import java.util.List;
import java.util.Map;

/**
 * Created by Jsan on 2019/1/3.
 */
public interface TaskAssignLoader {

    /**
     *
     * @param taskDefinitionKey
     * @param processDefinitionId
     * @param mapParam
     * @return
     * @throws RuntimeException
     */
    List<String> taskAssignLoad(String taskDefinitionKey, String processDefinitionId
            , Map mapParam) throws RuntimeException;
}
