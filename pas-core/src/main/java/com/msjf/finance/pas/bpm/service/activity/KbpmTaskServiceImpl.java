package com.msjf.finance.pas.bpm.service.activity;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.alibaba.fastjson.serializer.SimplePropertyPreFilter;
import com.msjf.finance.pas.bpm.common.ParametersConstant;
import com.msjf.finance.pas.bpm.service.KbpmTaskService;
import com.msjf.finance.pas.common.StringUtil;
import com.msjf.finance.pas.common.VerificationUtil;
import com.msjf.finance.pas.common.WorkflowUtils;
import com.msjf.finance.pas.common.response.Response;
import org.activiti.bpmn.model.*;
import org.activiti.engine.*;
import org.activiti.engine.delegate.Expression;
import org.activiti.engine.form.FormProperty;
import org.activiti.engine.form.TaskFormData;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.history.HistoricTaskInstanceQuery;
import org.activiti.engine.history.HistoricVariableInstance;
import org.activiti.engine.impl.RepositoryServiceImpl;
import org.activiti.engine.impl.bpmn.behavior.ParallelMultiInstanceBehavior;
import org.activiti.engine.impl.bpmn.behavior.SequentialMultiInstanceBehavior;
import org.activiti.engine.impl.bpmn.behavior.UserTaskActivityBehavior;
import org.activiti.engine.impl.form.TaskFormDataImpl;
import org.activiti.engine.impl.persistence.entity.*;
import org.activiti.engine.impl.pvm.PvmActivity;
import org.activiti.engine.impl.pvm.PvmTransition;
import org.activiti.engine.impl.pvm.delegate.ActivityBehavior;
import org.activiti.engine.impl.pvm.process.ActivityImpl;
import org.activiti.engine.impl.task.TaskDefinition;
import org.activiti.engine.runtime.Execution;
import org.activiti.engine.task.Task;
import org.activiti.engine.task.TaskQuery;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionStatus;

import java.util.*;

import static com.msjf.finance.pas.bpm.common.ParametersConstant.*;

/**
 * Created by chengjunping on 2018/12/27.
 */
@Service("kbpmTaskServiceImpl")
public class KbpmTaskServiceImpl implements KbpmTaskService{

    protected Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private TaskService taskService;

    @Autowired
    private FormService formService;

    @Autowired
    private HistoryService historyService;

    @Autowired
    private RuntimeService runtimeService;

    @Autowired
    private IdentityService identityService;

    @Autowired
    private RepositoryService repositoryService;

    /*@Autowired
    private BexCommandExecutor commandExecutor;*/

    @Override
    public void claim(Map<String, Object> mapParams, Response rs) {
        String userId = (String) mapParams.get(USER_ID);
        String taskId = (String) mapParams.get(TASK_ID);
        if (logger.isDebugEnabled()) {
            logger.debug("Claim : userId->{}, taskId->{}", userId, taskId);
        }
        try {
            taskService.claim(taskId, userId);
            rs.success("签收成功");
        } catch (ActivitiObjectNotFoundException e) {
            if (logger.isWarnEnabled()) {
                logger.warn("Activiti object not found exception : taskId = > {}, userId = > {}, by : {}", taskId,
                        userId, e.getMessage());
            }
            rs.fail("0","没有找到待办任务");
            throw new RuntimeException(e);
        } catch (ActivitiTaskAlreadyClaimedException e) {
            if (logger.isWarnEnabled()) {
                logger.warn("Activiti task already claimed exception : taskId = > {}, userId = > {}, by : {}", taskId,
                        userId, e.getMessage());
            }
            rs.fail("0","待办任务已被签收");
            throw new RuntimeException(e);
        }
    }

    @Override
    public void unClaim(Map<String, Object> mapParams, Response rs) {
        String userId = (String) mapParams.get(USER_ID);
        String taskId = (String) mapParams.get(TASK_ID);
        if (logger.isDebugEnabled()) {
            logger.debug("UnClaim : userId->{}, taskId->{}", userId, taskId);
        }
        try {
            taskService.unclaim(taskId);
            rs.success("反签收成功");
        } catch (ActivitiObjectNotFoundException e) {
            if (logger.isWarnEnabled()) {
                logger.warn("Activiti object not found exception : taskId = > {}, userId = > {}, by : {}", taskId,
                        userId, e.getMessage());
            }
            rs.fail("0","没有找到待办任务");
            throw new RuntimeException(e);
        }
    }

    @Override
    public void userTaskList(Map<String, Object> mapParams, Response rs) {
        String userId = (String) mapParams.get(USER_ID);
        int pageSize = (Integer) mapParams.get(PAGE_SIZE);
        int pageNumber = (Integer) mapParams.get(PAGE_NUMBER);
        int firstResult = pageSize * (pageNumber - 1);

        if (logger.isDebugEnabled()) {
            logger.debug("taskList : userId->{}, pageSize->{}, pageNumber->{}", userId, pageSize, pageNumber);
        }
        TaskQuery taskQuery = taskService.createTaskQuery().taskCandidateOrAssigned(userId).includeProcessVariables()
                .active();
        List<Task> tasks = taskQuery.orderByTaskCreateTime().desc().listPage(firstResult, pageSize);
       /* rs.setLengths((int) taskQuery.count());*/

        SimplePropertyPreFilter filter = new SimplePropertyPreFilter(Task.class,
                new String[] { "createTime", "deleted", "suspended", "id", "processInstanceId", "priority",
                        "taskDefinitionKey", "name", "processDefinitionId" });

        JSONArray array = new JSONArray();
        if (tasks != null) {
            for (Task task : tasks) {
                JSONObject object = JSON
                        .parseObject(JSON.toJSONString(task, filter, SerializerFeature.WriteMapNullValue));
                TaskEntity taskEntity = (TaskEntity) task;
                List<VariableInstanceEntity> variables = taskEntity.getQueryVariables();
                for (VariableInstanceEntity variable : variables) {
                    if ("title".equals(variable.getName())) {
                        object.put("title", variable.getCachedValue());
                    }
                }

                array.add(object);
            }
        }

        /*rs.setResult(array);
        rs.setResType(Constant.WS_TYPE_FASTJSONARRAY);*/
        rs.success("1","查询成功",array);
    }

