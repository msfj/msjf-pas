package com.msjf.finance.pas.bpm.listener;


import com.msjf.finance.pas.bpm.common.ParametersConstant;
import com.msjf.finance.pas.bpm.service.EventHandler;
import com.msjf.finance.pas.bpm.service.TaskAssignLoader;
import com.msjf.finance.pas.common.VerificationUtil;
import com.msjf.finance.pas.common.WorkflowUtils;
import org.activiti.engine.EngineServices;
import org.activiti.engine.delegate.event.ActivitiActivityEvent;
import org.activiti.engine.delegate.event.ActivitiEvent;
import org.activiti.engine.delegate.event.impl.ActivitiActivityEventImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 多实例审核人设置逻辑
 */
public class ActivityStartedListener implements EventHandler {

    private static Logger logger = LoggerFactory.getLogger(ActivityStartedListener.class);
    //此变量为子流程绑定变量，为加载部门bex，需前端配合使用(这里只是指定部门，事实上可以用于其他业务场景，
    // 比如多实例子公司、多实例科室等)
    private static final String DEPARTMENT_BEX_KEY = "departmentBex";
    private static final String USER_TASK_TYPE_KEY = "_process_usertasktype";

    public ActivityStartedListener(){}

    private TaskAssignLoader taskAssignLoader;

    public TaskAssignLoader getTaskAssignLoader() {
        return taskAssignLoader;
    }

    public void setTaskAssignLoader(TaskAssignLoader taskAssignLoader) {
        this.taskAssignLoader = taskAssignLoader;
    }

    @Override
    public void handle(ActivitiEvent event) {
        String processDefinitionId = event.getProcessDefinitionId();
        EngineServices engineServices = event.getEngineServices();

        if(event instanceof ActivitiActivityEventImpl){
            ActivitiActivityEvent activitiActivityEvent = (ActivitiActivityEvent)event;
            String activitiId = activitiActivityEvent.getActivityId();

            //设置UserTaskType
            if("userTask".equals(activitiActivityEvent.getActivityType())
                   || "subProcess".equals(activitiActivityEvent.getActivityType())){
                setUserTaskType(activitiId,event,engineServices);
            }

            //usertask类型、多实例流程才在这里设置
            if ("userTask".equals(activitiActivityEvent.getActivityType())
                    && activitiActivityEvent.getBehaviorClass().contains("MultiInstanceBehavior")){

                //关联获选人，这里最新触发,从变量表里获取bex
                logger.info("trigger ActivityStartedListener ,start get AssigneeList collection,processDefinitionId = {}"
                                + ",taskDefinitionKey = {}",processDefinitionId, activitiId);

                String assigneeListCollectionName = activitiId + ParametersConstant.ASSIGNEE_LIST_SUFFIX;
                Object assigneeList= engineServices.getRuntimeService().getVariable(event.getExecutionId(), assigneeListCollectionName);
                if(!WorkflowUtils.isDefaultAssigneeList((List) assigneeList)){
                    List<String> newAssigneeList =  taskAssignLoader.taskAssignLoad(activitiId,processDefinitionId,
                            WorkflowUtils.getVariablesFromProcessInstance(engineServices,activitiActivityEvent));
                    engineServices.getRuntimeService().setVariable(event.getExecutionId(),assigneeListCollectionName,newAssigneeList);
                    logger.info("trigger ActivityStartedListener ,processDefinitionId = {} " +
                                                    ", get AssigneeList {} SuccessFully!",processDefinitionId,assigneeListCollectionName);
                }else {
                    logger.info("trigger ActivityStartedListener,processDefinitionId = {}"  +
                                                         ",AssigneeList {} is Already set, ignore event!",processDefinitionId,assigneeListCollectionName);
                }
                //subProcess类型，多实例流程才在这里设置
            }else if ("subProcess".equals(activitiActivityEvent.getActivityType())
                    && activitiActivityEvent.getBehaviorClass().contains("MultiInstanceBehavior")){

                //关联分支机构，这里最新触发,从变量表里获取bex
                logger.info("trigger ActivityStartedListener ,start get DepartmentList collection,processDefinitionId = {}"
                        + ",taskDefinitionKey = {}",processDefinitionId, activitiId);

                String departmentListCollectionName = activitiId + ParametersConstant.DEPARTMENT_LIST_SUFFIX;
                Object departmentList = engineServices.getRuntimeService().getVariable(event.getExecutionId(), departmentListCollectionName);
                if(!WorkflowUtils.isDefaultDepartmentList((List) departmentList)){
                    /*Response rs = new Response();
                    doBexGetSubProcessDepartmentList(engineServices,activitiActivityEvent,
                            WorkflowUtils.getVariablesFromProcessInstance(engineServices,activitiActivityEvent),rs);
                    engineServices.getRuntimeService().setVariable(event.getExecutionId(),departmentListCollectionName,getDepartmentListFromResult(rs.getResult()));*/
                    logger.info("trigger ActivityStartedListener ,processDefinitionId = {} " +
                            ", get DepartmentList {} successfully!",processDefinitionId,departmentListCollectionName);
                    // String[] assigneeArray = new String[]{"1802271007286860010000000000_20160517104217496801","1802271007286860010000000000_20160514164030391809"};
                    // engineServices.getRuntimeService().setVariable(event.getExecutionId(),assigneeListCollectionName, Arrays.asList(assigneeArray));
                }else {
                    logger.info("trigger ActivityStartedListener,processDefinitionId = {}"  +
                            ",DepartmentList {} is already set, ignore event!",processDefinitionId,departmentListCollectionName);
                }
            }
        }
    }

