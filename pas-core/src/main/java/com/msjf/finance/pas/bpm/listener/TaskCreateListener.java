package com.msjf.finance.pas.bpm.listener;


import com.msjf.finance.pas.bpm.service.EventHandler;
import com.msjf.finance.pas.bpm.service.TaskAssignLoader;
import com.msjf.finance.pas.common.WorkflowUtils;
import org.activiti.bpmn.model.Activity;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.engine.delegate.event.ActivitiEntityEvent;
import org.activiti.engine.delegate.event.ActivitiEvent;
import org.activiti.engine.delegate.event.impl.ActivitiEntityEventImpl;
import org.activiti.engine.impl.persistence.entity.TaskEntity;

//单实例审核人设置逻辑
//如果判断当前节点非多实例，则设置审核人
public class TaskCreateListener implements EventHandler {

    private TaskAssignLoader taskAssignLoader;

    public TaskCreateListener() {
    }

    public TaskCreateListener(TaskAssignLoader taskAssignLoader) {
        this.taskAssignLoader = taskAssignLoader;
    }

    @Override
    public void handle(ActivitiEvent event) {

        if(event instanceof ActivitiEntityEvent){
            TaskEntity taskEntity = (TaskEntity) ((ActivitiEntityEventImpl) event).getEntity();
            BpmnModel bpmnModel = event.getEngineServices().getRepositoryService().getBpmnModel(taskEntity.getProcessDefinitionId());
            Activity activity = (Activity) bpmnModel.getFlowElement(taskEntity.getTaskDefinitionKey());
            //非多实例节点
            if(!WorkflowUtils.isActivityMultiInstance(activity)){
                taskEntity.addCandidateUsers(taskAssignLoader.taskAssignLoad(taskEntity.getTaskDefinitionKey(),
                        taskEntity.getProcessDefinitionId(),
                        WorkflowUtils.getVariablesFromProcessInstance(event.getEngineServices(),event)));
            }
        }
    }

    public TaskAssignLoader getTaskAssignLoader() {
        return taskAssignLoader;
    }

    public void setTaskAssignLoader(TaskAssignLoader taskAssignLoader) {
        this.taskAssignLoader = taskAssignLoader;
    }
}