    @Override
    public void getTaskForm(Map<String, Object> mapParams, Response rs) {
        String taskId = (String) mapParams.get(TASK_ID);
        if (logger.isDebugEnabled()) {
            logger.debug("taskId : {} getTaskForm", taskId);
        }
        TaskFormData taskFormData = null;
        try {
            // Check if task requires a form
            taskFormData = formService.getTaskFormData(taskId);
        } catch (ActivitiObjectNotFoundException e) {
            if (logger.isWarnEnabled()) {
                logger.warn("Activiti object not found exception : taskId = > {}, by : {}", taskId, e.getMessage());
            }
            rs.fail("0","没有找到待办任务");
            return;
        } catch (ActivitiException e) {
            logger.error("Activiti exception : ", e);
            rs.fail("0","没有表单数据");
            return;
        }
        if (taskFormData != null && taskFormData.getFormProperties() != null) {
            JSONArray array = new JSONArray();
            List<FormProperty> formProperties = taskFormData.getFormProperties();
            for (FormProperty formProperty : formProperties) {
                JSONObject object = (JSONObject) JSON.toJSON(formProperty);
                if ("enum".equals(formProperty.getType().getName())) {
                    @SuppressWarnings("unchecked")
                    Map<String, String> values = (Map<String, String>) formProperty.getType().getInformation("values");
                    if (values != null) {
                        object.put("values", values);

                    }
                } else if ("date".equals(formProperty.getType().getName())) {
                    String datePattern = (String) formProperty.getType().getInformation("datePattern");
                    if (datePattern != null) {
                        object.put("datePattern", datePattern);
                    }
                }
                array.add(object);
                if (logger.isDebugEnabled()) {
                    logger.debug("Form property : {}", object.toJSONString());
                }
            }

            /*rs.setResult(array);
            rs.setResType(Constant.WS_TYPE_FASTJSONARRAY);*/
            rs.success("1","获取表单成功",array);
        } else {
            rs.success("无表单数据");
        }
    }


    /**
     * 多实例执行逻辑
     * @param mapParams
     * @param rs
     * @throws RuntimeException
     */
    public void submitTaskFormDataForMultiInstance(Activity activity,Task task,Map<String, Object> mapParams, Response rs) {

        Object userIds = mapParams.get(USER_IDS);
        String taskId = (String) mapParams.get(TASK_ID);
        Map<String, Object> variables = new HashMap<String, Object>();
        String currentTaskDefinitionKey = task.getTaskDefinitionKey(); //保存当前任务对于的节点id
        String comment = (String) mapParams.get("comment");

        // 所有以fp_开始的参数都复制到formPraperties中，用于保存到用户任务中
        mapParams.forEach((key,value) -> {
            if(key.startsWith(ParametersConstant.FORM_PRAPERTIES_SUFFIX)){
                variables.put(key.replaceFirst(ParametersConstant.FORM_PRAPERTIES_SUFFIX, ""),
                        value != null ? value.toString() : null);
            }
        });

        if (logger.isDebugEnabled()) {
            logger.debug("taskId : {}, submitTaskFormData : {}", taskId, variables);
        }

        WorkflowUtils.setAndAddTaskFormData(formService.getTaskFormData(taskId),variables);

        // 保存审核意见到批注中
        if (comment != null) {
            taskService.addComment(taskId, task.getProcessInstanceId(), comment);
        }

        try {
            //设置可能的多实例审核人@see ActivityStartedListener
            //setMultiInstanceAssignees(task,userTasks,mapParams,variables);
            //统计节点用户同意计数
            setAndAddUserTaskApproveCount(task,mapParams);

            //WorkflowUtils.setMultiInstanceCommonVariable(activity,variables);
            // 提交审核
            taskService.complete(taskId,variables);

            if(isProcessInstanceFinish(task.getProcessInstanceId())){
                processInstanceFinishExecEndBex(true,task.getProcessInstanceId(),task,rs);
                /*SetProcessInstStepServices setProcessInstStepServices = (SetProcessInstStepServices) SpringContextHelper.getBean("setProcessInstStepServices");
                HashMap<String,Object> taskMap = new HashMap<String,Object>();
                taskMap.put("processUUID",task.getProcessDefinitionId());
                taskMap.put("processINST",task.getProcessInstanceId());
                taskMap.put("stepId",task.getTaskDefinitionKey());
                taskMap.put("stepName",task.getName());
                taskMap.put("procInstStatus","1");
                taskMap.put("auditorsTodo",task.getAssignee());
                setProcessInstStepServices.doBusiness(taskMap,rs);
                if(!rs.isSuccessful()){
                    throw new WsRollbackRuntimeException("业务流程实例表更新数据失败!");
                }*/
                setProcessInstanceStatusValue(true,
                        true,"审核成功，流程结束!",rs);
            }else {
                //没有审核完，这里进行可能的单节点任务审核人赋值
                // 因为当前是单实例节点，故一定会走到下一步，这里无需判断即可进行获取下一批用户任务
                List<Task> nextTasks = taskService.createTaskQuery().processInstanceId(task.getProcessInstanceId()).list();
                if(!VerificationUtil.isEmpty(nextTasks)){
                    //如果发生节点切换，这里将可能进行对新单节点设置审核人
                    if(UserTaskIsSwitched(currentTaskDefinitionKey,nextTasks)){
                        //  @see TaskAssignedListener
                        //	setSingleInstanceAssignees(userTasks,nextTasks,userIds);
                       /* Map delMap = new HashMap(2);
                        delMap.put("procinstId",task.getProcessInstanceId());
                        DaoUtil.commonPersistance.delete("act_ru_VARIABLE_delByProcinstId",delMap);*/
                        setProcessInstanceStatusValue(true,
                                false,"跳转下一步成功!",rs);
                    }else {
                        setProcessInstanceStatusValue(false,
                                false,"审核成功!",rs);
                    }
                }
            }
        } catch (ActivitiException e) {
            logger.error("Activiti Core exception :", e);
            rs.fail("0","任务状态异常或者参数错误");
            /*rs.setDetailMessage(e.getMessage());*/
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Activiti Exec exception :", e);
            rs.fail("0","流程审核异常");
            /*rs.setDetailMessage(e.getMessage());*/
        }
    }

    /**
     *
     * @param subTaskComplete 当前节点是否完成
     * @param processInstanceComplete 整个流程是否完成
     * @param errorMessage
     * @param rs
     */
    private void setProcessInstanceStatusValue(boolean subTaskComplete
            ,boolean processInstanceComplete,String errorMessage,Response rs){

        //失败则返回
        if(!rs.getFlag().equals("1")){
            return;
        }

        Map retMap = new HashMap(2);
        if(subTaskComplete){
            retMap.put("subComplete","1");
        }else {
            retMap.put("subComplete","0");
        }

        if(processInstanceComplete){
            retMap.put("complete","1");
        }else {
            retMap.put("complete","0");
        }
        rs.success("1",errorMessage,retMap);
        /*ResultUtil.makerSuccessResult(rs,errorMessage,retMap);*/
    }

    /**
     * 这里判断多实例是否发生任务节点切换，如果发生切换
     * 新的任务对应的TaskDefinitionKey必然与之前节点的
     * 不一样
     * @param currentTaskDefinitionKey
     * @param nextTasks
     * @return
     */
    public boolean UserTaskIsSwitched(String currentTaskDefinitionKey,List<Task> nextTasks){

        if(!VerificationUtil.isEmpty(nextTasks)){
            Task task = nextTasks.get(0);
            if(task.getTaskDefinitionKey().equals(currentTaskDefinitionKey)){
                return false;
            }else {
                return true;
            }
        }
        return false;
    }


