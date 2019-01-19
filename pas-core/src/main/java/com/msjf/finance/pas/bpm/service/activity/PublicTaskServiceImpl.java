package com.msjf.finance.pas.bpm.service.activity;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.msjf.finance.msjf.core.response.Response;
import com.msjf.finance.pas.bpm.common.ParametersConstant;
import com.msjf.finance.pas.bpm.dao.mapper.CustProStateDao;
import com.msjf.finance.pas.bpm.dao.mapper.PasHisProcessinstanceDao;
import com.msjf.finance.pas.bpm.dao.mapper.PasProTodoDao;
import com.msjf.finance.pas.bpm.service.KbpmTaskService;
import com.msjf.finance.pas.bpm.service.ProcessDefinitionService;
import com.msjf.finance.pas.bpm.service.ProcessInstanceService;
import com.msjf.finance.pas.bpm.service.PublicTaskService;
import com.msjf.finance.pas.common.*;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.UserTask;
import org.activiti.engine.*;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

import javax.annotation.Resource;
import java.util.*;

import static com.msjf.finance.pas.bpm.common.ParametersConstant.*;
import static com.msjf.finance.pas.common.StringUtil.valueOf;

/**
 * Created by 成俊平 on 2018/12/28.
 */

@Service("publicTaskService")
@Transactional
public class PublicTaskServiceImpl implements PublicTaskService {

    protected Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private RuntimeService runtimeService;
    @Autowired
    private TaskService taskService;
    @Autowired
    private FormService formService;
    @Autowired
    private HistoryService historyService;
    @Autowired
    private RepositoryService repositoryService;
    @Autowired
    private IdentityService identityService;

    @Autowired
    private ProcessDefinitionService processDefinitionService;

    @Autowired
    private ProcessInstanceService processInstanceService;

    @Autowired
    private KbpmTaskService kbpmTaskService;


    @Resource
    CustProStateDao custProStateDao;
    @Resource
    PasHisProcessinstanceDao hisProcessinstanceDao;
    @Resource
    PasProTodoDao proTodoDao;


    @Override
    public Response createFlow(Map<String, Object> mapParam) throws RuntimeException {
        Response result = new Response();
        if(!checkCreateFlowParam(mapParam, result)){
            return result;
        }
        // 插入申请流程
        Map<String, Object> resultHashMap = commitFormData(mapParam);

        //加入一些key以fp_前缀的K-V,流程引擎会保存
        mapParam.forEach((key,value)->{
            if(key.startsWith("fp_")){
                resultHashMap.put(key,value);
            }
        });
        // 发起流程
        return startFlow(mapParam);
    }

    @Override
    public Response executeNextStep(Map<String, Object> mapParam) throws Exception {
        if (StringUtil.isNull(mapParam.get("processInstanceId"))) {
            return new Response().fail("0","processInstanceId不能为空");
        }
        if (StringUtil.isNull(mapParam.get("taskId"))) {
            return new Response().fail("0","taskId");
        }
        if (StringUtil.isNull(mapParam.get("userId"))) {
            return new Response().fail("0","userId");
        }
        if (StringUtil.isNull(mapParam.get("userName"))) {
            return new Response().fail("0","userName");
        }
        if (StringUtil.isNull(mapParam.get("taskDefinitionKey"))) {
            return new Response().fail("0","taskDefinitionKey");
        }
        if(StringUtil.isNull(mapParam.get("approve"))){
            mapParam.put("approve",1);
        }

        return taskSubmitForm(mapParam);
    }

