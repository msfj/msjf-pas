package com.msjf.finance.pas.bpm.service.activity;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.msjf.finance.msjf.core.response.Response;
import com.msjf.finance.pas.bpm.dao.mapper.DeploymentQueryMapper;
import com.msjf.finance.pas.bpm.entity.DeploymentEntity;
import com.msjf.finance.pas.bpm.service.KbpmTaskService;
import com.msjf.finance.pas.bpm.service.ProcessDefinitionService;
import com.msjf.finance.pas.common.StringUtil;
import org.activiti.bpmn.converter.BpmnXMLConverter;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.FlowElement;
import org.activiti.editor.constants.ModelDataJsonConstants;
import org.activiti.editor.language.json.converter.BpmnJsonConverter;
import org.activiti.engine.*;
import org.activiti.engine.form.FormProperty;
import org.activiti.engine.history.HistoricActivityInstance;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.impl.RepositoryServiceImpl;
import org.activiti.engine.impl.bpmn.behavior.BoundaryEventActivityBehavior;
import org.activiti.engine.impl.bpmn.behavior.CallActivityBehavior;
import org.activiti.engine.impl.bpmn.parser.BpmnParse;
import org.activiti.engine.impl.bpmn.parser.ErrorEventDefinition;
import org.activiti.engine.impl.bpmn.parser.EventSubscriptionDeclaration;
import org.activiti.engine.impl.cmd.AbstractCustomSqlExecution;
import org.activiti.engine.impl.cmd.CustomSqlExecution;
import org.activiti.engine.impl.form.DefaultFormHandler;
import org.activiti.engine.impl.form.FormPropertyHandler;
import org.activiti.engine.impl.form.StartFormDataImpl;
import org.activiti.engine.impl.jobexecutor.TimerDeclarationImpl;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.activiti.engine.impl.pvm.PvmTransition;
import org.activiti.engine.impl.pvm.delegate.ActivityBehavior;
import org.activiti.engine.impl.pvm.process.*;
import org.activiti.engine.impl.task.TaskDefinition;
import org.activiti.engine.repository.Model;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.repository.ProcessDefinitionQuery;
import org.activiti.engine.runtime.Execution;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.explorer.util.XmlUtil;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.*;

import static com.msjf.finance.pas.bpm.common.ParametersConstant.*;

/**
 * Created by chengjunping on 2018/12/27.
 */
@Service("ProcessDefinitionService")
public class ProcessDefinitionServiceImpl implements ProcessDefinitionService {

    protected Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private RepositoryService repositoryService;

    @Autowired
    private FormService formService;

    @Autowired
    private RuntimeService runtimeService;

    @Autowired
    private KbpmTaskService kbpmTaskService;

    @Autowired
    private ManagementService managementService;

    @Autowired
    private HistoryService historyService;