    private boolean isProcessInstanceFinish(String processInstanceId){
        Execution execution = runtimeService.createProcessInstanceQuery().processInstanceId(processInstanceId)
                .singleResult();
        // 如果execution为空，则表示流程结束
        if (execution == null) {
            return true;
        }
        return false;
    }

    private void processInstanceFinishExecEndBex(boolean isMultiInstanceUserTask ,String processInstanceId,Task task, Response rs){

        if (logger.isDebugEnabled()) {
            logger.debug("Execution is null, process instance will goto end bex");
        }
        List<HistoricVariableInstance> variables = historyService.createHistoricVariableInstanceQuery()
                .processInstanceId(processInstanceId).list();
        String endBex = null;
        Map<String, Object> requestMap = null;
        for (HistoricVariableInstance variable : variables) {
            // 获取配置变量中是否有endBex
            if ("process_eventend".equals(variable.getVariableName())) {
                endBex = variable.getValue() == null ? null : variable.getValue().toString();
            }
            // 其它变量添加到requestMap，用于bex调用
            else {
                if (requestMap == null) {
                    requestMap = new HashMap<String, Object>();
                }
                if( variable.getVariableTypeName() == null
                        ||("string").equals(variable.getVariableTypeName())
                        || ("null").equals(variable.getVariableTypeName())
                        || ("double").equals(variable.getVariableTypeName())
                        || ("long").equals(variable.getVariableTypeName())
                        || ("integer").equals(variable.getVariableTypeName())){
                    if(!variable.getVariableName().contains("_assigneeList")){
                        requestMap.put(variable.getVariableName(), variable.getValue());
                    }
                }
            }
        }
        if (logger.isDebugEnabled()) {
            logger.debug("Historic variable for [process_eventend] : {}", endBex);
        }
        if (StringUtils.isNotEmpty(endBex)) {
            /*Response rrs = new Result();
            rrs.failed("调用失败");
            Map<String, Object> mapParam = new HashMap<String, Object>();
            mapParam.put("props_targetid", endBex);
            Transaction transaction = WsContext.geCurrentTransaction();
            if (transaction != null) {
                mapParam.put("props_current", transaction.getCurrent());
            }

            if (requestMap != null) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Historic variables : {}", requestMap.toString());
                }
                // 支持表单参数中指定bex类型，http/local
                String cmdType = (String) requestMap.get("process_eventend_cmdType");
                if (StringUtils.isNotEmpty(cmdType)) {
                    mapParam.put("props_cmd_cmdType", cmdType);
                }
                // 如果是跨系统调用的bex，支持表单参数中指定策略bean
                String strategy = (String) requestMap.get("process_eventend_strategy");
                if (StringUtils.isNotEmpty(strategy)) {
                    mapParam.put("props_strategy", strategy);
                }
                mapParam.put("processInstanceId",processInstanceId);
                mapParam.putAll(requestMap);
                // 标识审核结果变量，特殊处理
                if (mapParam.containsKey("audit_result")) {
                    if(!isMultiInstanceUserTask) {        //如果最后一节点是非多实例节点，覆盖之
                        mapParam.put("audit_result", mapParam.get(task.getTaskDefinitionKey() + "_approve"));
                    }
                }else {//如果流程实例里没有审核结果值，则赋最后一个审核结果值
                    mapParam.put("audit_result", mapParam.get(task.getTaskDefinitionKey() + "_approve"));
                }
            }
            // 执行bex
            if (logger.isDebugEnabled()) {
                logger.debug("Bex commandExecutor variables : {}", mapParam.toString());
            }
            commandExecutor.execute(mapParam, rrs);
            if (!rrs.isSuccessful()) {
                rs.failed(rrs.getErrorMessage());
                rs.setDetailMessage(rrs.getDetailMessage());
                rs.setReturnCode(rrs.getReturnCode());
                logger.error("execute end bex failed ,mapParam:" + mapParam);
                throw new WsRuntimeException("回调BEX失败"+ rrs.getErrorMessage());
            }*/
        }
    }

    @Override
    public void submitTaskFormData(Map<String, Object> mapParams, Response rs) {
        String taskId = (String) mapParams.get(TASK_ID);

        Task task = taskService.createTaskQuery().includeProcessVariables().taskId(taskId).singleResult();
        if (task == null) {
            rs.fail("0","没有找到对应的任务信息!");
            return;
        }

        BpmnModel bpmnModel = repositoryService.getBpmnModel(task.getProcessDefinitionId());
        Activity activity = (Activity) bpmnModel.getFlowElement(task.getTaskDefinitionKey());

        if(WorkflowUtils.isActivityMultiInstance(activity)){
            submitTaskFormDataForMultiInstance(activity,task,mapParams,rs);
        }else {
            submitTaskFormDataForSingleInstance(task,mapParams,rs);
        }
    }

    public void submitTaskFormDataForSingleInstance(Task task,Map<String, Object> mapParams, Response rs) {
        String userId = (String) mapParams.get(USER_ID);
        Object userIds = mapParams.get(USER_IDS);
        String taskId = (String) mapParams.get(TASK_ID);
        Map<String, Object> variables = new HashMap<String, Object>();
        String comment = (String) mapParams.get("comment");

        // 所有以fp_开始的参数都复制到formPraperties中，用于保存到用户任务中
        mapParams.forEach((key,value) -> {
            if(key.startsWith(ParametersConstant.FORM_PRAPERTIES_SUFFIX)){
                variables.put(key.replaceFirst(ParametersConstant.FORM_PRAPERTIES_SUFFIX, ""),
                        value != null ? value.toString() : null);
            }
        });

        if (logger.isDebugEnabled()) {
            logger.debug("taskId : {}, submitTaskFormData : {}", taskId, variables);
        }
        try {

            WorkflowUtils.setAndAddTaskFormData(formService.getTaskFormData(taskId),variables);

            String processInstanceId = task.getProcessInstanceId();
            mapParams.put("processInstanceId",processInstanceId);
            identityService.setAuthenticatedUserId(userId);
            // 保存审核意见到批注中
            if (comment != null) {
                taskService.addComment(taskId, processInstanceId, comment);
            }
            // 把审核人添加到任务变量里面
            variables.put("_$_" + task.getTaskDefinitionKey() + "_assignment", userId);
            // 设置可能的多实例审核人
            // 设置可能的多实例审核人@see ActivityStartedListener
            // setMultiInstanceAssignees(task,userTasks,mapParams,variables);
            // 统计节点用户同意计数
            setAndAddUserTaskApproveCount(task,mapParams);
            // 提交审核
            taskService.complete(taskId, variables);

            // 如果execution为空，则表示流程结束
            if (isProcessInstanceFinish(processInstanceId)) {
                processInstanceFinishExecEndBex(false,processInstanceId,task,rs);
               /* SetProcessInstStepServices setProcessInstStepServices = (SetProcessInstStepServices) SpringContextHelper.getBean("setProcessInstStepServices");
                HashMap<String,Object> taskMap = new HashMap<String,Object>();
                taskMap.put("processUUID",task.getProcessDefinitionId());
                taskMap.put("processINST",processInstanceId);
                taskMap.put("stepId",task.getTaskDefinitionKey());
                taskMap.put("stepName",task.getName());
                taskMap.put("procInstStatus","1");
                taskMap.put("stepType","finish");
                taskMap.put("customParam",mapParams.get("fp_customParam"));
                setProcessInstStepServices.doBusiness(taskMap,rs);
                if(!rs.isSuccessful()){
                    throw new WsRollbackRuntimeException("业务流程实例表更新数据失败!");
                }*/
                setProcessInstanceStatusValue(true,
                        true,"审核成功，流程结束!",rs);

            } else {
				/* 因为当前是单实例节点，故一定会走到下一步，这里无需判断即可进行获取下一批用户任务
				@see TaskAssignedListener
				List<Task> nextTasks = taskService.createTaskQuery().processInstanceId(processInstanceId).list();
				if(!VerificationUtil.isEmpty(nextTasks)){
				 	setSingleInstanceAssignees(userTasks,nextTasks,userIds);
				}else {
					//这里不可能，除非引擎有BUG
					throw new WsRuntimeException("Unkown Error:ProcessInstanceId = " + processInstanceId);
				}*/
                /*Map delMap = new HashMap(2);
                delMap.put("procinstId",processInstanceId);
                DaoUtil.commonPersistance.delete("act_ru_VARIABLE_delByProcinstId",delMap);*/
                setProcessInstanceStatusValue(true,
                        false,"跳转下一步成功!",rs);
            }
        }catch (ActivitiException e) {
            logger.error("Activiti exception : ", e);
            rs.fail("0","任务状态异常或者参数错误：" + e.getMessage());
            /*rs.setDetailMessage(e.getMessage());*/
        } catch (Exception e) {
            logger.error("流程执行异常!", e);
            rs.fail("0","流程审核错误:" + e.getMessage());
        } finally {
            identityService.setAuthenticatedUserId(null);
        }
    }