    /**
     * 验证发起流程参数
     *
     * @param mapParam
     * @param rs
     */
    public Boolean checkCreateFlowParam(Map<String, Object> mapParam,
                                     Response rs) {

        if (StringUtil.isNull(mapParam.get("processDefinitionId"))) {
            rs.fail("0","流程定义Id不存在!");
            return false;
        }
        if (StringUtil.isNull(mapParam.get("custName"))) {
            rs.fail("0","企业名称不存在!");
            return false;
        }
        if (StringUtil.isNull(mapParam.get("custNo"))) {
            rs.fail("0","企业客户号不存在!");
            return false;
        }
        if (StringUtil.isNull(mapParam.get("userId"))) {
            rs.fail("0","当前用户id不存在!");
            return false;
        }
        if (StringUtil.isNull(mapParam.get("userName"))) {
            rs.fail("0","当前用户名称不存在!");
            return false;
        }
        rs.success("检验成功!");
        return true;
    }

    /**
     * 增加到审核人表
     *
     * @param mapParam
     *
     */
    public void addTodo(Map<String, Object> mapParam) {
        try {
            proTodoDao.addPasProTodoList(mapParam);
        }catch (Exception e){
            //打印错误日志
            e.printStackTrace();
            throw new RuntimeException("更新表失败");
        }
    }

    /**
     * 增加审核历史记录
     *
     * @param mapParam
     *
     */
    public void addHisbpm(Map<String, Object> mapParam) {
        try {
            hisProcessinstanceDao.addPasHisProcessinstance(mapParam);
        }catch (Exception e){
            //打印错误日志
            e.printStackTrace();
            throw new RuntimeException("更新表失败");
        }
    }

    /**
     * 修改企业流程状态
     *
     * @param mapParam
     *
     */
    public void updCustState(Map<String, Object> mapParam) {
        try {
            List<Map> list = custProStateDao.queryCustProStateList(mapParam);
            if(list!=null&&list.size()>0){
                custProStateDao.updateCustProState(mapParam);
            }else{
                custProStateDao.addCustProState(mapParam);
            }
        }catch (Exception e){
            //打印错误日志
            e.printStackTrace();
            throw new RuntimeException("更新表失败");
        }
    }

    /**
     * 提交表单数据
     *
     * @param mapParam
     */
    public Map<String, Object> commitFormData(
            Map<String, Object> mapParam) throws RuntimeException {

        HashMap<String, Object> formParamHashMap = new HashMap<>();
        formParamHashMap.put("koauth2EmployeeId", mapParam.get("koauth2EmployeeId"));
        formParamHashMap.put("customerno", mapParam.get("customerno"));
        formParamHashMap.put("tablename", mapParam.get("tablename"));
        String bexno = valueOf(formParamHashMap.get("applytype"));
        String pageid = valueOf(formParamHashMap.get("pageid"));
        Map<String, Object> resultHashMap = new HashMap<String, Object>();
        if(StringUtil.isNull(pageid)){

            resultHashMap.put("bexno", bexno);
            resultHashMap.put("title", formParamHashMap.get("title"));
            resultHashMap.put("applyid", formParamHashMap.get("applyid"));
            resultHashMap.put("tablename", formParamHashMap.get("tablename"));
            return resultHashMap;
        }
        mapParam.put("bexno", bexno);
        Response result = new Response();
        String startFlowEvent = getStartFlowEvent(mapParam, result);
        resultHashMap.put("startFlowEvent",startFlowEvent);
        return resultHashMap;
    }


    /**
     * 获取开始流程事件
     *
     * @param mapParam
     * @param result
     */
    public String getStartFlowEvent(Map<String, Object> mapParam,
                                    Response result) {
        String taskEvent = "";
        List<Map<String, Object>> startFlowList = selectStartFlowForm(
                mapParam, result);
        for (Map<String, Object> startFlowMap : startFlowList) {
            if ("process_eventaudit".equals(startFlowMap.get("id"))) {
                Map<String, Object> valuesHashMap = (Map)startFlowMap.get("values");
                taskEvent = valueOf(valuesHashMap.get("value"));
            }
        }
        return taskEvent;

    }