    private void setUserTaskType(String activitiId, ActivitiEvent event, EngineServices engineServices){
        String userTaskTypeName = activitiId + USER_TASK_TYPE_KEY;
        String userTaskType = WorkflowUtils.getFormPropertyFromBpmnModel(activitiId,event.getProcessDefinitionId(),
                userTaskTypeName,engineServices.getRepositoryService());
        engineServices.getRuntimeService().setVariable(event.getExecutionId(),userTaskTypeName,userTaskType);
    }

 /*   public void doBexGetSubProcessDepartmentList(EngineServices engineServices, ActivitiActivityEvent activitiActivityEvent, Map mapParam, Response rs){

        String departmentBexName = getDepartmentBexName(engineServices,activitiActivityEvent);
        BexCommandExecutor commandExecutor = (BexCommandExecutor) WsContext.getBean("bexCommandExecutor");
        if(VerificationUtil.isEmpty(departmentBexName)){
            throw new WsRollbackRuntimeException("执行多实例子流程时,获取分支机构bex不能为空!");
        }
        mapParam.put("props_targetid", departmentBexName);
        commandExecutor.execute(mapParam,rs);
        if (!rs.isSuccessful()) {
            throw new WsRollbackRuntimeException("execute get departmentBex failed :" + rs);
        }
    }*/

    private String getDepartmentBexName(EngineServices engineServices, ActivitiActivityEvent activitiActivityEvent){
        return WorkflowUtils.getFormPropertyFromBpmnModel(activitiActivityEvent.getActivityId(),activitiActivityEvent.getProcessDefinitionId(),
                DEPARTMENT_BEX_KEY,engineServices.getRepositoryService());
    }

    private List<String> getDepartmentListFromResult(Object result){
        if(result instanceof List){
            List resultList = (List)result;
            if(VerificationUtil.isEmpty(resultList)){
                throw new RuntimeException("bex获取部门信息不能为空!");
            }
            return (List<String>)resultList.stream().map(e ->
                    e instanceof Map?((Map)e).get("departmentId"):e.toString()
            ).distinct().collect(Collectors.toList());
        }
        throw new RuntimeException("bex获取部门结果集类型有误!");
    }
}