    /**
     * 设置和增加当前审核节点同意计数
     * @param task
     * @param mapParam
     */
    public void setAndAddUserTaskApproveCount(Task task,Map mapParam){
        String approveStr = StringUtil.valueOf(mapParam.get("approve"));
        if (StringUtil.isNull(approveStr)) {
            return;
        }
        int approve = Integer.parseInt(approveStr);

        String taskApproveKey = "_" + task.getTaskDefinitionKey() + "_agreeOfcnt";
        VariableInstance taskApproveValue = runtimeService.getVariableInstance(task.getExecutionId(),taskApproveKey);

        //只有审核同意才变更当前节点审核同意计数
        if(approve == 0){
            //如果没有初始值，则设置为0
            if(taskApproveValue == null){
                runtimeService.setVariable(task.getExecutionId(),taskApproveKey,0);
            }
            return;
        }

        //每当节点初始化时，会设置值
        if(taskApproveValue == null || (int)taskApproveValue.getValue() == 0){
            runtimeService.setVariable(task.getExecutionId(),taskApproveKey,1);
        }else {
            int taskApproveCount = (int)taskApproveValue.getValue();
            runtimeService.setVariable(task.getExecutionId(),taskApproveKey,taskApproveCount + 1);
        }
    }

    @Override
    public void saveFormData(Map<String, Object> mapParams, Response rs) {
        String taskId = (String) mapParams.get(TASK_ID);
        Map<String, String> properties = new HashMap<String, String>();
        for (Iterator<Map.Entry<String, Object>> it = mapParams.entrySet().iterator(); it.hasNext();) {
            Map.Entry<String, Object> entry = it.next();
            String key = entry.getKey();
            if (key.startsWith(ParametersConstant.FORM_PRAPERTIES_SUFFIX)) {
                Object value = entry.getValue();
                if (null != value) {
                    properties.put(key, entry.getValue().toString());
                } else {
                    properties.put(key, null);
                }
            }
        }

        if (logger.isDebugEnabled()) {
            logger.debug("taskId : {}, saveFormData : {}", taskId, properties);
        }
        try {
            formService.saveFormData(taskId, properties);
            rs.success("保存成功");
        } catch (ActivitiObjectNotFoundException e) {
            if (logger.isWarnEnabled()) {
                logger.error("Activiti object not found exception : {}", e.getMessage());
            }
            rs.fail("0","没有找到待办任务");
            throw new RuntimeException(e);
        }
    }

    @Override
    public void processInstanceTaskList(Map<String, Object> mapParams, Response rs) {
        String processInstanceId = (String) mapParams.get("processInstanceId");

        // Fetch all tasks
        List<HistoricTaskInstance> tasks = getTasksByPiid(processInstanceId, true, true);
        if (tasks != null) {
            for (HistoricTaskInstance historicTaskInstance : tasks) {
                logger.debug(historicTaskInstance.getProcessVariables().toString());
            }
        }
        JSONArray array = (JSONArray) JSON.toJSON(tasks);
        rs.success("1","查询成功",array);
        /*rs.setResult(array);
        rs.setResType(Constant.WS_TYPE_FASTJSONARRAY);
        rs.successful("查询成功");*/
    }

    private List<HistoricTaskInstance> getTasksByPiid(String piid, boolean includeProcessVariables,
                                                      boolean includeTaskLocalVariables) {
        // Fetch all tasks
        HistoricTaskInstanceQuery query = historyService.createHistoricTaskInstanceQuery();
        if (includeProcessVariables) {
            query.includeProcessVariables();
        }
        if (includeTaskLocalVariables) {
            query.includeTaskLocalVariables();
        }
        List<HistoricTaskInstance> tasks = query.processInstanceId(piid).orderByHistoricTaskInstanceEndTime().desc()
                .orderByTaskCreateTime().desc().list();
        return tasks;
    }

   /* public void getNextTaskBak(Map<String, Object> mapParams, Response rs) {
        String taskId = (String) mapParams.get(TASK_ID);
        String conditionValue = String.valueOf(mapParams.get("conditionValue")) ;
        String conditionVariable = String.valueOf(mapParams.get("conditionVariable"));
        Map<String, Object> conditionContext = null;

        if (StringUtils.isBlank(conditionVariable)) {
            conditionContext = JSON.parseObject((String) mapParams.get("conditionContext"));
        }

        if (logger.isDebugEnabled()) {
            logger.debug("getNextTask taskId=>{}, conditionVariable=>{}, conditionValue=>{}, conditionContext=>{}",
                    taskId, conditionVariable, conditionValue, conditionContext);
        }

        Task cuurentTask = taskService.createTaskQuery().taskId(taskId).singleResult();

        if (cuurentTask == null) {
            rs.fail("0","当前任务不存在");
            return;
        }

        Map<String, Object> map = this.getVariables(cuurentTask.getProcessInstanceId());
        if (StringUtils.isNotBlank(conditionVariable)) {
            map.put(conditionVariable, conditionValue);
        } else {
            if (conditionValue != null) {
                map.put(cuurentTask.getTaskDefinitionKey() + "_approve", conditionValue);
            } else if (conditionContext != null) {
                map.putAll(conditionContext);
            }
        }

        Set<ActivityBehavior> abs = nextActivityBehavior(cuurentTask, map);

        JSONArray array = behavior2JSONArray(abs, map);
        rs.success("1","查询成功",array);
        *//*rs.successful("查询成功");
        rs.setResult(array);
        rs.setLengths(array.size());
        rs.setResType(Constant.WS_TYPE_FASTJSONARRAY);*//*
    }*/

