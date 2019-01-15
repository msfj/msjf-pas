package com.msjf.finance.pas.bpm.service.activity;


import com.msjf.finance.pas.bpm.dao.mapper.CustProStateDao;
import com.msjf.finance.pas.bpm.dao.mapper.PasHisProcessinstanceDao;
import com.msjf.finance.pas.bpm.dao.mapper.PasProTodoDao;
import com.msjf.finance.pas.bpm.service.KbpmTaskService;
import com.msjf.finance.pas.bpm.service.ProcessDefinitionService;
import com.msjf.finance.pas.bpm.service.ProcessInstanceService;
import com.msjf.finance.pas.bpm.service.PublicTaskService;
import com.msjf.finance.pas.common.*;
import com.msjf.finance.pas.common.response.Response;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.UserTask;
import org.activiti.engine.*;
import org.activiti.engine.task.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import static com.msjf.finance.pas.bpm.common.ParametersConstant.PDID;
import static com.msjf.finance.pas.common.VerificationUtil.valueOf;

/**
 * Created by 成俊平 on 2018/12/28.
 */

@Service("publicTaskService")
@Transactional
public class PublicTaskServiceImpl implements PublicTaskService {

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
    public void createFlow(Map<String, Object> mapParam, Response result) throws RuntimeException {
        checkCreateFlowParam(mapParam, result);
        /*if(result.getCode().equals("0")){
            return;
        }*/
// 插入申请流程
        Map<String, Object> resultHashMap = commitFormData(mapParam,
                result);
        String processDefinitionId =  (String)mapParam.get("processDefinitionId");
        String custName = (String)mapParam.get("custName");
        String custNo = (String)mapParam.get("custNo");
        String userId =(String)mapParam.get("userId");
        String userName =(String)mapParam.get("userName");
        String taskId =(String)mapParam.get("taskId");
        Task task = taskService.createTaskQuery().includeProcessVariables().taskId(taskId).singleResult();

        //加入一些key以fp_前缀的K-V,流程引擎会保存
        mapParam.forEach((key,value)->{
            if(key.startsWith("fp_")){
                resultHashMap.put(key,value);
            }
        });
        // 发起流程
        startFlow(mapParam, result);

       /* Map<String, String> submitFormProperties = new HashMap<String, String>();

        // 从mapParams中读取参数然后转换
        // 所有以fp_开始的参数都复制到formPraperties中，用于保存到用户任务中
        mapParam.forEach((key,value) -> {
            if(key.startsWith(ParametersConstant.FORM_PRAPERTIES_SUFFIX)){
                submitFormProperties.put(key.replaceFirst(ParametersConstant.FORM_PRAPERTIES_SUFFIX, ""),
                        value != null ? value.toString() : null);
            }
        });
        // 用来设置启动流程的人员ID，引擎会自动把用户ID保存到activiti:initiator中
        identityService.setAuthenticatedUserId(userId);
        // 查询流程启动参数，处理内置参数
        WorkflowUtils.setAndAddTaskFormData(formService.getStartFormData(processDefinitionId)
                ,submitFormProperties);
        BpmnModel bpmnModel = repositoryService.getBpmnModel(processDefinitionId);
        List<UserTask> userTaskList = WorkflowUtils.getOrderUserTask(bpmnModel);
        UserTask userTask = new UserTask();
        if(userTaskList != null && userTaskList.size() >= 1){
            userTask = userTaskList.get(0);
            if(userTask!=null){
                String assigneeListCollectionName = userTask.getId() + ParametersConstant.ASSIGNEE_LIST_SUFFIX;
                mapParam.put(assigneeListCollectionName,mapParam.get("assigneeList"));
            }
        }else{
            throw new RuntimeException("流程模型异常，找不到第一个模型节点!");
        }

        // 启动流程
        ProcessInstance procIns = runtimeService.startProcessInstanceById(processDefinitionId,mapParam);
        String proDefKeyName = bpmnModel.getProcesses().get(0).getName();
        List<Task> taskList = taskService.createTaskQuery().processInstanceId(procIns.getProcessInstanceId()).list();
        List<Map> todoList = new ArrayList<>();
        for(int i=0;i<taskList.size();i++){
            Map<String,Object> taskMap = new HashMap<>();
            Task task = taskList.get(i);
            String assignee = task.getAssignee();
            String[] assignees = assignee.split("\\|");
            String assigeneeid = assignees[0];
            String assigeneeName = assignees[1];
            String taskId = task.getId();
            taskMap.put("proInstance",procIns.getProcessInstanceId());
            taskMap.put("actId",userTask.getId());
            taskMap.put("actName",userTask.getName());
            taskMap.put("proDefKey",processDefinitionId);
            taskMap.put("proDefName",proDefKeyName);
            taskMap.put("auditorId",assigeneeid);
            taskMap.put("auditorName",assigeneeName);
            taskMap.put("taskId",taskId);
            todoList.add(taskMap);
        }
        Map<String,Object> todoMap = new HashMap<>();
        todoMap.put("list",todoList);
        addTodo(todoMap);

        Map<String,Object> custStateMap = new HashMap<>();
        custStateMap.put("Id", StringUtil.getUUID());
        custStateMap.put("custName", custName);
        custStateMap.put("custNo", custNo);
        custStateMap.put("proInstance", procIns.getProcessInstanceId());
        custStateMap.put("proDefKey", processDefinitionId);
        custStateMap.put("proDefName", proDefKeyName);
        custStateMap.put("startTime", DateUtils.getDate(DateUtils.DATE_FMT_DATETIME));
        custStateMap.put("proSate", "0");
        updCustState(custStateMap);

        Map<String,Object> HisBpmMap = new HashMap<>();
        HisBpmMap.put("Id", StringUtil.getUUID());
        HisBpmMap.put("custName", custName);
        HisBpmMap.put("custNo", custNo);
        HisBpmMap.put("proInstance", procIns.getProcessInstanceId());
        HisBpmMap.put("proDefKey", processDefinitionId);
        HisBpmMap.put("proDefName", proDefKeyName);
        HisBpmMap.put("endTime", DateUtils.getDate(DateUtils.DATE_FMT_DATETIME));
        HisBpmMap.put("auditorId", userId);
        HisBpmMap.put("auditorId", userName);
        HisBpmMap.put("auditResult", "发起流程");
        addHisbpm(HisBpmMap);*/
    }