    /**
     * 查询发起流程表单
     *
     * @param mapParam
     * @param result
     */
    public List<Map<String, Object>> selectStartFlowForm(
            Map<String, Object> mapParam, Response result) {
        List<Map<String, Object>> startFlowParamList = new ArrayList<Map<String, Object>>();
        try {
            HashMap<String, Object> paramHashMap = new HashMap<String, Object>();
            // 查询流程编号
            String processUUID = (String)mapParam.get("processDefinitionId");
            paramHashMap.put("processDefinitionId", processUUID);
            result = processDefinitionService.findStartForm(paramHashMap);

            startFlowParamList = (List<Map<String, Object>>)result.getData();
            result.success("1","获取发起流程表单成功!",startFlowParamList);
        } catch (Exception e) {
            e.printStackTrace();
            result.fail("0","获取发起流程表单失败!");
        }
        return startFlowParamList;
    }


    /**
     * 启动流程
     *
     * @param resultHashMap
     */
    public Response startFlow(Map<String, Object> resultHashMap) {
        Map<String, Object> startFlowParamHashMap = resultHashMap;
        startFlowParamHashMap.put("fp_title", resultHashMap.get("title"));
        startFlowParamHashMap.put("fp_applyid", resultHashMap.get("applyid"));
        startFlowParamHashMap.put("fp_kgrpTaskId",resultHashMap.get("taskId"));
        startFlowParamHashMap.put("fp_configId",resultHashMap.get("configId"));
        startFlowParamHashMap.put("fp_departmentId",resultHashMap.get("departmentId"));
        startFlowParamHashMap.put("fp_isCreate","1");
        startFlowParamHashMap.put("fp_customParam",resultHashMap.get("customParam"));
        startFlowParamHashMap.put("userIds", resultHashMap.get("userIds"));
        startFlowParamHashMap.put("user",
                resultHashMap.get("koauth2EmployeeId"));
        startFlowParamHashMap.put("processDefinitionId",
                resultHashMap.get("processDefinitionId"));
        Response result = new Response();
        submitStartFormAndStartProcessInstance(startFlowParamHashMap,result);
        return result;
    }