    @Override
    public void getNextTask(Map<String, Object> mapParams, Response rs) {
        String taskId = (String) mapParams.get(TASK_ID);

        Task cuurentTask = taskService.createTaskQuery().taskId(taskId).singleResult();

        if (cuurentTask == null) {
            rs.fail("0","当前任务不存在");
            return;
        }

        Map<String, Object> map = this.getVariables(cuurentTask.getProcessInstanceId());

        /*Set<ActivityBehavior> abs = nextActivityBehavior(cuurentTask, map);

        JSONArray array = behavior2JSONArray(abs, map);
        rs.success("1","查询成功",array);*/
        /*rs.successful("查询成功");
        rs.setResult(array);
        rs.setLengths(array.size());
        rs.setResType(Constant.WS_TYPE_FASTJSONARRAY);*/
    }

    private Map<String, Object> getVariables(String processInstanceId) {
        Map<String, Object> variables = runtimeService.getVariables(processInstanceId);
        if (logger.isDebugEnabled()) {
            logger.debug("getVariables : processInstanceId=>{}, variables=>{}", processInstanceId, variables);
        }
        return variables;
    }

   /* private Set<ActivityBehavior> nextActivityBehavior(Task task, Map<String, Object> map) {
        // 1.获取流程定义
        ProcessDefinitionEntity def = (ProcessDefinitionEntity) ((RepositoryServiceImpl) repositoryService)
                .getDeployedProcessDefinition(task.getProcessDefinitionId());
        String executionId = task.getExecutionId();
        // 2.获取流程实例
        ExecutionEntity execution = (ExecutionEntity) runtimeService.createExecutionQuery().executionId(executionId)
                .singleResult();
        // 3.通过流程实例查找当前活动的ID
        String activityId = execution.getActivityId();
        // 4.通过活动的ID在流程定义中找到对应的活动对象
        ActivityImpl activity = def.findActivity(activityId);
        if (logger.isDebugEnabled()) {
            logger.debug("Current task : ", activity.getProperty("name"));
        }
        // 5.通过活动对象找当前活动的所有出口
        List<PvmTransition> transitions = activity.getOutgoingTransitions();
        return nextActivityBehaviorForBreadthSearch(transitions, map);
    }*/

    /**
     * 遍历所有出口，找到出口的目标
     *
     * @param transitions
     * @param map
     * @return
     */
   /* public Set<ActivityBehavior> nextActivityBehavior(List<PvmTransition> transitions, Map<String, Object> map) {
        Set<ActivityBehavior> set = new HashSet<ActivityBehavior>();
        if (transitions != null) {
            for (PvmTransition trans : transitions) {
                PvmActivity ac = trans.getDestination();
                String activityType = (String) ac.getProperty("type");
                if (logger.isDebugEnabled()) {
                    logger.debug("PvmActivity => {}, type => {}", ac.getId(), activityType);
                }
                if ("userTask".equals(ac.getProperty("type"))) {
                    ActivityBehavior taskObj = ((ActivityImpl) ac).getActivityBehavior();
                    set.add(taskObj);
                } else {
                    List<PvmTransition> acOutgoings = ac.getOutgoingTransitions();
                    if (acOutgoings != null) {
                        if (acOutgoings.size() == 1) {
                            set.addAll(nextActivityBehavior((ActivityImpl) acOutgoings.get(0).getDestination(), map));
                        } else if (acOutgoings.size() > 1) {
                            for (PvmTransition acOutgoing : acOutgoings) {
                                String conditionText = (String) acOutgoing.getProperty("conditionText");
                                if (StringUtils.isNotBlank(conditionText) && ("exclusiveGateway".equals(activityType)
                                        || "inclusiveGateway".equals(activityType))) {
                                    Boolean flag = (Boolean) WorkflowUtils.getCheckCondition(map, conditionText);
                                    if (flag) {
                                        set.addAll(
                                                nextActivityBehavior((ActivityImpl) acOutgoing.getDestination(), map));
                                        // 排它网关只返回第一次匹配节点
                                        if ("exclusiveGateway".equals(activityType)) {
                                            break;
                                        }
                                    }
                                } else {
                                    set.addAll(nextActivityBehavior((ActivityImpl) acOutgoing.getDestination(), map));
                                }
                            }
                        }
                    }
                }
            }
        }
        return set;
    }*/

    /**
     * 广度遍历所有出口，只找到每条线的第一个UserTask
     *
     * @param transitions
     * @param map
     * @return
     */
   /* public Set<ActivityBehavior> nextActivityBehaviorForBreadthSearch(List<PvmTransition> transitions, Map<String, Object> map) {
        Set<ActivityBehavior> set = new HashSet<ActivityBehavior>();
        Set<ActivityBehavior> tempSet;
        if (transitions != null) {
            for (PvmTransition trans : transitions) {
                tempSet = new HashSet<ActivityBehavior>();
                set.addAll(nextActivityBehaviorForBreadthSearch(trans,tempSet));
            }
        }
        return set;
    }*/

    /**
     * 获取当前一条线的第一个UserTask
     * @param pvmTransition
     * @param set
     * @return
     */
  /*  public Set<ActivityBehavior> nextActivityBehaviorForBreadthSearch(PvmTransition pvmTransition,Set<ActivityBehavior> set) {

        //只获取第一个UserTask节点
        if(set.size() == 1){
            return set;
        }

        if (pvmTransition == null) {
            return set;
        }

        PvmActivity ac = pvmTransition.getDestination();
        String activityType = (String) ac.getProperty("type");
        if (logger.isDebugEnabled()) {
            logger.debug("PvmActivity => {}, type => {}", ac.getId(), activityType);
        }

        if ("userTask".equals(ac.getProperty("type"))) {
            ActivityBehavior taskObj = ((ActivityImpl) ac).getActivityBehavior();
            set.add(taskObj);
            return set;
        } else {  //网关系列
            List<PvmTransition> acOutgoings = ac.getOutgoingTransitions();
            if (acOutgoings != null) {
                for (PvmTransition acOutgoing : acOutgoings) {
                    set.addAll(nextActivityBehaviorForBreadthSearch(acOutgoing, set));
                    if(set.size() == 1){
                        return set;
                    }
                }
            }
        }

        return set;
    }*/

