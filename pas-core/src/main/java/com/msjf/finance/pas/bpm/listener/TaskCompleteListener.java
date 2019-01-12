package com.msjf.finance.pas.bpm.listener;


import com.msjf.finance.pas.bpm.service.EventHandler;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.delegate.event.ActivitiEvent;
import org.activiti.engine.delegate.event.impl.ActivitiEntityWithVariablesEventImpl;
import org.activiti.engine.impl.persistence.entity.TaskEntity;
import org.activiti.engine.impl.persistence.entity.VariableInstance;

public class TaskCompleteListener implements EventHandler {

    @Override
    public void handle(ActivitiEvent event) {
        //保存每个节点的已完成次数

        if(event instanceof ActivitiEntityWithVariablesEventImpl){
            TaskEntity taskEntity = (TaskEntity) ((ActivitiEntityWithVariablesEventImpl) event).getEntity();
            RuntimeService runtimeService = event.getEngineServices().getRuntimeService();
            String taskCompleteKey = "_" + taskEntity.getTaskDefinitionKey() + "_completed";
            String executionId = event.getExecutionId();
            VariableInstance nrOfCompletedInstancesValue = runtimeService.getVariableInstance(executionId,"nrOfCompletedInstances");
            int nrOfCompletedInstances ;
            if(nrOfCompletedInstancesValue !=null){
                nrOfCompletedInstances = (int)nrOfCompletedInstancesValue.getValue() + 1;
            }else{
                nrOfCompletedInstances = 1;
            }
            runtimeService.setVariable(executionId,taskCompleteKey,nrOfCompletedInstances);

        }
    }
}