    public void submitStartFormAndStartProcessInstance(Map<String, Object> mapParams, Response rs) {
        String processDefinitionId =  (String)mapParams.get("processDefinitionId");
        BpmnModel bpmnModel = repositoryService.getBpmnModel(processDefinitionId);
        List<UserTask> userTaskList = WorkflowUtils.getOrderUserTask(bpmnModel);

        if(ObjectUtils.isEmpty(userTaskList)){
            throw new RuntimeException("流程模型异常，找不到第一个模型节点!");
        }

        String userId = (String) mapParams.get(USER_ID);
        Object userIds = mapParams.get(USER_IDS);
        String custName = (String)mapParams.get("custName");
        String custNo = (String)mapParams.get("custNo");
        String userName =(String)mapParams.get("userName");

        Map<String, String> submitFormProperties = new HashMap<String, String>();

        // 从mapParams中读取参数然后转换
        // 所有以fp_开始的参数都复制到formPraperties中，用于保存到用户任务中
        mapParams.forEach((key,value) -> {
            if(key.startsWith(ParametersConstant.FORM_PRAPERTIES_SUFFIX)){
                submitFormProperties.put(key.replaceFirst(ParametersConstant.FORM_PRAPERTIES_SUFFIX, ""),
                        value != null ? value.toString() : null);
            }
        });

        if (logger.isDebugEnabled()) {
            logger.debug("Submit start form parameters: {}", submitFormProperties);
        }

        try {
            // 查询流程启动参数，处理内置参数
            WorkflowUtils.setAndAddTaskFormData(formService.getStartFormData(processDefinitionId)
                    ,submitFormProperties);

            identityService.setAuthenticatedUserId(userId+",starter");
            //保存前端设置的动态审核人
            addDynamicAssignees(userIds,submitFormProperties);

            ProcessInstance processInstance = formService.submitStartFormData(processDefinitionId,
                    submitFormProperties);
            if (logger.isDebugEnabled()) {
                logger.debug("start a processInstance : {}", processInstance);
            }

            if (processInstance != null) {

                ExecutionEntity executionEntity = (ExecutionEntity) processInstance;
                String identUserIds = "";//发起人加审核人
                String idUserIds = "";//审核人


                String proDefKeyName = bpmnModel.getProcesses().get(0).getName();
                List<Map> todoList = new ArrayList<>();
                Map<String,Object> userMap = new HashMap<>();
                userMap.put("processInstanceId",processInstance.getProcessInstanceId());
                List<Map> userList = hisProcessinstanceDao.getTaskUsersByProcInsId(userMap);
                if(ObjectUtils.isEmpty(userList)){
                    rs.fail("0","待审核人列表为空！");
                    throw new RuntimeException("待审核人列表为空！");
                }
                for(int i=0;i<userList.size();i++){
                    Map<String,Object> taskMap = new HashMap<>();
                    String[] assignees = userList.get(i).get("userId").toString().split("\\|");
                    String assigeneeid = assignees[0];
                    String assigeneeName = assignees[1];
                    taskMap.put("proInstance",processInstance.getProcessInstanceId());
                    taskMap.put("actId",userList.get(i).get("taskDefinitionKey"));
                    taskMap.put("actName",userList.get(i).get("actName"));
                    taskMap.put("proDefKey",processDefinitionId);
                    taskMap.put("proDefName",proDefKeyName);
                    taskMap.put("auditorId",assigeneeid);
                    taskMap.put("auditorName",assigeneeName);
                    taskMap.put("taskId",userList.get(i).get("id"));
                    todoList.add(taskMap);
                }
                Map<String,Object> todoMap = new HashMap<>();
                todoMap.put("list",todoList);
                addTodo(todoMap);

                Map<String,Object> HisBpmMap = new HashMap<>();
                HisBpmMap.put("Id", StringUtil.getUUID());
                HisBpmMap.put("custName", custName);
                HisBpmMap.put("custNo", custNo);
                HisBpmMap.put("proInstance", processInstance.getProcessInstanceId());
                HisBpmMap.put("proDefKey", processDefinitionId);
                HisBpmMap.put("proDefName", proDefKeyName);
                HisBpmMap.put("endTime", DateUtils.getDate(DateUtils.DATE_FMT_DATETIME));
                HisBpmMap.put("auditorId", userId);
                HisBpmMap.put("auditorName", userName);
                HisBpmMap.put("auditResult", "发起流程");
                addHisbpm(HisBpmMap);

                JSONObject object = new JSONObject();
                object.put("processInstanceId", executionEntity.getProcessInstanceId());
                object.put("identUserIds", identUserIds);
                object.put("idUserIds", idUserIds);
                object.put("activityId", executionEntity.getActivityId());
                object.put("activityName", executionEntity.getCurrentActivityName());

                JSONArray array = new JSONArray();
                array.add(object);
                rs.success("1","流程实例启动成功",array);
            } else {
                rs.fail("0","流程启动失败:");
            }
        } catch (Exception e) {
            rs.fail("0","流程启动失败:" + e.getMessage());
            logger.error("流程启动失败,Exception : ", e);
        } finally {
            identityService.setAuthenticatedUserId(null);
        }

    }


    public void addDynamicAssignees(Object userIds,Map variables){
        if (userIds instanceof String && StringUtils.isNotBlank(userIds.toString())) {
            JSONObject userJson = (JSONObject) JSON.parse(userIds.toString());
            variables.put(USER_IDS,userJson);
        } else if (userIds instanceof Map) {
            variables.put(USER_IDS,userIds);
        }
    }