    /**
     * 处理活动是否是userTask，是则返回，不是则通过出口继续向下寻找
     *
     * @param activity
     * @param map
     * @return
     */
   /* private Set<ActivityBehavior> nextActivityBehavior(PvmActivity activity, Map<String, Object> map) {
        Set<ActivityBehavior> set = new HashSet<ActivityBehavior>();
        String activityType = (String) activity.getProperty("type");
        if (logger.isDebugEnabled()) {
            logger.debug("PvmActivity => {}, type => {}", activity.getId(), activityType);
        }
        if ("userTask".equals(activityType)) {
            ActivityBehavior taskObj = ((ActivityImpl) activity).getActivityBehavior();
            set.add(taskObj);
        } else {
            List<PvmTransition> transitions = activity.getOutgoingTransitions();

            set.addAll(nextActivityBehavior(transitions, map));
        }
        return set;
    }*/

    @Override
    public void userTaskDetails(Map<String, Object> mapParams, Response rs) {
        String taskId = (String) mapParams.get(TASK_ID);

        if (logger.isDebugEnabled()) {
            logger.debug("userTaskDetails : taskId->{}", taskId);
        }
        try {

            TaskFormDataImpl taskFormData = (TaskFormDataImpl) formService.getTaskFormData(taskId);
            Task task = taskService.createTaskQuery().taskId(taskId).includeTaskLocalVariables()
                    .includeProcessVariables().active().singleResult();

            SimplePropertyPreFilter filter = new SimplePropertyPreFilter(Task.class,
                    new String[] { "createTime", "deleted", "suspended", "id", "processInstanceId", "priority",
                            "taskDefinitionKey", "name", "processDefinitionId" });

            JSONObject object = JSON.parseObject(JSON.toJSONString(task, filter, SerializerFeature.WriteMapNullValue));
            TaskEntity taskEntity = (TaskEntity) task;
            List<VariableInstanceEntity> variables = taskEntity.getQueryVariables();
            JSONObject vars = new JSONObject();
            for (VariableInstanceEntity variable : variables) {
                vars.put(variable.getName(), variable.getCachedValue());
            }
            object.put("variables", vars);
            JSONArray array = new JSONArray();
            if (taskFormData != null && taskFormData.getFormProperties() != null) {
                List<FormProperty> formProperties = taskFormData.getFormProperties();
                for (FormProperty formProperty : formProperties) {
                    JSONObject form = (JSONObject) JSON.toJSON(formProperty);
                    if ("enum".equals(formProperty.getType().getName())) {
                        @SuppressWarnings("unchecked")
                        Map<String, String> values = (Map<String, String>) formProperty.getType()
                                .getInformation("values");
                        if (values != null) {
                            if (logger.isDebugEnabled()) {
                                for (Map.Entry<String, String> enumEntry : values.entrySet()) {
                                    logger.debug("enum, key: {}, value: {}", enumEntry.getKey(), enumEntry.getValue());
                                }
                            }
                            form.put("values", values);
                        }
                    } else if ("date".equals(formProperty.getType().getName())) {
                        String datePattern = (String) formProperty.getType().getInformation("datePattern");
                        if (datePattern != null) {
                            if (logger.isDebugEnabled()) {
                                logger.debug("date, datePattern: {}", datePattern);
                            }
                            form.put("datePattern", datePattern);
                        }
                    }
                    array.add(form);
                }
            }
            object.put("formProperties", array);
            rs.success("1","查询成功",object);
            /*rs.setResult(object);
            rs.setResType(Constant.WS_TYPE_FASTJSONOBJECT);
            rs.successful("查询成功");*/
        } catch (ActivitiObjectNotFoundException e) {
            if (logger.isWarnEnabled()) {
                logger.warn("Activiti object not found exception : taskId = > {}, by : {}", taskId, e.getMessage());
            }
            rs.fail("0","没有找到待办任务");
        } catch (ActivitiException e) {
            logger.error("Activiti exception : ", e);
            rs.fail("0","没有找到表单配置");
        }
    }

    private void addCandidateUserOrClaimWithHistory(Task task) {
        String key = task.getTaskDefinitionKey();
        String historicAssignment = null;

        List<HistoricVariableInstance> variables = historyService.createHistoricVariableInstanceQuery()
                .variableName("_$_" + key + "_assignment").processInstanceId(task.getProcessInstanceId()).list();
        if (variables.size() > 0) {
            HistoricVariableInstanceEntity variable = (HistoricVariableInstanceEntity) variables.get(0);
            historicAssignment = (String) variable.getCachedValue();
        }
        if (StringUtils.isNotEmpty(historicAssignment)) {
            // 代签收
            if (logger.isDebugEnabled()) {
                logger.debug("Claim task [{}] with historic assignment user [{}]", task.getId(), historicAssignment);
            }
            taskService.claim(task.getId(), historicAssignment);
        }
    }

    @Override
    public void getNextGateway(Map<String, Object> mapParams, Response rs) {
        String taskId = String.valueOf(mapParams.get(TASK_ID));
        String conditionValue = String.valueOf(mapParams.get("conditionValue"));
        String conditionVariable = String.valueOf(mapParams.get("conditionVariable"));
        Map<String, Object> conditionContext = null;

        if (StringUtils.isBlank(conditionVariable)) {
            conditionContext = JSON.parseObject((String) mapParams.get("conditionContext"));
        }

        if (logger.isDebugEnabled()) {
            logger.debug("getNextGateway taskId=>{}, conditionVariable=>{}, conditionValue=>{}, conditionContext=>{}",
                    taskId, conditionVariable, conditionValue, conditionContext);
        }

        Task cuurentTask = taskService.createTaskQuery().taskId(taskId).singleResult();
        if (cuurentTask == null) {
            rs.fail("0","当前任务不存在");
            return;
        }

        Map<String, Object> map = this.getVariables(cuurentTask.getProcessInstanceId());
        if (StringUtils.isNotBlank(conditionVariable)) {
            map.put(conditionVariable, conditionValue);
        } else {
            //map.put(cuurentTask.getTaskDefinitionKey() + "_approve", conditionValue);
            if (conditionValue != null) {
                map.put(cuurentTask.getTaskDefinitionKey() + "_approve", conditionValue);
            } else if (conditionContext != null) {
                map.putAll(conditionContext);
            }
        }
        Set<PvmActivity> set = nextGateway(cuurentTask, map);
        JSONArray array = new JSONArray();
        for (PvmActivity activiti : set) {
            JSONObject object = new JSONObject();
            object.put("id", activiti.getId());
            object.put("join", activiti.getIncomingTransitions().size());
            object.put("fork", activiti.getOutgoingTransitions().size());
            String activityType = (String) activiti.getProperty("type");
            object.put("type", activityType);
            array.add(object);
        }
        rs.success("1","查询成功",array);
        /*rs.successful("查询成功");
        rs.setResult(array);
        rs.setLengths(array.size());
        rs.setResType(Constant.WS_TYPE_FASTJSONARRAY);*/
    }

