package com.msjf.finance.pas.bpm.service.activity;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.msjf.finance.msjf.core.response.Response;
import com.msjf.finance.pas.bpm.common.ParametersConstant;
import com.msjf.finance.pas.bpm.service.ProcessInstanceService;
import com.msjf.finance.pas.common.StringUtil;
import com.msjf.finance.pas.common.WorkflowUtils;
import org.activiti.engine.*;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.impl.persistence.entity.IdentityLinkEntity;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.ProcessInstance;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import static com.msjf.finance.pas.bpm.common.ParametersConstant.PDID;
import static com.msjf.finance.pas.bpm.common.ParametersConstant.USER_ID;
import static com.msjf.finance.pas.bpm.common.ParametersConstant.USER_IDS;

/**
 * Created by Jsan on 2019/1/3.
 */
@Service("processInstanceService")
public class ProcessInstanceServiceImpl implements ProcessInstanceService {

    protected Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private RepositoryService repositoryService;

    @Autowired
    private RuntimeService runtimeService;



    @Autowired
    private IdentityService identityService;

    @Autowired
    private FormService formService;

    @Autowired
    private HistoryService historyService;

    @Autowired
    private TaskService taskService;

    @Autowired
    private ManagementService managementService;

    @Override
    public void resourceReadByProcessInstanceId(Map<String, Object> mapParams, Response rs) {
        String processInstanceId = (String) mapParams.get("pid");
        String resourceType = (String) mapParams.get("type");
        if (logger.isDebugEnabled()) {
            logger.debug("resourceReadByProcessInstanceId : {}, {}", processInstanceId, resourceType);
        }

        ProcessInstance processInstance = runtimeService.createProcessInstanceQuery()
                .processInstanceId(processInstanceId).singleResult();
        if (processInstance == null) {
            rs.fail("0","找不到流程实例");
            return;
        }
        ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery()
                .processDefinitionId(processInstance.getProcessDefinitionId()).singleResult();
        if (processDefinition == null) {
            rs.fail("0","找不到流程定义");
            return;
        }
        getResource(processDefinition, resourceType, rs);
    }


    private void getResource(ProcessDefinition processDefinition, String resourceType, Response rs) {

        String resourceName = "";
        StringBuilder base64Str = new StringBuilder();
        if (resourceType.equals("image")) {
            resourceName = processDefinition.getDiagramResourceName();
            base64Str.append("data:image/png;base64,");
        } else if (resourceType.equals("xml")) {
            resourceName = processDefinition.getResourceName();
        }
        InputStream resourceAsStream = repositoryService.getResourceAsStream(processDefinition.getDeploymentId(),
                resourceName);

        try {
            byte[] data = new byte[resourceAsStream.available()];
            resourceAsStream.read(data);
            base64Str.append(Base64.encodeBase64String(data));

            rs.success("1","获取成功",base64Str);
        } catch (IOException e) {
            logger.error("IOException", e);
            rs.fail("0","转换出错");

        }
    }

    @Override
    public void updateProcessDefinitionState(Map<String, Object> mapParams, Response rs) {

    }

    @Override
    public void updateProcessInstanceState(Map<String, Object> mapParams, Response rs) {

    }

    @Override
    public void traceProcess(Map<String, Object> mapParams, Response rs) {

    }

    @Override
    public void submitStartFormAndStartProcessInstance(Map<String, Object> mapParams, Response rs) {
        String processDefinitionId = (String) mapParams.get(PDID);

      /*  BpmnModel bpmnModel = repositoryService.getBpmnModel(processDefinitionId);
        List<UserTask> userTaskList = WorkflowUtils.getOrderUserTask(bpmnModel);

        if(VerificationUtil.isEmpty(userTaskList)){
            throw new RuntimeException("流程模型异常，找不到第一个模型节点!");
        }*/
        submitStartFormAndStartProcessSingleInstance(mapParams,rs);

    }

    public void submitStartFormAndStartProcessSingleInstance(Map<String, Object> mapParams, Response rs) {
        String userId = (String) mapParams.get(USER_ID);
        Object userIds = mapParams.get(USER_IDS);
        String processDefinitionId = (String) mapParams.get(PDID);

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
				/* 获取下一节点
				List<Task> nextTasks = taskService.createTaskQuery().processInstanceId(processInstance.getId()).list();
				if (nextTasks != null && nextTasks.size() > 0) {
					if (userIds instanceof String && StringUtils.isNotBlank(userIds.toString())) {
						JSONObject userJson = (JSONObject) JSON.parse(userIds.toString());
						addCandidateUserOrClaimWithUserIds(nextTasks, userJson);
					} else if (userIds instanceof Map) {
						addCandidateUserOrClaimWithUserIds(nextTasks, (Map) userIds);
					}
				}*/

                ExecutionEntity executionEntity = (ExecutionEntity) processInstance;
                String identUserIds = "";//发起人加审核人
                String idUserIds = "";//审核人
                for(int i=0;i<executionEntity.getIdentityLinks().size();i++){
                    IdentityLinkEntity identityLinkEntity = executionEntity.getIdentityLinks().get(i);
                    if(!StringUtil.isNull(identityLinkEntity.getUserId())){
                        identUserIds = identUserIds +identityLinkEntity.getUserId()+",";
                        if(!identityLinkEntity.getType().equals("starter")){
                            idUserIds = idUserIds +identityLinkEntity.getUserId()+",";
                        }
                    }
                }
                HashMap<String,Object> paraMap = new HashMap<String,Object>();
                //paraMap.put("processInstanceId",executionEntity.);
                //List<HashMap<String, Object>> userList = DaoUtil.commonPersistance.query("",paraMap);
                JSONObject object = new JSONObject();
                object.put("processInstanceId", executionEntity.getProcessInstanceId());
                object.put("identUserIds", identUserIds.substring(0,identUserIds.length()-1));
                object.put("idUserIds", idUserIds.substring(0,idUserIds.length()-1));
                object.put("activityId", executionEntity.getActivityId());
                object.put("activityName", executionEntity.getCurrentActivityName());

                JSONArray array = new JSONArray();
                array.add(object);
                rs.success("1","流程实例启动成功",array);
            } else {
                rs.fail("0","流程实例启动失败");
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
            variables.put(ParametersConstant.USER_IDS,userJson);
        } else if (userIds instanceof Map) {
            variables.put(ParametersConstant.USER_IDS,userIds);
        }
    }

    @Override
    public void startWorkflow(Map<String, Object> mapParams, Response rs) {

    }

    @Override
    public void listRunning(Map<String, Object> mapParams, Response rs) {

    }

    @Override
    public void listRunningByPage(Map<String, Object> mapParams, Response rs) {

    }

    @Override
    public void myProcessInstance(Map<String, Object> mapParams, Response rs) {

    }

    @Override
    public void involvedList(Map<String, Object> mapParams, Response rs) {

    }

    @Override
    public void getVariables(Map<String, Object> mapParams, Response rs) {

    }

    @Override
    public void getComments(Map<String, Object> mapParams, Response rs) {

    }

    @Override
    public void findStartForm(Map<String, Object> mapParams, Response rs) {

    }

    @Override
    public void listInstance(Map<String, Object> mapParams, Response rs) {

    }

    @Override
    public void listInstanceContainSomeone(Map<String, Object> params, Response rs) {

    }

    @Override
    public void isInstanceCompleted(Map<String, Object> mapParams, Response rs) {

    }
}