    @Override
    public Response convertToModel(Map<String, Object> mapParams) {

        String processDefinitionId = (String) mapParams.get("processDefinitionId");
        if (logger.isDebugEnabled()) {
            logger.debug("convertToModel : {}", processDefinitionId);
        }

        ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery()
                .processDefinitionId(processDefinitionId).singleResult();
        if (processDefinition == null) {
            return new Response().fail("0","流程定义未找到");
        }
        InputStream bpmnStream = repositoryService.getResourceAsStream(processDefinition.getDeploymentId(),
                processDefinition.getResourceName());

        //XMLInputFactory xif = XMLInputFactory.newInstance();
        XMLInputFactory xif = XmlUtil.createSafeXmlInputFactory();

        try {

            InputStreamReader in = new InputStreamReader(bpmnStream, "UTF-8");
            XMLStreamReader xtr = xif.createXMLStreamReader(in);

            BpmnModel bpmnModel = new BpmnXMLConverter().convertToBpmnModel(xtr);
            BpmnJsonConverter converter = new BpmnJsonConverter();
            com.fasterxml.jackson.databind.node.ObjectNode modelNode = converter.convertToJson(bpmnModel);
            Model modelData = repositoryService.newModel();
            modelData.setKey(processDefinition.getKey());
            modelData.setName(processDefinition.getResourceName());
            modelData.setCategory(processDefinition.getDeploymentId());

            ObjectNode modelObjectNode = new ObjectMapper().createObjectNode();
            modelObjectNode.put(ModelDataJsonConstants.MODEL_NAME, processDefinition.getName());
            modelObjectNode.put(ModelDataJsonConstants.MODEL_REVISION, 1);
            modelObjectNode.put(ModelDataJsonConstants.MODEL_DESCRIPTION, processDefinition.getDescription());
            modelData.setMetaInfo(modelObjectNode.toString());

            repositoryService.saveModel(modelData);

            repositoryService.addModelEditorSource(modelData.getId(), modelNode.toString().getBytes("utf-8"));
            JSONObject object = (JSONObject) JSON.toJSON(modelData);
            return new Response().success("1","转换成功",object);
        } catch (UnsupportedEncodingException e) {
            logger.error("UnsupportedEncodingException : {}", e);
            throw new RuntimeException(e);
        } catch (XMLStreamException e) {
            logger.error("XMLStreamException : {}", e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public Response listProcessDefinitionByState(Map<String, Object> mapParams) {
        String state = (String) mapParams.get("state");
        String lastVersion = String.valueOf(mapParams.get("lastVersion"));

        if (logger.isDebugEnabled()) {
            logger.debug("listByState : state->{}", state);
        }

        JSONArray objects = new JSONArray();
        ProcessDefinitionQuery processDefinitionQuery = repositoryService.createProcessDefinitionQuery()
                .orderByDeploymentId().desc();
        if (StringUtils.isNotBlank(state)) {
            if ("active".equals(state)) {
                processDefinitionQuery.active();
            } else if ("suspended".equals(state)) {
                processDefinitionQuery.suspended();
            }
        }
        if ("true".equalsIgnoreCase(lastVersion)) {
            processDefinitionQuery.latestVersion();
        }
        List<ProcessDefinition> processDefinitionList = processDefinitionQuery.list();
        if (null != processDefinitionList) {
            for (ProcessDefinition processDefinition : processDefinitionList) {
                JSONObject object1 = new JSONObject();
                object1.put("id", processDefinition.getId());
                object1.put("key", processDefinition.getKey());
                object1.put("name", processDefinition.getName());
                object1.put("resourceName", processDefinition.getResourceName());
                object1.put("tenantId", processDefinition.getTenantId());
                object1.put("version", processDefinition.getVersion());
                object1.put("category", processDefinition.getCategory());
                object1.put("deploymentId", processDefinition.getDeploymentId());
                object1.put("description", processDefinition.getDescription());
                object1.put("diagramResourceName", processDefinition.getDiagramResourceName());
                object1.put("graphicalNotationDefined",
                        ((ProcessDefinitionEntity) processDefinition).isGraphicalNotationDefined());

                objects.add(object1);
            }
        }
        return new Response().success("1","查询成功",objects);
    }

    @Override
    public Response listProcessDefinitionByStateAndPage(Map<String, Object> mapParams) {
        String state = (String) mapParams.get("state");
        int pageSize = (Integer) mapParams.get(PAGE_SIZE);
        int pageNumber = (Integer) mapParams.get(PAGE_NUMBER);
        String lastVersion = String.valueOf(mapParams.get("lastVersion"));
        int firstResult = pageSize * (pageNumber - 1);

        if (logger.isDebugEnabled()) {
            logger.debug("listByStateAndPage : state->{}, pageSize->{}, pageNumber->{}", state, pageSize, pageNumber);
        }

        JSONArray objects = new JSONArray();
        ProcessDefinitionQuery processDefinitionQuery = repositoryService.createProcessDefinitionQuery()
                .orderByDeploymentId().desc();
        if (StringUtils.isNotBlank(state)) {
            if ("active".equals(state)) {
                processDefinitionQuery.active();
            } else if ("suspended".equals(state)) {
                processDefinitionQuery.suspended();
            }
        }
        if ("true".equalsIgnoreCase(lastVersion)) {
            processDefinitionQuery.latestVersion();
        }
        List<ProcessDefinition> processDefinitionList = processDefinitionQuery.listPage(firstResult, pageSize);
        //rs.setLengths((int) processDefinitionQuery.count());
        if (null != processDefinitionList) {
            for (ProcessDefinition processDefinition : processDefinitionList) {
                JSONObject object1 = new JSONObject();
                object1.put("id", processDefinition.getId());
                object1.put("key", processDefinition.getKey());
                object1.put("name", processDefinition.getName());
                object1.put("resourceName", processDefinition.getResourceName());
                object1.put("tenantId", processDefinition.getTenantId());
                object1.put("version", processDefinition.getVersion());
                object1.put("category", processDefinition.getCategory());
                object1.put("deploymentId", processDefinition.getDeploymentId());
                object1.put("description", processDefinition.getDescription());
                object1.put("diagramResourceName", processDefinition.getDiagramResourceName());
                object1.put("graphicalNotationDefined",
                        ((ProcessDefinitionEntity) processDefinition).isGraphicalNotationDefined());

                objects.add(object1);
            }
        }
        return  new Response().success("1","查询成功",objects);
    }

    @Override
    public Response listProcessDeploymentByState(Map<String, Object> mapParams) {
        String processDefinitionId = (String) mapParams.get("processDefinitionId");
        String resourceType = (String) mapParams.get("type");
        if (logger.isDebugEnabled()) {
            logger.debug("resourceReadByProcessDefinitionId : {}, {}", processDefinitionId, resourceType);
        }
        ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery()
                .processDefinitionId(processDefinitionId).singleResult();
        if (processDefinition == null) {
            return new Response().fail("0","找不到流程定义");
        }
        getResource(processDefinition, resourceType);
        if (resourceType.equals("image")) {
            Map modelMap = new HashMap(2);
            modelMap.put("name",processDefinition.getName());
            List<Map> modelList = null;
                    //DaoUtil.commonPersistance.query("get_kbpm_moderId_by_prouuId", modelMap);

            if(modelList!=null&&modelList.size()>0){
                String modelId = (String) modelList.get(0).get("modelId");
                byte[] editorSourceExtra = repositoryService.getModelEditorSourceExtra(modelId);

                if (editorSourceExtra != null) {
                    String base64Str = "data:image/png;base64," + Base64.encodeBase64String(editorSourceExtra);
                    return new Response().success("1","获取成功",base64Str);
                   /* rs.setResult(base64Str);
                    rs.setResType(Constant.WS_TYPE_STRING);*/
                }
                else {
                   return new Response().fail("0","没有找到图片");
                }
            }else{
               return getResource(processDefinition, resourceType);
            }
        }else if (resourceType.equals("xml")) {
            return getResource(processDefinition, resourceType);
        }
        return null;
    }

    private Response getResource(ProcessDefinition processDefinition, String resourceType) {

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

            return new Response().success("1","获取成功",base64Str);
        } catch (IOException e) {
            logger.error("IOException", e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public Response listProcessDeploymentByStateAndPage(Map<String, Object> mapParams) {
        final String state = (String) mapParams.get("state");
        final String lastVersion = String.valueOf(mapParams.get("lastVersion"));
        final int pageSize = (Integer) mapParams.get(PAGE_SIZE);
        int pageNumber = (Integer) mapParams.get(PAGE_NUMBER);
        final int firstResult = pageSize * (pageNumber - 1);
        String reName = (String) mapParams.get("name");

        if (logger.isDebugEnabled()) {
            logger.debug("listByStateAndPage : state->{}, pageSize->{}, pageNumber->{}", state, pageSize, pageNumber);
        }

        List<Object[]> objects = new ArrayList<Object[]>();

        CustomSqlExecution<DeploymentQueryMapper, List<DeploymentEntity>> customSqlExecution = new AbstractCustomSqlExecution<DeploymentQueryMapper, List<DeploymentEntity>>(
                DeploymentQueryMapper.class) {
            @Override
            public List<DeploymentEntity> execute(DeploymentQueryMapper customMapper) {
                List<DeploymentEntity> deployments = null;
                if (StringUtils.isNotBlank(state)) {
                    if ("active".equals(state)) {
                        if ("true".equalsIgnoreCase(lastVersion)) {
                            deployments = customMapper.findDeploymentsBySteteWithLastVersion(1, firstResult, pageSize);
                        } else {
                            deployments = customMapper.findDeploymentsByStete(1, firstResult, pageSize);
                        }
                    } else if ("suspended".equals(state)) {
                        if ("true".equalsIgnoreCase(lastVersion)) {
                            deployments = customMapper.findDeploymentsBySteteWithLastVersion(0, firstResult, pageSize);
                        } else {
                            deployments = customMapper.findDeploymentsByStete(0, firstResult, pageSize);
                        }
                    }
                } else {
                    if ("true".equalsIgnoreCase(lastVersion)) {
                        deployments = customMapper.findDeploymentsWithLastVersion(firstResult, pageSize);
                    } else {
                        deployments = customMapper.findDeployments(firstResult, pageSize);
                    }
                }
                System.out.println(deployments.toString());
                return deployments;
            }
        };
        List<DeploymentEntity> deployments = managementService.executeCustomSql(customSqlExecution);

        CustomSqlExecution<DeploymentQueryMapper, Long> customCountSqlExecution = new AbstractCustomSqlExecution<DeploymentQueryMapper, Long>(
                DeploymentQueryMapper.class) {
            @Override
            public Long execute(DeploymentQueryMapper customMapper) {
                Long counts = null;
                if (StringUtils.isNotBlank(state)) {
                    if ("active".equals(state)) {
                        if ("true".equalsIgnoreCase(lastVersion)) {
                            counts = customMapper.countDeploymentsBySteteWithLastVersion(1);
                        } else {
                            counts = customMapper.countDeploymentsByStete(1);
                        }
                    } else if ("suspended".equals(state)) {
                        if ("true".equalsIgnoreCase(lastVersion)) {
                            counts = customMapper.countDeploymentsBySteteWithLastVersion(0);
                        } else {
                            counts = customMapper.countDeploymentsByStete(0);
                        }
                    }
                } else {
                    if ("true".equalsIgnoreCase(lastVersion)) {
                        counts = customMapper.countDeploymentsWithLastVersion();
                    } else {
                        counts = customMapper.countDeployments();
                    }
                }
                return counts;
            }
        };
        Long counts = managementService.executeCustomSql(customCountSqlExecution);

        if (null != deployments) {
            //rs.setLengths(counts.intValue());
            for (DeploymentEntity entity : deployments) {

                JSONObject object1 = new JSONObject();
                JSONObject object2 = new JSONObject();
                if (StringUtils.isNotBlank(reName)) {
                    if (entity.getName().contains(reName)) {
                        object1.put("id", entity.getId());
                        object1.put("key", entity.getKey());
                        object1.put("name", entity.getName());
                        object1.put("resourceName", entity.getResourceName());
                        object1.put("tenantId", entity.getTenantId());
                        object1.put("version", entity.getVersion());
                        object1.put("category", entity.getCategory());
                        object1.put("deploymentId", entity.getDeploymentId());
                        object1.put("description", entity.getDescription());
                        object1.put("diagramResourceName", entity.getDiagramResourceName());
                        object1.put("graphicalNotationDefined", entity.getGraphicalNotationDefined());


                        object2.put("id", entity.getDeploymentId());
                        object2.put("name", entity.getName());
                        object2.put("tenantId", entity.getTenantId());
                        object2.put("category", entity.getCategory());
                        object2.put("deploymentTime", entity.getDeploymentTime());
                        objects.add(new Object[]{object1, object2});
                    }
                } else {
                    object1.put("id", entity.getId());
                    object1.put("key", entity.getKey());
                    object1.put("name", entity.getName());
                    object1.put("resourceName", entity.getResourceName());
                    object1.put("tenantId", entity.getTenantId());
                    object1.put("version", entity.getVersion());
                    object1.put("category", entity.getCategory());
                    object1.put("deploymentId", entity.getDeploymentId());
                    object1.put("description", entity.getDescription());
                    object1.put("diagramResourceName", entity.getDiagramResourceName());
                    object1.put("graphicalNotationDefined", entity.getGraphicalNotationDefined());


                    object2.put("id", entity.getDeploymentId());
                    object2.put("name", entity.getName());
                    object2.put("tenantId", entity.getTenantId());
                    object2.put("category", entity.getCategory());
                    object2.put("deploymentTime", entity.getDeploymentTime());
                    objects.add(new Object[]{object1, object2});
                }

            }
        }

        JSONArray array = (JSONArray) JSON.toJSON(objects);

        /*rs.setResult(array);
        rs.setResType(Constant.WS_TYPE_FASTJSONARRAY);*/
        return new Response().success("1","查询成功",array);
    }

    @Override
    public Response resourceReadByProcessDefinitionId(Map<String, Object> mapParams) throws Exception {
        String processDefinitionId = (String) mapParams.get("processDefinitionId");
        String resourceType = (String) mapParams.get("type");
        if (logger.isDebugEnabled()) {
            logger.debug("resourceReadByProcessDefinitionId : {}, {}", processDefinitionId, resourceType);
        }
        ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery()
                .processDefinitionId(processDefinitionId).singleResult();
        if (processDefinition == null) {
            return new Response().fail("0","找不到流程定义");
        }
        getResource(processDefinition, resourceType);
        if (resourceType.equals("image")) {
            Map modelMap = new HashMap(2);
            modelMap.put("name",processDefinition.getName());
            List<Map> modelList = null;
                    //DaoUtil.commonPersistance.query("get_kbpm_moderId_by_prouuId", modelMap);

            if(modelList!=null&&modelList.size()>0){
                String modelId = (String) modelList.get(0).get("modelId");
                byte[] editorSourceExtra = repositoryService.getModelEditorSourceExtra(modelId);

                if (editorSourceExtra != null) {
                    String base64Str = "data:image/png;base64," + Base64.encodeBase64String(editorSourceExtra);
                    return new Response().success("1","获取成功",base64Str);
                    /*rs.setResult(base64Str);
                    rs.setResType(Constant.WS_TYPE_STRING);*/
                }
                else {
                    return new Response().fail("0","没有找到图片");
                }
            }else{
               return getResource(processDefinition, resourceType);
            }
        }else if (resourceType.equals("xml")) {
            return getResource(processDefinition, resourceType);
        }
        return null;
    }

    @Override
    public Response findStartForm(Map<String, Object> mapParams) {
        String processDefinitionId = (String) mapParams.get("processDefinitionId");
        if (logger.isDebugEnabled()) {
            logger.debug("findStartForm, processDefinitionId=>{}", processDefinitionId);
        }

        try {
            StartFormDataImpl startFormData = (StartFormDataImpl) formService.getStartFormData(processDefinitionId);
            startFormData.setProcessDefinition(null);

            if (startFormData != null && startFormData.getFormProperties() != null) {
                JSONArray array = new JSONArray();
                List<FormProperty> formProperties = startFormData.getFormProperties();
                for (FormProperty formProperty : formProperties) {
                    JSONObject object = (JSONObject) JSON.toJSON(formProperty);
                    if ("enum".equals(formProperty.getType().getName())) {
                        Map<String, String> values = (Map<String, String>) formProperty.getType()
                                .getInformation("values");
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
                return new Response().success("1","获取表单成功",array);
                /*rs.setResult(array);
                rs.setLengths(array.size());
                rs.setResType(Constant.WS_TYPE_FASTJSONARRAY);
                rs.successful("获取表单成功");*/
            } else {
                return new Response().success("无表单数据");
            }
        } catch (ActivitiObjectNotFoundException e) {
            if (logger.isWarnEnabled()) {
                logger.warn("Activiti object not found exception : processDefinitionId = > {}", processDefinitionId);
            }
            return new Response().fail("0","流程定义不存在");
        } catch (ActivitiException e) {
            if (logger.isWarnEnabled()) {
                logger.warn("Activiti exception : processDefinitionId = > {}", processDefinitionId);
            }
            return new Response().fail("0","没有配置表单");
        }
    }

    @Override
    public Response findProcessDefinitionDetails(Map<String, Object> mapParams) {
        String processDefinitionId = (String) mapParams.get("processDefinitionId");

        ProcessDefinitionEntity processDefinition = (ProcessDefinitionEntity) ((RepositoryServiceImpl) repositoryService)
                .getDeployedProcessDefinition(processDefinitionId);
        if (null != processDefinition) {
            JSONObject object = new JSONObject();
            object.put("id", processDefinition.getId());
            object.put("key", processDefinition.getKey());
            object.put("name", processDefinition.getName());
            object.put("resourceName", processDefinition.getResourceName());
            object.put("tenantId", processDefinition.getTenantId());
            object.put("version", processDefinition.getVersion());
            object.put("category", processDefinition.getCategory());
            object.put("deploymentId", processDefinition.getDeploymentId());
            object.put("description", processDefinition.getDescription());
            object.put("diagramResourceName", processDefinition.getDiagramResourceName());

            object.put("graphicalNotationDefined", processDefinition.isGraphicalNotationDefined());
            JSONObject taskDefinitions = new JSONObject();
            Map<String, TaskDefinition> mapTaskDefinitions = processDefinition.getTaskDefinitions();
            if (mapTaskDefinitions != null) {
                for (Iterator<Map.Entry<String, TaskDefinition>> it = mapTaskDefinitions.entrySet().iterator(); it
                        .hasNext(); ) {
                    JSONObject taskDefinition = new JSONObject();
                    JSONObject taskForm = new JSONObject();
                    //JSONObject formProperty = new JSONObject();
                    Map.Entry<String, TaskDefinition> entry = it.next();
                    TaskDefinition tdf = entry.getValue();
                    DefaultFormHandler defaultFormHandler = (DefaultFormHandler) (tdf.getTaskFormHandler());
                    List<FormPropertyHandler> formPropertyHandlers = defaultFormHandler.getFormPropertyHandlers();
                    JSONArray formArray = new JSONArray();
                    for (FormPropertyHandler formPropertyHandler : formPropertyHandlers) {
                        JSONObject formObject = (JSONObject) JSON.toJSON(formPropertyHandler);
                        if ("enum".equals(formPropertyHandler.getType().getName())) {
                            @SuppressWarnings("unchecked")
                            Map<String, String> values = (Map<String, String>) formPropertyHandler.getType()
                                    .getInformation("values");
                            if (values != null) {
                                formObject.put("values", values);
                            }
                        } else if ("date".equals(formPropertyHandler.getType().getName())) {
                            String datePattern = (String) formPropertyHandler.getType().getInformation("datePattern");
                            if (datePattern != null) {
                                formObject.put("datePattern", datePattern);
                            }
                        }
                        formArray.add(formObject);
                        if (logger.isDebugEnabled()) {
                            logger.debug("Form property : {}", formObject.toJSONString());
                        }
                    }
                    taskForm.put("formProperty", formArray);

                    taskDefinition.put("taskForm", taskForm);

                    taskDefinitions.put(entry.getKey(), taskDefinition);
                }
            }
            object.put("taskDefinitions", taskDefinitions);

            JSONObject variables = new JSONObject();
            Map<String, Object> mapVariables = processDefinition.getVariables();
            if (mapVariables != null) {
                for (Iterator<Map.Entry<String, Object>> it = mapVariables.entrySet().iterator(); it.hasNext(); ) {
                    Map.Entry<String, Object> entry = it.next();
                    variables.put(entry.getKey(), JSON.toJSON(entry.getValue()));
                }
            }
            object.put("variables", variables);

            /*rs.setResult(object);
            rs.setResType(Constant.WS_TYPE_FASTJSONOBJECT);*/
            return new Response().success("1","查询成功",object);
        } else {
            return new Response().fail("0","没有找到流程定义");
        }
    }

    @Override
    public Response findStartUserActivities(Map<String, Object> mapParams) {
        String processDefinitionId = (String) mapParams.get(PDID);
        String conditionValue = (String) mapParams.get("conditionValue");
        String conditionVariable = (String) mapParams.get("conditionVariable");

        ProcessDefinitionEntity def = null;
        try {
            def = (ProcessDefinitionEntity) ((RepositoryServiceImpl) repositoryService)
                    .getDeployedProcessDefinition(processDefinitionId);
        } catch (ActivitiObjectNotFoundException e) {
            return new Response().fail("0","流程不存在");
        }
        if (def == null) {
            return new Response().fail("0","流程不存在");
        }
        ActivityImpl initial = def.getInitial();

        Map<String, Object> map = new HashMap<String, Object>();
        if (StringUtils.isNotBlank(conditionVariable)) {
            map.put(conditionVariable, conditionValue);
        } else {
            map.put(initial.getId() + "_approve", conditionValue);
        }
        List<PvmTransition> transitions = initial.getOutgoingTransitions();
        JSONArray array = kbpmTaskService.nextTaskDefinition(transitions, map);

        return new Response().success("1","获取成功",array);
       /* rs.setResult(array);
        rs.setLengths(array.size());
        rs.setResType(Constant.WS_TYPE_FASTJSONARRAY);*/
    }

    @Override
    public Response delete(Map<String, Object> mapParams) {
        String deploymentId = (String) mapParams.get("deploymentId");
        boolean ensure = (Boolean) mapParams.get("ensure");

        try {
            if (ensure) {
                repositoryService.deleteDeployment(deploymentId, true);
                return new Response().success("1","删除成功!","删除成功!");
            } else {
                List<ProcessDefinition> processDefinitions = repositoryService.createProcessDefinitionQuery()
                        .deploymentId(deploymentId).list();

                int nrOfProcessInstances = 0;
                for (ProcessDefinition processDefinition : processDefinitions) {
                    nrOfProcessInstances += runtimeService.createProcessInstanceQuery()
                            .processDefinitionId(processDefinition.getId()).count();
                }

                if (nrOfProcessInstances == 0) {
                    return new Response().fail("0","确定要删除该部署流程吗？");
                } else {
                   return new Response().fail("0",String.format("该流程部署包含 %d 个未结束任务，确定要删除该部署流程吗？", nrOfProcessInstances));

                }
            }
        } catch (Exception e) {
            logger.error("Activiti exception : deploymentId = > {}", deploymentId);
            logger.error("Activiti exception : ", e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public Response findProcessDefinitionDetail(Map<String, Object> mapParams) {
        String processDefinitionId = (String) mapParams.get("processDefinitionId");
        BpmnModel model = repositoryService.getBpmnModel(processDefinitionId);
        List<Map<String,Object>> modelList = new ArrayList<>();
        if (model != null) {
            Collection<FlowElement> flowElements = model.getMainProcess().getFlowElements();
            for (FlowElement e : flowElements) {
                if(e instanceof org.activiti.bpmn.model.UserTask){
                    Map<String,Object> modelMap = new HashMap<>();
                    modelMap.put("stepId",e.getId());
                    modelMap.put("stepName",e.getName());
                    modelList.add(modelMap);
                }
            }
            return new Response().success("1","查询流程信息成功",modelList);
            /*ResultUtil.makerSusResults("查询流程信息成功!", modelList, rs);*/
        }else{
            return new Response().fail("0","当前流程信息不存在");
        }
    }

    @Override
    public Response getDiagram(Map<String, Object> mapParams) {
        String processDefinitionId = (String) mapParams.get("processDefinitionId");
        String processInstanceId = (String) mapParams.get("processInstanceId");

        List<String> highLightedFlows = Collections.<String> emptyList();
        List<String> highLightedActivities = Collections.<String> emptyList();

        Map<String, JSONObject> subProcessInstanceMap = new HashMap<String, JSONObject>();

        ProcessInstance processInstance = null;
        if (processInstanceId != null) {
            processInstance = runtimeService.createProcessInstanceQuery().processInstanceId(processInstanceId)
                    .singleResult();
            if (processInstance == null) {
                throw new ActivitiObjectNotFoundException("Process instance could not be found");
            }
            processDefinitionId = processInstance.getProcessDefinitionId();

            List<ProcessInstance> subProcessInstances = runtimeService.createProcessInstanceQuery()
                    .superProcessInstanceId(processInstanceId).list();

            for (ProcessInstance subProcessInstance : subProcessInstances) {
                String subDefId = subProcessInstance.getProcessDefinitionId();

                String superExecutionId = ((ExecutionEntity) subProcessInstance).getSuperExecutionId();
                ProcessDefinitionEntity subDef = (ProcessDefinitionEntity) repositoryService
                        .getProcessDefinition(subDefId);

                JSONObject processInstanceJSON = new JSONObject();
                processInstanceJSON.put("processInstanceId", subProcessInstance.getId());
                processInstanceJSON.put("superExecutionId", superExecutionId);
                processInstanceJSON.put("processDefinitionId", subDef.getId());
                processInstanceJSON.put("processDefinitionKey", subDef.getKey());
                processInstanceJSON.put("processDefinitionName", subDef.getName());

                subProcessInstanceMap.put(superExecutionId, processInstanceJSON);
            }
        }

        if (processDefinitionId == null) {
            throw new ActivitiObjectNotFoundException("No process definition id provided");
        }

        ProcessDefinitionEntity processDefinition = (ProcessDefinitionEntity) repositoryService
                .getProcessDefinition(processDefinitionId);

        if (processDefinition == null) {
            throw new ActivitiException("Process definition " + processDefinitionId + " could not be found");
        }

        JSONObject responseJSON = new JSONObject();

        // Process definition
        JSONObject pdrJSON = getProcessDefinitionResponse(processDefinition);

        if (pdrJSON != null) {
            responseJSON.put("processDefinition", pdrJSON);
        }

        // Highlighted activities
        if (processInstance != null) {
            JSONArray activityArray = new JSONArray();
            JSONArray flowsArray = new JSONArray();

            highLightedActivities = runtimeService.getActiveActivityIds(processInstanceId);
            highLightedFlows = getHighLightedFlows(processInstanceId, processDefinition);

            for (String activityName : highLightedActivities) {
                activityArray.add(activityName);
            }

            for (String flow : highLightedFlows)
                flowsArray.add(flow);

            responseJSON.put("highLightedActivities", activityArray);
            responseJSON.put("highLightedFlows", flowsArray);
        }

        // Pool shape, if process is participant in collaboration
        if (processDefinition.getParticipantProcess() != null) {
            ParticipantProcess pProc = processDefinition.getParticipantProcess();

            JSONObject participantProcessJSON = new JSONObject();
            participantProcessJSON.put("id", pProc.getId());
            if (StringUtils.isNotEmpty(pProc.getName())) {
                participantProcessJSON.put("name", pProc.getName());
            }
            else {
                participantProcessJSON.put("name", "");
            }
            participantProcessJSON.put("x", pProc.getX());
            participantProcessJSON.put("y", pProc.getY());
            participantProcessJSON.put("width", pProc.getWidth());
            participantProcessJSON.put("height", pProc.getHeight());

            responseJSON.put("participantProcess", participantProcessJSON);
        }

        // Draw lanes

        if (processDefinition.getLaneSets() != null && !processDefinition.getLaneSets().isEmpty()) {
            JSONArray laneSetArray = new JSONArray();
            for (LaneSet laneSet : processDefinition.getLaneSets()) {
                JSONArray laneArray = new JSONArray();
                if (laneSet.getLanes() != null && !laneSet.getLanes().isEmpty()) {
                    for (Lane lane : laneSet.getLanes()) {
                        JSONObject laneJSON = new JSONObject();
                        laneJSON.put("id", lane.getId());
                        if (StringUtils.isNotEmpty(lane.getName())) {
                            laneJSON.put("name", lane.getName());
                        }
                        else {
                            laneJSON.put("name", "");
                        }
                        laneJSON.put("x", lane.getX());
                        laneJSON.put("y", lane.getY());
                        laneJSON.put("width", lane.getWidth());
                        laneJSON.put("height", lane.getHeight());

                        List<String> flowNodeIds = lane.getFlowNodeIds();
                        JSONArray flowNodeIdsArray = new JSONArray();
                        for (String flowNodeId : flowNodeIds) {
                            flowNodeIdsArray.add(flowNodeId);
                        }
                        laneJSON.put("flowNodeIds", flowNodeIdsArray);

                        laneArray.add(laneJSON);
                    }
                }
                JSONObject laneSetJSON = new JSONObject();
                laneSetJSON.put("id", laneSet.getId());
                if (StringUtils.isNotEmpty(laneSet.getName())) {
                    laneSetJSON.put("name", laneSet.getName());
                }
                else {
                    laneSetJSON.put("name", "");
                }
                laneSetJSON.put("lanes", laneArray);

                laneSetArray.add(laneSetJSON);
            }

            if (laneSetArray.size() > 0){
                responseJSON.put("laneSets", laneSetArray);
            }
        }

        JSONArray sequenceFlowArray = new JSONArray();
        JSONArray activityArray = new JSONArray();

        // Activities and their sequence-flows

        for (ActivityImpl activity : processDefinition.getActivities()) {
            getActivity(processInstanceId, activity, activityArray, sequenceFlowArray, processInstance,
                    highLightedFlows, subProcessInstanceMap);
        }

        responseJSON.put("activities", activityArray);
        responseJSON.put("sequenceFlows", sequenceFlowArray);
        System.out.println(responseJSON.toString());
        return new Response().success("1","查询成功",responseJSON);
    }

    @Override
    public Response getHighlighted(Map<String, Object> mapParams) {
        long start = System.currentTimeMillis();
        String processInstanceId = (String) mapParams.get("processInstanceId");
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode responseJSON = objectMapper.createObjectNode();

        responseJSON.put("processInstanceId", processInstanceId);

        ArrayNode activitiesArray = objectMapper.createArrayNode();
        ArrayNode flowsArray = objectMapper.createArrayNode();

        try {
            ProcessInstance processInstance = runtimeService.createProcessInstanceQuery()
                    .processInstanceId(processInstanceId).singleResult();
            ProcessDefinitionEntity processDefinition = null;
            //  获取历史流程实例
            HistoricProcessInstance historicProcessInstance = historyService.createHistoricProcessInstanceQuery()
                    .processInstanceId(processInstanceId).singleResult();
            if(processInstance!=null){
                processDefinition=(ProcessDefinitionEntity) repositoryService
                        .getProcessDefinition(processInstance.getProcessDefinitionId());
                responseJSON.put("processDefinitionId", processInstance.getProcessDefinitionId());
                List<String> highLightedActivities = runtimeService.getActiveActivityIds(processInstanceId);
                for (String activityId : highLightedActivities) {
                    activitiesArray.add(activityId);
                }
            }else{
                processDefinition=(ProcessDefinitionEntity) repositoryService
                        .getProcessDefinition(historicProcessInstance.getProcessDefinitionId());
                responseJSON.put("processDefinitionId", historicProcessInstance.getProcessDefinitionId());
                // 获取流程历史中已执行节点，并按照节点在流程中执行先后顺序排序
                List<HistoricActivityInstance> historicActivityInstanceList = historyService.createHistoricActivityInstanceQuery()
                        .processInstanceId(processInstanceId).orderByHistoricActivityInstanceId().asc().list();
                // 获取最后一个节点
                activitiesArray.add(historicActivityInstanceList.get(historicActivityInstanceList.size()-1).getActivityId());
            }
            List<Map> highLightedFlows = getHighLightedFlows(processDefinition, processInstanceId);
            for (Map flow : highLightedFlows) {
                String f = JSONArray.toJSONString(flow);
                flowsArray.add(f);
            }
        }
        catch (Exception e) {
            //e.printStackTrace();
            logger.error("getHighlighted error", e);
        }

        responseJSON.set("activities", activitiesArray);
        responseJSON.set("flows", flowsArray);

        ObjectNode frameJSON = objectMapper.createObjectNode();

        frameJSON.put("flag", "1");
        frameJSON.put("prompt", "获取成功");
        frameJSON.put("detail", "");
        frameJSON.put("code", "0");
        frameJSON.set("extend", responseJSON);
        long end = System.currentTimeMillis();
        frameJSON.put("timespent", end - start);

        return new Response().success("1","获取成功",frameJSON);
        /*rs.setResult(frameJSON);
        rs.setResType(Constant.WS_TYPE_JSON);*/
    }

    /**
     * getHighLightedFlows
     *
     * @param processDefinition
     * @param processInstanceId
     * @return
     */
    private List<Map> getHighLightedFlows(ProcessDefinitionEntity processDefinition, String processInstanceId) {

        List<Map> highLightedFlows = new ArrayList<Map>();

        List<HistoricActivityInstance> historicActivityInstances = historyService.createHistoricActivityInstanceQuery()
                .processInstanceId(processInstanceId)
                //order by startime asc is not correct. use default order is correct.
                //.orderByHistoricActivityInstanceStartTime().asc()/*.orderByActivityId().asc()*/
                .list();

        LinkedList<HistoricActivityInstance> hisActInstList = new LinkedList<HistoricActivityInstance>();
        hisActInstList.addAll(historicActivityInstances);

        getHighlightedFlows(processDefinition.getActivities(), hisActInstList, highLightedFlows);

        return highLightedFlows;
    }

    /**
     * getHighlightedFlows
     *
     * code logic:
     * 1. Loop all activities by id asc order;
     * 2. Check each activity's outgoing transitions and eventBoundery outgoing transitions, if outgoing transitions's destination.id is in other executed activityIds, add this transition to highLightedFlows List;
     * 3. But if activity is not a parallelGateway or inclusiveGateway, only choose the earliest flow.
     *
     * @param activityList
     * @param hisActInstList
     * @param highLightedFlows
     */
    private void getHighlightedFlows(List<ActivityImpl> activityList,
                                     LinkedList<HistoricActivityInstance> hisActInstList, List<Map> highLightedFlows) {

        //check out startEvents in activityList
        List<ActivityImpl> startEventActList = new ArrayList<ActivityImpl>();
        Map<String, ActivityImpl> activityMap = new HashMap<String, ActivityImpl>(activityList.size());
        for (ActivityImpl activity : activityList) {

            activityMap.put(activity.getId(), activity);

            String actType = (String) activity.getProperty("type");
            if (actType != null && actType.toLowerCase().indexOf("startevent") >= 0) {
                startEventActList.add(activity);
            }
        }

        //These codes is used to avoid a bug:
        //ACT-1728 If the process instance was started by a callActivity, it will be not have the startEvent activity in ACT_HI_ACTINST table
        //Code logic:
        //Check the first activity if it is a startEvent, if not check out the startEvent's highlight outgoing flow.
        HistoricActivityInstance firstHistActInst = hisActInstList.getFirst();
        String firstActType = (String) firstHistActInst.getActivityType();
        if (firstActType != null && firstActType.toLowerCase().indexOf("startevent") < 0) {
            PvmTransition startTrans = getStartTransaction(startEventActList, firstHistActInst);
            if (startTrans != null) {
                Map<String,Object> flowMap = new HashMap<>();
                flowMap.put("id",startTrans.getId());
                highLightedFlows.add(flowMap);
            }
        }

        while (!hisActInstList.isEmpty()) {
            HistoricActivityInstance histActInst = hisActInstList.removeFirst();
            ActivityImpl activity = activityMap.get(histActInst.getActivityId());
            if (activity != null) {
                boolean isParallel = false;
                String type = histActInst.getActivityType();
                if ("parallelGateway".equals(type) || "inclusiveGateway".equals(type)) {
                    isParallel = true;
                }
                else if ("subProcess".equals(histActInst.getActivityType())) {
                    getHighlightedFlows(activity.getActivities(), hisActInstList, highLightedFlows);
                }

                List<PvmTransition> allOutgoingTrans = new ArrayList<PvmTransition>();
                allOutgoingTrans.addAll(activity.getOutgoingTransitions());
                allOutgoingTrans.addAll(getBoundaryEventOutgoingTransitions(activity));
                List<Map> activityHighLightedFlowIds = getHighlightedFlows(allOutgoingTrans, hisActInstList,
                        isParallel);
                highLightedFlows.addAll(activityHighLightedFlowIds);
            }
        }
    }

    /**
     * Check out the outgoing transition connected to firstActInst from startEventActList
     *
     * @param startEventActList
     * @param firstActInst
     * @return
     */
    private PvmTransition getStartTransaction(List<ActivityImpl> startEventActList,
                                              HistoricActivityInstance firstActInst) {
        for (ActivityImpl startEventAct : startEventActList) {
            for (PvmTransition trans : startEventAct.getOutgoingTransitions()) {
                if (trans.getDestination().getId().equals(firstActInst.getActivityId())) {
                    return trans;
                }
            }
        }
        return null;
    }

    /**
     * getBoundaryEventOutgoingTransitions
     *
     * @param activity
     * @return
     */
    private List<PvmTransition> getBoundaryEventOutgoingTransitions(ActivityImpl activity) {
        List<PvmTransition> boundaryTrans = new ArrayList<PvmTransition>();
        for (ActivityImpl subActivity : activity.getActivities()) {
            String type = (String) subActivity.getProperty("type");
            if (type != null && type.toLowerCase().indexOf("boundary") >= 0) {
                boundaryTrans.addAll(subActivity.getOutgoingTransitions());
            }
        }
        return boundaryTrans;
    }

    /**
     * find out single activity's highlighted flowIds
     *
     * @param
     * @param hisActInstList
     * @return
     */
    private List<Map> getHighlightedFlows(List<PvmTransition> pvmTransitionList,
                                          LinkedList<HistoricActivityInstance> hisActInstList, boolean isParallel) {

        List<Map> highLightedFlowIds = new ArrayList<Map>();

        PvmTransition earliestTrans = null;
        HistoricActivityInstance earliestHisActInst = null;

        for (PvmTransition pvmTransition : pvmTransitionList) {

            String destActId = pvmTransition.getDestination().getId();
            HistoricActivityInstance destHisActInst = findHisActInst(hisActInstList, destActId);
            if (destHisActInst != null) {
                if (isParallel) {
                    Map<String,Object> flowMap = new HashMap<>();
                    flowMap.put("id",pvmTransition.getId());
                    highLightedFlowIds.add(flowMap);
                }
                else if (earliestHisActInst == null
                        || (earliestHisActInst.getId().compareTo(destHisActInst.getId()) > 0)) {
                    earliestTrans = pvmTransition;
                    earliestHisActInst = destHisActInst;
                }
            }
        }

        if ((!isParallel) && earliestTrans != null) {
            Map<String,Object> flowMap = new HashMap<>();
            flowMap.put("id",earliestTrans.getId());
            if(!StringUtil.isNull(earliestTrans.getProperty("name"))&&earliestTrans.getProperty("name").equals("不通过")){
                flowMap.put("isRoll","1");
            }else{
                flowMap.put("isRoll","0");
            }
            highLightedFlowIds.add(flowMap);
        }

        return highLightedFlowIds;
    }

    private HistoricActivityInstance findHisActInst(LinkedList<HistoricActivityInstance> hisActInstList, String actId) {
        for (HistoricActivityInstance hisActInst : hisActInstList) {
            if (hisActInst.getActivityId().equals(actId)) {
                return hisActInst;
            }
        }
        return null;
    }

    private List<String> getHighLightedFlows(String processInstanceId, ProcessDefinitionEntity processDefinition) {

        List<String> highLightedFlows = new ArrayList<String>();
        List<HistoricActivityInstance> historicActivityInstances = historyService.createHistoricActivityInstanceQuery()
                .processInstanceId(processInstanceId).orderByHistoricActivityInstanceStartTime().asc().list();

        List<String> historicActivityInstanceList = new ArrayList<String>();
        for (HistoricActivityInstance hai : historicActivityInstances) {
            historicActivityInstanceList.add(hai.getActivityId());
        }

        // add current activities to list
        List<String> highLightedActivities = runtimeService.getActiveActivityIds(processInstanceId);
        historicActivityInstanceList.addAll(highLightedActivities);

        // activities and their sequence-flows
        for (ActivityImpl activity : processDefinition.getActivities()) {
            int index = historicActivityInstanceList.indexOf(activity.getId());

            if (index >= 0 && index + 1 < historicActivityInstanceList.size()) {
                List<PvmTransition> pvmTransitionList = activity.getOutgoingTransitions();
                for (PvmTransition pvmTransition : pvmTransitionList) {
                    String destinationFlowId = pvmTransition.getDestination().getId();
                    if (destinationFlowId.equals(historicActivityInstanceList.get(index + 1))) {
                        highLightedFlows.add(pvmTransition.getId());
                    }
                }
            }
        }
        return highLightedFlows;
    }

    private void getActivity(String processInstanceId, ActivityImpl activity, JSONArray activityArray,
                             JSONArray sequenceFlowArray, ProcessInstance processInstance, List<String> highLightedFlows,
                             Map<String, JSONObject> subProcessInstanceMap) {

        JSONObject activityJSON = new JSONObject();

        // Gather info on the multi instance marker
        String multiInstance = (String) activity.getProperty("multiInstance");
        if (multiInstance != null) {
            if (!"sequential".equals(multiInstance)) {
                multiInstance = "parallel";
            }
        }

        ActivityBehavior activityBehavior = activity.getActivityBehavior();
        // Gather info on the collapsed marker
        Boolean collapsed = (activityBehavior instanceof CallActivityBehavior);
        Boolean expanded = (Boolean) activity.getProperty(BpmnParse.PROPERTYNAME_ISEXPANDED);
        if (expanded != null) {
            collapsed = !expanded;
        }

        Boolean isInterrupting = null;
        if (activityBehavior instanceof BoundaryEventActivityBehavior) {
            isInterrupting = ((BoundaryEventActivityBehavior) activityBehavior).isInterrupting();
        }

        // Outgoing transitions of activity
        for (PvmTransition sequenceFlow : activity.getOutgoingTransitions()) {
            String flowName = (String) sequenceFlow.getProperty("name");
            boolean isHighLighted = (highLightedFlows.contains(sequenceFlow.getId()));
            boolean isConditional = sequenceFlow.getProperty(BpmnParse.PROPERTYNAME_CONDITION) != null
                    && !((String) activity.getProperty("type")).toLowerCase().contains("gateway");
            boolean isDefault = sequenceFlow.getId().equals(activity.getProperty("default"))
                    && ((String) activity.getProperty("type")).toLowerCase().contains("gateway");

            List<Integer> waypoints = ((TransitionImpl) sequenceFlow).getWaypoints();
            JSONArray xPointArray = new JSONArray();
            JSONArray yPointArray = new JSONArray();
            for (int i = 0; i < waypoints.size(); i += 2) { // waypoints.size()
                // minimally 4: x1, y1,
                // x2, y2
                xPointArray.add(waypoints.get(i));
                yPointArray.add(waypoints.get(i + 1));
            }

            JSONObject flowJSON = new JSONObject();
            flowJSON.put("id", sequenceFlow.getId());
            flowJSON.put("name", flowName);
            flowJSON.put("flow", "(" + sequenceFlow.getSource().getId() + ")--" + sequenceFlow.getId() + "-->("
                    + sequenceFlow.getDestination().getId() + ")");

            if (isConditional){
                flowJSON.put("isConditional", isConditional);
            }
            if (isDefault){
                flowJSON.put("isDefault", isDefault);
            }
            if (isHighLighted){
                flowJSON.put("isHighLighted", isHighLighted);
            }

            flowJSON.put("xPointArray", xPointArray);
            flowJSON.put("yPointArray", yPointArray);

            sequenceFlowArray.add(flowJSON);
        }

        // Nested activities (boundary events)
        JSONArray nestedActivityArray = new JSONArray();
        for (ActivityImpl nestedActivity : activity.getActivities()) {
            nestedActivityArray.add(nestedActivity.getId());
        }

        Map<String, Object> properties = activity.getProperties();
        JSONObject propertiesJSON = new JSONObject();
        for (String key : properties.keySet()) {
            Object prop = properties.get(key);
            if (prop instanceof String){
                propertiesJSON.put(key, (String) properties.get(key));
            }
            else if (prop instanceof Integer){
                propertiesJSON.put(key, (Integer) properties.get(key));
            }
            else if (prop instanceof Boolean){
                propertiesJSON.put(key, (Boolean) properties.get(key));
            }
            else if ("initial".equals(key)) {
                ActivityImpl act = (ActivityImpl) properties.get(key);
                propertiesJSON.put(key, act.getId());
            }
            else if ("timerDeclarations".equals(key)) {
                @SuppressWarnings("unchecked")
                ArrayList<TimerDeclarationImpl> timerDeclarations = (ArrayList<TimerDeclarationImpl>) properties
                        .get(key);
                JSONArray timerDeclarationArray = new JSONArray();

                if (timerDeclarations != null)
                    for (TimerDeclarationImpl timerDeclaration : timerDeclarations) {
                        JSONObject timerDeclarationJSON = new JSONObject();

                        timerDeclarationJSON.put("isExclusive", timerDeclaration.isExclusive());
                        if (timerDeclaration.getRepeat() != null){
                            timerDeclarationJSON.put("repeat", timerDeclaration.getRepeat());
                        }

                        timerDeclarationJSON.put("retries", String.valueOf(timerDeclaration.getRetries()));
                        timerDeclarationJSON.put("type", timerDeclaration.getJobHandlerType());
                        timerDeclarationJSON.put("configuration", timerDeclaration.getJobHandlerConfiguration());
                        //timerDeclarationJSON.put("expression", timerDeclaration.getDescription());

                        timerDeclarationArray.add(timerDeclarationJSON);
                    }
                if (timerDeclarationArray.size() > 0){
                    propertiesJSON.put(key, timerDeclarationArray);
                }
            }
            else if ("eventDefinitions".equals(key)) {
                @SuppressWarnings("unchecked")
                ArrayList<EventSubscriptionDeclaration> eventDefinitions = (ArrayList<EventSubscriptionDeclaration>) properties
                        .get(key);
                JSONArray eventDefinitionsArray = new JSONArray();

                if (eventDefinitions != null) {
                    for (EventSubscriptionDeclaration eventDefinition : eventDefinitions) {
                        JSONObject eventDefinitionJSON = new JSONObject();

                        if (eventDefinition.getActivityId() != null){
                            eventDefinitionJSON.put("activityId", eventDefinition.getActivityId());
                        }


                        eventDefinitionJSON.put("eventName", eventDefinition.getEventName());
                        eventDefinitionJSON.put("eventType", eventDefinition.getEventType());
                        eventDefinitionJSON.put("isAsync", eventDefinition.isAsync());
                        eventDefinitionJSON.put("isStartEvent", eventDefinition.isStartEvent());
                        eventDefinitionsArray.add(eventDefinitionJSON);
                    }
                }

                if (eventDefinitionsArray.size() > 0){
                    propertiesJSON.put(key, eventDefinitionsArray);
                }
            }
            else if ("errorEventDefinitions".equals(key)) {
                @SuppressWarnings("unchecked")
                ArrayList<ErrorEventDefinition> errorEventDefinitions = (ArrayList<ErrorEventDefinition>) properties
                        .get(key);
                JSONArray errorEventDefinitionsArray = new JSONArray();

                if (errorEventDefinitions != null) {
                    for (ErrorEventDefinition errorEventDefinition : errorEventDefinitions) {
                        JSONObject errorEventDefinitionJSON = new JSONObject();

                        if (errorEventDefinition.getErrorCode() != null){
                            errorEventDefinitionJSON.put("errorCode", errorEventDefinition.getErrorCode());
                        }
                        else{
                            errorEventDefinitionJSON.put("errorCode", null);
                        }

                        errorEventDefinitionJSON.put("handlerActivityId", errorEventDefinition.getHandlerActivityId());

                        errorEventDefinitionsArray.add(errorEventDefinitionJSON);
                    }
                }

                if (errorEventDefinitionsArray.size() > 0){
                    propertiesJSON.put(key, errorEventDefinitionsArray);
                }
            }

        }

        if ("callActivity".equals(properties.get("type"))) {
            CallActivityBehavior callActivityBehavior = null;

            if (activityBehavior instanceof CallActivityBehavior) {
                callActivityBehavior = (CallActivityBehavior) activityBehavior;
            }

            if (callActivityBehavior != null) {
                propertiesJSON.put("processDefinitonKey", callActivityBehavior.getProcessDefinitonKey());

                // get processDefinitonId from execution or get last processDefinitonId
                // by key
                JSONArray processInstanceArray = new JSONArray();
                if (processInstance != null) {
                    List<Execution> executionList = runtimeService.createExecutionQuery()
                            .processInstanceId(processInstanceId).activityId(activity.getId()).list();
                    if (!executionList.isEmpty()) {
                        for (Execution execution : executionList) {
                            JSONObject processInstanceJSON = subProcessInstanceMap.get(execution.getId());
                            processInstanceArray.add(processInstanceJSON);
                        }
                    }
                }

                // If active activities nas no instance of this callActivity then add
                // last definition
                if (processInstanceArray.size() == 0
                        && StringUtils.isNotEmpty(callActivityBehavior.getProcessDefinitonKey())) {
                    // Get last definition by key
                    ProcessDefinition lastProcessDefinition = repositoryService.createProcessDefinitionQuery()
                            .processDefinitionKey(callActivityBehavior.getProcessDefinitonKey()).latestVersion()
                            .singleResult();

                    // unuseful fields there are processDefinitionName, processDefinitionKey
                    if (lastProcessDefinition != null) {
                        JSONObject processInstanceJSON = new JSONObject();
                        processInstanceJSON.put("processDefinitionId", lastProcessDefinition.getId());
                        processInstanceJSON.put("processDefinitionKey", lastProcessDefinition.getKey());
                        processInstanceJSON.put("processDefinitionName", lastProcessDefinition.getName());
                        processInstanceArray.add(processInstanceJSON);
                    }
                }

                if (processInstanceArray.size() > 0) {
                    propertiesJSON.put("processDefinitons", processInstanceArray);
                }
            }
        }

        activityJSON.put("activityId", activity.getId());
        activityJSON.put("properties", propertiesJSON);
        if (multiInstance != null){
            activityJSON.put("multiInstance", multiInstance);
        }
        if (collapsed){
            activityJSON.put("collapsed", collapsed);
        }
        if (nestedActivityArray.size() > 0){
            activityJSON.put("nestedActivities", nestedActivityArray);
        }
        if (isInterrupting != null){
            activityJSON.put("isInterrupting", isInterrupting);
        }
        activityJSON.put("x", activity.getX());
        activityJSON.put("y", activity.getY());
        activityJSON.put("width", activity.getWidth());
        activityJSON.put("height", activity.getHeight());

        activityArray.add(activityJSON);

        // Nested activities (boundary events)
        for (ActivityImpl nestedActivity : activity.getActivities()) {
            getActivity(processInstanceId, nestedActivity, activityArray, sequenceFlowArray, processInstance,
                    highLightedFlows, subProcessInstanceMap);
        }
    }

    private JSONObject getProcessDefinitionResponse(ProcessDefinitionEntity processDefinition) {
        JSONObject pdrJSON = new JSONObject();
        pdrJSON.put("id", processDefinition.getId());
        pdrJSON.put("name", processDefinition.getName());
        pdrJSON.put("key", processDefinition.getKey());
        pdrJSON.put("version", processDefinition.getVersion());
        pdrJSON.put("deploymentId", processDefinition.getDeploymentId());
        pdrJSON.put("isGraphicNotationDefined", isGraphicNotationDefined(processDefinition));
        return pdrJSON;
    }

    private boolean isGraphicNotationDefined(ProcessDefinitionEntity processDefinition) {
        return ((ProcessDefinitionEntity) repositoryService.getProcessDefinition(processDefinition.getId()))
                .isGraphicalNotationDefined();
    }
}