    private Set<PvmActivity> nextGateway(Task task, Map<String, Object> map) {
        // 1.获取流程定义
        ProcessDefinitionEntity def = (ProcessDefinitionEntity) ((RepositoryServiceImpl) repositoryService)
                .getDeployedProcessDefinition(task.getProcessDefinitionId());
        String executionId = task.getExecutionId();
        // 2.获取流程实例
        ExecutionEntity execution = (ExecutionEntity) runtimeService.createExecutionQuery().executionId(executionId)
                .singleResult();
        // 3.通过流程实例查找当前活动的ID
        String activityId = execution.getActivityId();
        // 4.通过活动的ID在流程定义中找到对应的活动对象
        ActivityImpl activity = def.findActivity(activityId);
        if (logger.isDebugEnabled()) {
            logger.debug("Current task : ", activity.getProperty("name"));
        }
        // 5.通过活动对象找当前活动的所有出口
        List<PvmTransition> transitions = activity.getOutgoingTransitions();
        return nextGateway(transitions, map);
    }

    public Set<PvmActivity> nextGateway(List<PvmTransition> transitions, Map<String, Object> map) {
        Set<PvmActivity> set = new HashSet<PvmActivity>();
        if (transitions != null) {
            for (PvmTransition trans : transitions) {
                PvmActivity ac = trans.getDestination();
                String activityType = (String) ac.getProperty("type");
                if (logger.isDebugEnabled()) {
                    logger.debug("PvmActivity => {}, type => {}", ac.getId(), activityType);
                }
                if ("userTask".equals(activityType)) {
                    continue;
                } else if (("parallelGateway".equals(activityType) || "inclusiveGateway".equals(activityType))) {
                    set.add(ac);
                } else {
                    List<PvmTransition> acOutgoings = ac.getOutgoingTransitions();
                    if (acOutgoings != null) {
                        if (acOutgoings.size() == 1) {
                            set.addAll(nextGateway((ActivityImpl) acOutgoings.get(0).getDestination(), map));
                        } else if (acOutgoings.size() > 1) {
                            for (PvmTransition acOutgoing : acOutgoings) {
                                /*String conditionText = (String) acOutgoing.getProperty("conditionText");
                                if (StringUtils.isNotBlank(conditionText) && "exclusiveGateway".equals(activityType)) {
                                    Boolean flag = (Boolean) WorkflowUtils.getCheckCondition(map, conditionText);
                                    if (logger.isDebugEnabled()) {
                                        logger.debug("conditionText => {}, result => {}", conditionText, flag);
                                    }
                                    if (flag) {
                                        set.addAll(nextGateway((ActivityImpl) acOutgoing.getDestination(), map));
                                        break;
                                    }
                                } else {*/
                                    set.addAll(nextGateway((ActivityImpl) acOutgoing.getDestination(), map));
                                /*}*/
                            }
                        }
                    }
                }
            }
        }
        return set;
    }

    private Set<PvmActivity> nextGateway(PvmActivity activity, Map<String, Object> map) {
        Set<PvmActivity> set = new HashSet<PvmActivity>();
        String activityType = (String) activity.getProperty("type");
        if (logger.isDebugEnabled()) {
            logger.debug("PvmActivity => {}, type => {}", activity.getId(), activityType);
        }
        if (("parallelGateway".equals(activityType) || "inclusiveGateway".equals(activityType))) {
            set.add(activity);
        } else if (!"userTask".equals(activityType)) {
            List<PvmTransition> transitions = activity.getOutgoingTransitions();
            set.addAll(nextGateway(transitions, map));
        }
        return set;
    }

    private JSONArray behavior2JSONArray(Set<ActivityBehavior> abs, Map<String, Object> map) {
        JSONArray array = new JSONArray();
        if (abs != null) {
            SimplePropertyPreFilter filter = new SimplePropertyPreFilter(TaskDefinition.class, new String[] { "key",
                    "nameExpression", "descriptionExpression", "assigneeExpression", "candidateGroupIdExpressions" });
            for (ActivityBehavior behavior : abs) {
                JSONObject object = null;
                if (behavior instanceof UserTaskActivityBehavior) {
                    TaskDefinition taskDef = ((UserTaskActivityBehavior) behavior).getTaskDefinition();
                    String assignmentKey = "_$_" + taskDef.getKey() + "_assignment";
                    object = JSON.parseObject(JSON.toJSONString(taskDef, filter, SerializerFeature.WriteMapNullValue));
                    object.put("historyAssignment", map.get(assignmentKey));
                } else if (behavior instanceof ParallelMultiInstanceBehavior) {
                    ParallelMultiInstanceBehavior parallel = (ParallelMultiInstanceBehavior) behavior;
                    TaskDefinition taskDef = ((UserTaskActivityBehavior) parallel.getInnerActivityBehavior())
                            .getTaskDefinition();
                    String assignmentKey = "_$_" + taskDef.getKey() + "_assignment";
                    object = JSON.parseObject(JSON.toJSONString(taskDef, filter, SerializerFeature.WriteMapNullValue));
                    object.put("historyAssignment", map.get(assignmentKey));
                    object.put("multiinstance_type", "Parallel");
                    Expression collectionExpression = parallel.getCollectionExpression();
                    if (collectionExpression != null) {
                        object.put("multiinstance_collection", collectionExpression.getExpressionText());
                    }
                    object.put("multiinstance_variable", parallel.getCollectionVariable());
                } else if (behavior instanceof SequentialMultiInstanceBehavior) {
                    SequentialMultiInstanceBehavior sequential = (SequentialMultiInstanceBehavior) behavior;
                    TaskDefinition taskDef = ((UserTaskActivityBehavior) sequential.getInnerActivityBehavior())
                            .getTaskDefinition();
                    String assignmentKey = "_$_" + taskDef.getKey() + "_assignment";
                    object = JSON.parseObject(JSON.toJSONString(taskDef, filter, SerializerFeature.WriteMapNullValue));
                    object.put("historyAssignment", map.get(assignmentKey));
                    object.put("multiinstance_type", "Sequential");
                    Expression collectionExpression = sequential.getCollectionExpression();
                    if (collectionExpression != null) {
                        object.put("multiinstance_collection", collectionExpression.getExpressionText());
                    }
                    object.put("multiinstance_variable", sequential.getCollectionVariable());
                }
                array.add(object);
            }
        }
        return array;
    }

    @Override
    public JSONArray nextTaskDefinition(List<PvmTransition> transitions, Map<String, Object> map) {
        /*Set<ActivityBehavior> abs = nextActivityBehavior(transitions, map);
        return behavior2JSONArray(abs, map);*/

        return null;
    }