    @Override
    public void executeNextStep(HashMap<String, Object> mapParam, Response rs) throws Exception {
        if (VerificationUtil.isNull(mapParam.get("processInstanceId"))) {
            rs.fail("0","processInstanceId不能为空");
            return;
        }
        if (VerificationUtil.isNull(mapParam.get("taskId"))) {
            rs.fail("0","taskId");
            return;
        }
        Map<String,Object> taskParaMap = new HashMap<>();
        taskParaMap.put("procInsId",mapParam.get("processInstanceId"));
        taskParaMap.put("customerno",mapParam.get("customerno"));

        taskSubmitForm(mapParam, rs);
    }

    /**
     * 验证发起流程参数
     *
     * @param mapParam
     * @param rs
     */
    public void checkCreateFlowParam(Map<String, Object> mapParam,
                                     Response rs) {
        if (VerificationUtil.isNull("formParam")) {
            rs.fail("0","表单参数不存在!");
            return;
        }
        if (VerificationUtil.isNull(mapParam.get("processDefinitionId"))) {
            rs.fail("0","流程定义Id不存在!");
            return;
        }
        if (VerificationUtil.isNull(mapParam.get("custName"))) {
            rs.fail("0","企业名称不存在!");
            return;
        }
        if (VerificationUtil.isNull(mapParam.get("custNo"))) {
            rs.fail("0","企业客户号不存在!");
            return;
        }
        if (VerificationUtil.isNull(mapParam.get("userId"))) {
            rs.fail("0","当前用户id不存在!");
            return;
        }
        if (VerificationUtil.isNull(mapParam.get("userName"))) {
            rs.fail("0","当前用户名称不存在!");
            return;
        }
        rs.success("检验成功!");
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
        }
    }

    /**
     * 提交表单数据
     *
     * @param mapParam
     * @param result
     */
    public Map<String, Object> commitFormData(
            Map<String, Object> mapParam,
            Response result) throws RuntimeException {

        HashMap<String, Object> formParamHashMap = new HashMap<>();
        formParamHashMap.put("koauth2EmployeeId", mapParam.get("koauth2EmployeeId"));
        formParamHashMap.put("customerno", mapParam.get("customerno"));
        formParamHashMap.put("tablename", mapParam.get("tablename"));
        String bexno = valueOf(formParamHashMap.get("applytype"));
        String pageid = valueOf(formParamHashMap.get("pageid"));
        Map<String, Object> resultHashMap = new HashMap<String, Object>();
        if(VerificationUtil.isEmpty(pageid)){

            resultHashMap.put("bexno", bexno);
            resultHashMap.put("title", formParamHashMap.get("title"));
            resultHashMap.put("applyid", formParamHashMap.get("applyid"));
            resultHashMap.put("tablename", formParamHashMap.get("tablename"));
            return resultHashMap;
        }
        mapParam.put("bexno", bexno);
        // PublicBusinessBean publicBusinessBean = new PublicBusinessBean();
        // publicBusinessBean.addPublicApply(formParamHashMap, result);
        String startFlowEvent = getStartFlowEvent(mapParam, result);
        /*KesbBexUtil
                .wsContextDoBex(formParamHashMap, startFlowEvent, result);
        List<Map<String, Object>> resultList = ResultUtil.getResult(result);
        Map<String, Object> resultHashMap = resultList.get(0);
        resultHashMap.put("bexno", bexno);*/

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
            processDefinitionService.findStartForm(paramHashMap,result);

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
     * @param result
     */
    public void startFlow(Map<String, Object> resultHashMap, Response result) {
        Map<String, Object> startFlowParamHashMap = resultHashMap;
        startFlowParamHashMap.put("fp_title", resultHashMap.get("title"));
        startFlowParamHashMap.put("fp_applyid", resultHashMap.get("applyid"));
        startFlowParamHashMap.put("fp_kgrpTaskId",resultHashMap.get("taskId"));
        startFlowParamHashMap.put("fp_configId",resultHashMap.get("configId"));
        startFlowParamHashMap.put("fp_departmentId",resultHashMap.get("departmentId"));
        startFlowParamHashMap.put("fp_isCreate","1");
        startFlowParamHashMap.put("fp_customParam",resultHashMap.get("customParam"));
        startFlowParamHashMap.put("userIds", resultHashMap.get("userIds"));
        startFlowParamHashMap.put("wsuser",
                resultHashMap.get("koauth2EmployeeId"));
        startFlowParamHashMap.put("processDefinitionId",
                resultHashMap.get("processDefinitionId"));
        submitStartFormAndStartProcessInstance(startFlowParamHashMap,result);
    }


    public void submitStartFormAndStartProcessInstance(Map<String, Object> mapParams, Response rs) {
        String processDefinitionId =  (String)mapParams.get("processDefinitionId");
        BpmnModel bpmnModel = repositoryService.getBpmnModel(processDefinitionId);
        List<UserTask> userTaskList = WorkflowUtils.getOrderUserTask(bpmnModel);

        if(VerificationUtil.isEmpty(userTaskList)){
            throw new RuntimeException("流程模型异常，找不到第一个模型节点!");
        }
      processInstanceService.submitStartFormAndStartProcessInstance(mapParams,rs);

    }



    /**
     * 提交下一步事件
     *
     * @param mapParam
     * @param result
     */
    public void taskSubmitForm(HashMap<String, Object> mapParam, Response result) throws Exception {
        HashMap<String, Object> nextStepHashMap = new HashMap<String, Object>();
        // 当前审核人
        nextStepHashMap.put("koauth2EmployeeId", mapParam.get("koauth2EmployeeId"));
        nextStepHashMap.put("taskId", mapParam.get("taskId"));
        nextStepHashMap.put("approve", mapParam.get("approve"));
        // 设置审核结果
        nextStepHashMap.put("fp_" + mapParam.get("taskDefinitionKey")
                + "_approve", mapParam.get("approve"));
        nextStepHashMap.put("fp_lastStep", mapParam.get("taskDefinitionKey"));
        nextStepHashMap.put("fp_" + mapParam.get("taskDefinitionKey")
                + "_auditinfo", mapParam.get("auditinfo"));
        nextStepHashMap.put("fp_kgrpTaskId",mapParam.get("kgrpTaskId"));
        nextStepHashMap.put("fp_departmentId",mapParam.get("departmentId"));
        nextStepHashMap.put("fp_processInstanceId",mapParam.get("processInstanceId"));
        nextStepHashMap.put("fp_isCreate","0");
        //加入一些key以fp_前缀的K-V,流程引擎会保存
        mapParam.forEach((key,value)->{
            if(key.startsWith("fp_")){
                nextStepHashMap.put(key,value);
            }
        });

        // 设置审核意见
        nextStepHashMap.put("comment", mapParam.get("comment"));
        // 跳转节点
        kbpmTaskService.submitTaskFormData(nextStepHashMap,result);
    }


}