    /**
     * 提交下一步事件
     *
     * @param mapParam
     */
    public Response taskSubmitForm(Map<String, Object> mapParam) throws Exception {
        HashMap<String, Object> nextStepHashMap = new HashMap<String, Object>();
        // 当前审核人
        nextStepHashMap.put("userId", mapParam.get("userId"));
        nextStepHashMap.put("userName", mapParam.get("userName"));
        nextStepHashMap.put("custNo", mapParam.get("custNo"));
        nextStepHashMap.put("custName", mapParam.get("custName"));
        nextStepHashMap.put("taskId", mapParam.get("taskId"));
        nextStepHashMap.put("approve", mapParam.get("approve"));
        // 设置审核结果
        if(NumberUtils.isNumber(mapParam.get("approve").toString())){
            nextStepHashMap.put("fp_" + mapParam.get("taskDefinitionKey")
                    + "_approve", mapParam.get("approve"));
        }else{
            nextStepHashMap.put("fp_" + mapParam.get("taskDefinitionKey")
                    + "_approve", 1);
        }

        nextStepHashMap.put("fp_lastStep", mapParam.get("taskDefinitionKey"));
        nextStepHashMap.put("fp_" + mapParam.get("taskDefinitionKey")
                + "_auditinfo", mapParam.get("auditinfo"));
        nextStepHashMap.put("fp_kgrpTaskId",mapParam.get("kgrpTaskId"));
        nextStepHashMap.put("fp_departmentId",mapParam.get("departmentId"));
        nextStepHashMap.put("fp_processInstanceId",mapParam.get("processInstanceId"));
        nextStepHashMap.put("fp_isCreate","0");
        nextStepHashMap.put("fp_fid",mapParam.get("approve"));
        //加入一些key以fp_前缀的K-V,流程引擎会保存
        mapParam.forEach((key,value)->{
            if(key.startsWith("fp_")){
                nextStepHashMap.put(key,value);
            }
        });

        // 设置审核意见
        nextStepHashMap.put("comment", mapParam.get("comment"));
        String taskId = (String) mapParam.get(TASK_ID);
        Task task = taskService.createTaskQuery().includeProcessVariables().taskId(taskId).singleResult();
        BpmnModel bpmnModel = repositoryService.getBpmnModel(task.getProcessDefinitionId());
        // 跳转节点
        Response result = new Response();
        kbpmTaskService.submitTaskFormData(nextStepHashMap,result);
        if(result.checkIfSuccess()){
            String userId = (String) mapParam.get(USER_ID);
            Object userIds = mapParam.get(USER_IDS);
            String custName = (String)mapParam.get("custName");
            String custNo = (String)mapParam.get("custNo");
            String userName =(String)mapParam.get("userName");

            Map<String,Object> HisBpmMap = new HashMap<>();
            HisBpmMap.put("Id", StringUtil.getUUID());
            HisBpmMap.put("custName", custName);
            HisBpmMap.put("custNo", custNo);
            HisBpmMap.put("proInstance", task.getProcessInstanceId());
            HisBpmMap.put("proDefKey", task.getProcessDefinitionId());

            HisBpmMap.put("proDefName", bpmnModel.getProcesses().get(0).getName());
            Date startTime = historyService.createHistoricActivityInstanceQuery()
                    .processInstanceId(task.getProcessInstanceId())
                    .orderByHistoricActivityInstanceEndTime().desc().list().get(0).getEndTime();
            HisBpmMap.put("startTime",DateUtils.formatDate(startTime,DateUtils.DATE_FMT_DATETIME));
            HisBpmMap.put("endTime", DateUtils.getDate(DateUtils.DATE_FMT_DATETIME));
            HisBpmMap.put("auditorId", userId);
            HisBpmMap.put("auditorName", userName);
            HisBpmMap.put("messageText", mapParam.get("comment"));
            HisBpmMap.put("fileUrls", mapParam.get("fileUrls"));
            HisBpmMap.put("auditResult", "0".equals(mapParam.get("approve"))?"审核拒绝":"审核通过");
            addHisbpm(HisBpmMap);
        }
        return result;
    }


}