    /**
     * TODO 可以优化
     * @param paramMap
     * @param rs
     */
    public void getCurrentActivityNames(Map<String, Object> paramMap,Response rs) {
        String processInstanceIds = (String) paramMap.get("processInstanceIds");
        if(VerificationUtil.isEmpty(processInstanceIds)){
            rs.fail("0","流程实例不能为空!");
            return;
        }
        String[] processInstanceIdArray = processInstanceIds.split(",");
        Map processInstanceIdNameMap ;
        List processInstanceIdNameMapList = new ArrayList(processInstanceIdArray.length);
        try{
            for (String processInstanceId : processInstanceIdArray){
                processInstanceIdNameMap = new HashMap(2);
                processInstanceIdNameMap.put("processInstanceId",processInstanceId);
                processInstanceIdNameMap.put("processActivityName",getCurrentActivityName(processInstanceId));
                processInstanceIdNameMapList.add(processInstanceIdNameMap);
            }
        }catch (Exception e){
            logger.error("获取流程实例进度信息异常!",e);
            rs.fail("0","获取流程实例进度信息异常:" + e.getMessage());
            return;
        }
        rs.success("1","获取流程实例进度信息成功",processInstanceIdNameMapList);
        /*ResultUtil.makerSusResults("获取流程实例进度信息成功!",processInstanceIdNameMapList,rs);*/
    }

    /**
     * 查询是否到达最后一个任务
     * @param paramMap
     * @param rs
     */
    public void isTheLastStep(Map<String, Object> paramMap,Response rs) {
        String taskId = (String) paramMap.get("taskId");
        if(VerificationUtil.isEmpty(taskId)){
            rs.fail("0","任务ID不能为空!");
            return;
        }
        String approve = (String) paramMap.get("approve");
        if(VerificationUtil.isEmpty(approve)){
            rs.fail("0","approve变量不能为空!");
            return;
        }
        String comment = (String) paramMap.get("comment");
        if(VerificationUtil.isEmpty(comment)){
            rs.fail("0","comment不能为空!");
            return;
        }
        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
        if (task == null) {
            rs.fail("0","没有找到任务信息!");
            return;
        }
        Map islastMap = new HashMap(2);
        //手动控制事务
        //TransactionStatus status = this.newTransaction();
        try{
            //辅助变量
            Map variables = new HashMap(4);
            variables.put(task.getTaskDefinitionKey() + "_approve",paramMap.get("approve"));
            variables.put("comment",paramMap.get("comment"));

            taskService.complete(taskId,variables);

            if(isProcessInstanceFinish(task.getProcessInstanceId())){
                islastMap.put("islast","1");
            }else {
                islastMap.put("islast","0");
            }
            rs.success("1","查询是否最后一个流程任务成功",Arrays.asList(islastMap));
            /*ResultUtil.makerSusResults("查询是否最后一个流程任务成功!", Arrays.asList(islastMap),rs);*/
        }catch (ActivitiIllegalArgumentException e){
            //如果流程引擎抛出变量未设置，则流程没有走完，忽略异常
            islastMap.put("islast","0");
            rs.success("1","查询是否最后一个流程任务成功",Arrays.asList(islastMap));
            /*ResultUtil.makerSusResults("查询是否最后一个流程任务成功!", Arrays.asList(islastMap),rs);*/
        }catch (org.activiti.engine.ActivitiException e){
            //表达式异常，则通过图形来辅助判断
            if(e.getCause() instanceof  org.activiti.engine.impl.javax.el.PropertyNotFoundException){
                BpmnModel bpmnModel = repositoryService.getBpmnModel(task.getProcessDefinitionId());
                UserTask userTask = (UserTask) WorkflowUtils.getSpecifiedElement(task.getTaskDefinitionKey(),bpmnModel);
                if(VerificationUtil.isEmpty(userTask.getOutgoingFlows())){
                    islastMap.put("islast","1");
                    rs.success("1","查询是否最后一个流程任务成功",Arrays.asList(islastMap));
                    /*ResultUtil.makerSusResults("查询是否最后一个流程任务成功!", Arrays.asList(islastMap),rs);*/
                }else if(userTask.getOutgoingFlows().size() == 1){ //只有一条线出口，指向一个网关，并且网关出口都指向结束节点
                    SequenceFlow sequenceFlow = userTask.getOutgoingFlows().get(0);
                    if(isExecEndElement(sequenceFlow.getTargetRef(),bpmnModel)){
                        islastMap.put("islast","1");
                        rs.success("1","查询是否最后一个流程任务成功",Arrays.asList(islastMap));
                       /* ResultUtil.makerSusResults("查询是否最后一个流程任务成功!", Arrays.asList(islastMap),rs);*/
                    }else {
                        islastMap.put("islast","0");
                        rs.success("1","查询是否最后一个流程任务成功",Arrays.asList(islastMap));
                        /*ResultUtil.makerSusResults("查询是否最后一个流程任务成功!", Arrays.asList(islastMap),rs);*/
                    }
                }else {
                    islastMap.put("islast","0");
                    rs.success("1","查询是否最后一个流程任务成功",Arrays.asList(islastMap));
                    /*ResultUtil.makerSusResults("查询是否最后一个流程任务成功!", Arrays.asList(islastMap),rs);*/
                }
            }else{
                rs.fail("0","查询是否最后一个流程任务异常:" + e.getMessage());
                logger.error("查询是否最后一个流程任务异常!",e);
            }
        }catch (Exception e){
            rs.fail("0","查询是否最后一个流程任务异常:" + e.getMessage());
            logger.error("查询是否最后一个流程任务异常!",e);
        }finally {
            //因为尝试了执行流程任务,这里必须进行回退操作
            /*if(status != null){
                this.rollback(status);
            }*/
        }
    }

    /**
     * 判断一个节点执行路径是否全部指向结束节点
     * @param activityId
     * @param model
     * @return
     */
    public static boolean isExecEndElement(String activityId,BpmnModel model){
        FlowNode flowNode = (FlowNode)WorkflowUtils.getSpecifiedElement(activityId,model);
        List<SequenceFlow> outgoingFlows = flowNode.getOutgoingFlows();
        return outgoingFlows.stream().filter((e -> WorkflowUtils.isEndElement(e.getTargetRef(),model))).count()
                == outgoingFlows.size();
    }


    public String getCurrentActivityName(String processInstanceId) {
        // 1.获取流程实例信息
        HistoricProcessInstance processInstance = historyService.createHistoricProcessInstanceQuery().processInstanceId(processInstanceId).singleResult();
        if(processInstance == null){
            return "";
        }
        // 2.获取流程定义
        ProcessDefinitionEntity def = (ProcessDefinitionEntity) ((RepositoryServiceImpl) repositoryService)
                .getDeployedProcessDefinition(processInstance.getProcessDefinitionId());
        // 3.获取流程执行实例
        List<Execution> executionList = runtimeService.createExecutionQuery().
                processInstanceId(processInstanceId).list();
        // 4.通过流程执行实例查找当前活动的ID
        StringBuilder sb = new StringBuilder(16);
        for (Execution execution : executionList){
            String activityId = execution.getActivityId();
            if(activityId == null){  //流程实例execution，跳过
                continue;
            }
            // 5.通过活动的ID在流程定义中找到对应的活动对象
            ActivityImpl activity = def.findActivity(activityId);
            sb.append(activity.getProperty("name"));
            sb.append(",");
            break; //只需要取一次name即可
        }
        return  sb.length() == 0?"":sb.subSequence(0,sb.length() - 1).toString();
    }

}
