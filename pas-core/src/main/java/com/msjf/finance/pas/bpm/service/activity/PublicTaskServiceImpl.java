package com.msjf.finance.pas.bpm.service.activity;

import com.msjf.finance.pas.bpm.common.ParametersConstant;
import com.msjf.finance.pas.bpm.dao.mapper.CustProStateDao;
import com.msjf.finance.pas.bpm.dao.mapper.PasHisProcessinstanceDao;
import com.msjf.finance.pas.bpm.dao.mapper.PasProTodoDao;
import com.msjf.finance.pas.bpm.service.PublicTaskService;
import com.msjf.finance.pas.common.DateUtils;
import com.msjf.finance.pas.common.StringUtil;
import com.msjf.finance.pas.common.VerificationUtil;
import com.msjf.finance.pas.common.WorkflowUtils;
import com.msjf.finance.pas.common.response.Response;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.UserTask;
import org.activiti.engine.*;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by 成俊平 on 2018/12/28.
 */

@Service("publicTaskService")
@Transactional(propagation=Propagation.REQUIRED)
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


    @Resource
    CustProStateDao custProStateDao;
    @Resource
    PasHisProcessinstanceDao hisProcessinstanceDao;
    @Resource
    PasProTodoDao proTodoDao;


    @Override
    public void createFlow(Map<String, Object> mapParam, Response result) {
        checkCreateFlowParam(mapParam, result);
        /*if(result.getCode().equals("0")){
            return;
        }*/

        String processDefinitionId =  (String)mapParam.get("processDefinitionId");
        String custName = (String)mapParam.get("custName");
        String custNo = (String)mapParam.get("custNo");
        String userId =(String)mapParam.get("userId");
        String userName =(String)mapParam.get("userName");
        Map<String, Object> resultHashMap = new HashMap<>();

        //加入一些key以fp_前缀的K-V,流程引擎会保存
        mapParam.forEach((key,value)->{
            if(key.startsWith("fp_")){
                resultHashMap.put(key,value);
            }
        });
        Map<String, String> submitFormProperties = new HashMap<String, String>();

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
        addHisbpm(HisBpmMap);
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
}
