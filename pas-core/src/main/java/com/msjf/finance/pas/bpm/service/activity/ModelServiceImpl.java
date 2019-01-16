package com.msjf.finance.pas.bpm.service.activity;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.msjf.finance.msjf.core.response.Response;
import com.msjf.finance.pas.bpm.dao.mapper.DeploymentQueryMapper;
import com.msjf.finance.pas.bpm.entity.DeploymentEntity;
import com.msjf.finance.pas.bpm.service.ModelService;
import org.activiti.bpmn.converter.BpmnXMLConverter;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.editor.language.json.converter.BpmnJsonConverter;
import org.activiti.engine.ManagementService;
import org.activiti.engine.ProcessEngineConfiguration;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.impl.cmd.AbstractCustomSqlExecution;
import org.activiti.engine.impl.cmd.CustomSqlExecution;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.Model;
import org.activiti.engine.repository.ModelQuery;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.explorer.util.XmlUtil;
import org.apache.batik.transcoder.SVGAbstractTranscoder;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.PNGTranscoder;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.*;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.msjf.finance.pas.bpm.common.ParametersConstant.*;
import static org.activiti.editor.constants.ModelDataJsonConstants.*;

/**
 * Created by Jsan on 2018/12/24.
 */

@Service("modelService")
public class ModelServiceImpl implements ModelService {

    protected Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private RepositoryService repositoryService;



    @Autowired
    private ProcessEngineConfiguration processEngineConfiguration;

    @Autowired
    private RuntimeService runtimeService;

    @Autowired
    private ManagementService managementService;
    /**
     * 模型查询
     *
     */
    @Override
    public String modelList() {
        List<Model> list = repositoryService.createModelQuery().orderByCreateTime().desc().list();

        JSONArray array = (JSONArray) JSON.toJSON(list);
        return array.toString() ;
    }

/**
  模型列表查询分页
 */
    @Override
    public Response modelListPage(Map<String, Object> mapParams) {
        int pageSize = (Integer) mapParams.get(PAGE_SIZE);
        int pageNumber = (Integer) mapParams.get(PAGE_NUMBER);
        int firstResult = pageSize * (pageNumber - 1);

        if (logger.isDebugEnabled()) {
            logger.debug("modelListPage : pageSize->{}, pageNumber->{}", pageSize, pageNumber);
        }
        ModelQuery modelQuery = repositoryService.createModelQuery();
        List<Model> list = modelQuery.orderByCreateTime().desc().listPage(firstResult, pageSize);
        JSONArray array = (JSONArray) JSON.toJSON(list);
        return new Response().success(array);
    }

    /**
     * 模型创建
     *
     * @param mapParams
     */
    @Override
    public Response create(Map<String, Object> mapParams) {
        String name = (String) mapParams.get("name");
        //String key = (String) mapParams.get("key");
        String description = (String) mapParams.get("description");

        if (logger.isDebugEnabled()) {
            logger.debug("create : name->{}, description->{}", name, description);
        }
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            ObjectNode editorNode = objectMapper.createObjectNode();
            editorNode.put("id", "canvas");
            editorNode.put("resourceId", "canvas");
            ObjectNode stencilSetNode = objectMapper.createObjectNode();
            stencilSetNode.put("namespace", "http://b3mn.org/stencilset/bpmn2.0#");
            editorNode.set("stencilset", stencilSetNode);
            Model modelData = repositoryService.newModel();

            ObjectNode modelObjectNode = objectMapper.createObjectNode();
            modelObjectNode.put(MODEL_NAME, name);
            modelObjectNode.put(MODEL_REVISION, 1);
            description = StringUtils.defaultString(description);
            modelObjectNode.put(MODEL_DESCRIPTION, description);
            modelData.setMetaInfo(modelObjectNode.toString());
            modelData.setName(name);
            //modelData.setKey(StringUtils.defaultString(key));

            repositoryService.saveModel(modelData);
            repositoryService.addModelEditorSource(modelData.getId(), editorNode.toString().getBytes("utf-8"));

            JSONObject object = (JSONObject) JSON.toJSON(modelData);
            return  new Response().success(object);
        }
        catch (Exception e) {
            logger.error("创建模型失败", e);
            throw new RuntimeException(e);
        }
    }

    /**
     * 流程复制
     *
     * @param mapParams
     */
    @Override
    public Response copy(Map<String, Object> mapParams) {
        String name = (String) mapParams.get("name");
        String oldModelId = (String) mapParams.get("oldModelId");
        String description = (String) mapParams.get("description");

        if (logger.isDebugEnabled()) {
            logger.debug("create : name->{}, oldModelId->{}, description->{}", name, oldModelId, description);
        }
        try {
            Model newModelData = repositoryService.newModel();

            ObjectNode modelObjectNode = new ObjectMapper().createObjectNode();
            modelObjectNode.put(MODEL_NAME, name);
            description = StringUtils.defaultString(description);
            modelObjectNode.put(MODEL_DESCRIPTION, description);
            newModelData.setMetaInfo(modelObjectNode.toString());
            newModelData.setName(name);

            repositoryService.saveModel(newModelData);

            repositoryService.addModelEditorSource(newModelData.getId(),
                    repositoryService.getModelEditorSource(oldModelId));
            repositoryService.addModelEditorSourceExtra(newModelData.getId(),
                    repositoryService.getModelEditorSourceExtra(oldModelId));

            JSONObject object = (JSONObject) JSON.toJSON(newModelData);
            return new Response().success(object);
        }
        catch (Exception e) {
            if (logger.isErrorEnabled()) {
                logger.error("Exception : oldModelId = > {}", oldModelId);
            }

            throw new RuntimeException(e);
        }
    }

    /**
     * 模型详情数据
     *
     * @param mapParams
     */
    @Override
    public Response editorJson(Map<String, Object> mapParams) {
        String modelId = (String) mapParams.get(MID);

        JSONObject modelNode = null;

        Model model = repositoryService.getModel(modelId);

        if (model != null) {
            try {
                if (StringUtils.isNotEmpty(model.getMetaInfo())) {
                    modelNode = JSON.parseObject(model.getMetaInfo());
                }
                else {
                    modelNode = new JSONObject();
                    modelNode.put(MODEL_NAME, model.getName());
                }
                modelNode.put(MODEL_ID, model.getId());
                JSONObject editorJsonNode = JSON
                        .parseObject(new String(repositoryService.getModelEditorSource(model.getId()), "utf-8"));
                modelNode.put("model", editorJsonNode);

            }
            catch (Exception e) {
                logger.error("Error creating model JSON ", e);
            }
        }
        return  new Response().success(modelNode);
    }

    /**
     * 保存模型
     *
     * @param mapParams
     */
    @Override
    public Response saveModel(Map<String, Object> mapParams) {
        String modelId = (String) mapParams.get(MID);
        String name = (String) mapParams.get("name");
        String description = (String) mapParams.get("description");
        String jsonXml = (String) mapParams.get("json_xml");
        String svgXml = (String) mapParams.get("svg_xml");

        if (logger.isDebugEnabled()) {
            logger.debug("saveModel : modelId=>{}, name=>{}, description=>{}, json_xml=>{}, svg_xml=>{}",
                    new Object[] { modelId, name, description, jsonXml, svgXml });
        }
        try {
            jsonXml = URLDecoder.decode(jsonXml, "utf-8");
            svgXml = URLDecoder.decode(svgXml, "utf-8");
        }
        catch (UnsupportedEncodingException e) {
            logger.error("解码出错", e);
            return new Response().fail("0","保存失败");
        }

        if (logger.isDebugEnabled()) {
            logger.debug("saveModel URLDecoder decode : json_xml=>{}, svg_xml=>{}", new Object[] { jsonXml, svgXml });
        }

        try {
            Model model = repositoryService.getModel(modelId);
            ObjectMapper objectMapper =new ObjectMapper();
            ObjectNode modelJson = (ObjectNode) objectMapper.readTree(model.getMetaInfo());
            modelJson.put(MODEL_NAME, name);
            modelJson.put(MODEL_DESCRIPTION, description);
            model.setMetaInfo(modelJson.toString());
            model.setName(name);

            repositoryService.saveModel(model);
            repositoryService.addModelEditorSource(model.getId(), jsonXml.getBytes("utf-8"));
            InputStream svgStream = new ByteArrayInputStream(svgXml.getBytes("utf-8"));
            TranscoderInput input = new TranscoderInput(svgStream);

            PNGTranscoder transcoder = new PNGTranscoder();
            // Setup output
            ByteArrayOutputStream outStream = new ByteArrayOutputStream();
            TranscoderOutput output = new TranscoderOutput(outStream);

            //Set Font Family
            transcoder.addTranscodingHint(SVGAbstractTranscoder.KEY_DEFAULT_FONT_FAMILY
                    ,processEngineConfiguration.getActivityFontName());
            // Do the transformation
            transcoder.transcode(input, output);
            final byte[] result = outStream.toByteArray();
            repositoryService.addModelEditorSourceExtra(model.getId(), result);
            outStream.close();
            return  new Response().success("1","保存成功","保存成功");
        }
        catch (Exception e) {
            logger.error("Error saving model", e);
            throw new RuntimeException(e);
        }
    }

    /**
     * 根据模型发布流程
     *
     * @param mapParams
     */
    @Override
    public Response deploy(Map<String, Object> mapParams) {
        String modelId = (String) mapParams.get(MID);
        if (logger.isDebugEnabled()) {
            logger.debug("##############ModelServiceImpl#deploy : {}", modelId);
        }
        try {
            Model modelData = repositoryService.getModel(modelId);
            if (modelData == null) {
                return new Response().fail("0","模型数据不存在");
            }

            ObjectNode modelNode = (ObjectNode) new ObjectMapper()
                    .readTree(repositoryService.getModelEditorSource(modelData.getId()));
            BpmnModel model = new BpmnJsonConverter().convertToBpmnModel(modelNode);

            byte[] bpmnBytes = new BpmnXMLConverter().convertToXML(model);

            String processName = modelData.getName() + ".bpmn20.xml";
            Deployment deployment = repositoryService.createDeployment().name(modelData.getName())
                    .addString(processName, new String(bpmnBytes, "utf-8")).deploy();
            if (logger.isDebugEnabled()) {
                logger.debug("部署成功，部署ID={}", deployment.getId());
            }

            JSONObject object = (JSONObject) JSON.toJSON(deployment);

            return  new Response().success("1","部署成功",deployment.getId());
        }
        catch (Exception e) {
            logger.error("根据模型部署流程失败：modelId={}", modelId);
            logger.error("根据模型部署流程失败：", e);
            throw new RuntimeException(e);
        }
    }

    /**
     * 删除模型
     *
     * @param mapParams
     */
    @Override
    public Response delete(Map<String, Object> mapParams) {
        String modelId = (String) mapParams.get(MID);
        final int pageSize = 999;
        final int firstResult = 0;
        if (logger.isDebugEnabled()) {
            logger.debug("delete : {}", modelId);
        }
        try {
            Model model =repositoryService.getModel(modelId);
            String deploymentId = null;
            CustomSqlExecution<DeploymentQueryMapper, List<DeploymentEntity>> customSqlExecution = new AbstractCustomSqlExecution<DeploymentQueryMapper, List<DeploymentEntity>>(
                    DeploymentQueryMapper.class) {
                List<DeploymentEntity> deployments = null;
                @Override
                public List<DeploymentEntity> execute(DeploymentQueryMapper customMapper) {
                    deployments = customMapper.findDeploymentsWithLastVersion(firstResult, pageSize);
                    return deployments;
                }
            };
            List<DeploymentEntity> deployments = managementService.executeCustomSql(customSqlExecution);
            if (null != deployments) {
                for (DeploymentEntity entity : deployments) {
                    if(entity.getName().equals(model.getName())){
                        deploymentId = entity.getId();
                    }
                    break;
                }
            }
            if(deploymentId!=null){
                List<ProcessDefinition> processDefinitions = repositoryService.createProcessDefinitionQuery()
                        .deploymentId(deploymentId).list();
                int nrOfProcessInstances = 0;
                for (ProcessDefinition processDefinition : processDefinitions) {
                    nrOfProcessInstances += runtimeService.createProcessInstanceQuery()
                            .processDefinitionId(processDefinition.getId()).count();
                }
                if (nrOfProcessInstances == 0) {
                    repositoryService.deleteDeployment(deploymentId, true);
                } else {
                    logger.info(String.format("该流程部署包含 %d 个未结束任务，请处理后再进行删除", nrOfProcessInstances));
                    return new Response().fail("0",String.format("该流程部署包含 %d 个未结束任务，请处理后再进行删除", nrOfProcessInstances));
                }
            }
            repositoryService.deleteModel(modelId);
        }
        catch (Exception e) {
            logger.error("删除失败 : ", e);
        }
        return  new Response().success("1","删除成功",modelId);
    }

    /**
     * 模型数据导出
     *
     * @param mapParams
     */
    @Override
    public Response export(Map<String, Object> mapParams) {
        String modelId = (String) mapParams.get(MID);
        String type = (String) mapParams.get("type");

        try {
            Model modelData = repositoryService.getModel(modelId);
            if (modelData == null) {
                return new Response().fail("0","模型不存在");
            }
            BpmnJsonConverter jsonConverter = new BpmnJsonConverter();
            byte[] modelEditorSource = repositoryService.getModelEditorSource(modelData.getId());

            JsonNode editorNode = new ObjectMapper().readTree(modelEditorSource);
            BpmnModel bpmnModel = jsonConverter.convertToBpmnModel(editorNode);

            // 处理异常
            if (bpmnModel.getMainProcess() == null) {
               /* rs.failed("没有内容，不能导出");*/
                return new Response().fail("0","没有内容，不能导出");
            }

            if (type.equals("xml")) {
                BpmnXMLConverter xmlConverter = new BpmnXMLConverter();
                byte[] exportBytes = xmlConverter.convertToXML(bpmnModel);
                /*rs.successful("导出成功");
                rs.setResult(Base64.encodeBase64String(exportBytes));
                rs.setResType(Constant.WS_TYPE_STRING);*/
                return new Response().success("1","导出成功",Base64.encodeBase64String(exportBytes));
            }
           /* else if (type.equals("json")) {
                rs.successful("导出成功");
                rs.setResult(new String(modelEditorSource, "utf-8"));
                rs.setResType(Constant.WS_TYPE_STRING);
            }*/
        }
        catch (Exception e) {
            logger.error("导出model的xml文件失败：modelId={}, type={}", modelId, type);
            logger.error("导出model的xml文件失败：", e);
        }
        return  null;
    }

    /**
     * 模型的图片
     *
     * @param mapParams
     */
    @Override
    public Response initImage(Map<String, Object> mapParams) {
        String modelId = (String) mapParams.get(MID);

        byte[] editorSourceExtra = repositoryService.getModelEditorSourceExtra(modelId);

        if (editorSourceExtra != null) {
            String base64Str = "data:image/png;base64," + Base64.encodeBase64String(editorSourceExtra);
            /*rs.successful("获取成功");
            rs.setResult(base64Str);
            rs.setResType(Constant.WS_TYPE_STRING);*/
            System.out.println(base64Str);
            return new Response().success("1","获取成功",base64Str);
        }
        else {
            /*rs.failed("没有找到图片");*/
            return  new Response().fail("0","没有找到图片");
        }
    }

    /**
     * 模型数据导出为文件流
     *
     * @param mapParams
     */
    @Override
    public Response export(String s,Map<String, Object> mapParams) {
       /* String modelId = (String) mapParams.get(MID);
        String type = (String) mapParams.get("type");

        try {
            Model modelData = repositoryService.getModel(modelId);
            if (modelData == null) {
               *//* rs.failed("模型不存在");*//*
                return new Response().fail("0","模型不存在");
            }
            BpmnJsonConverter jsonConverter = new BpmnJsonConverter();
            byte[] modelEditorSource = repositoryService.getModelEditorSource(modelData.getId());

            JsonNode editorNode = new ObjectMapper().readTree(modelEditorSource);
            BpmnModel bpmnModel = jsonConverter.convertToBpmnModel(editorNode);

            // 处理异常
            if (bpmnModel.getMainProcess() == null) {
                *//*rs.failed("没有内容，不能导出");*//*
                return new Response().fail("0","没有内容，不能导出");
            }
            String filename = "";
            byte[] exportBytes = null;
            String mainProcessId = bpmnModel.getMainProcess().getId();
            if (type.equals("xml")) {
                BpmnXMLConverter xmlConverter = new BpmnXMLConverter();
                exportBytes = xmlConverter.convertToXML(bpmnModel);
                filename = mainProcessId + ".bpmn20.xml";

            }
            else if (type.equals("json")) {
                filename = mainProcessId + ".json";
                exportBytes = modelEditorSource;
            }
            response.setContentType("application/octet-stream");
            ByteArrayInputStream in = new ByteArrayInputStream(exportBytes);
            IOUtils.copy(in, response.getOutputStream());
            response.flushBuffer();
            in.close();
            return  new Response().success("1","导出成功","导出成功");
            *//*rs.successful("导出成功");*//*
        }
        catch (Exception e) {
            logger.error("导出model的xml文件失败：modelId={}, type={}", modelId, type);
            logger.error("导出model的xml文件失败：", e);
            *//*rs.failed("导出失败");*//*
        }*/
        return null;
    }

    /**
     * 导入模型
     *
     * @param mapParams
     */
    @Override
    public Response modelImport(Map<String, Object> mapParams) {
        try {
            ByteArrayInputStream data = (ByteArrayInputStream) mapParams.get("data");
            return   modelImportByInputStream(data,mapParams);
        }catch (Exception e) {
            String errorMsg = e.getMessage().replace(System.getProperty("line.separator"), "<br/>");
            /*rs.failed(errorMsg);*/
            throw new RuntimeException(e);
        }
    }

    /**
     * 导入模型
     *
     * @param mapParams
     */
    @Override
    public Response modelImportByLocalFile(Map<String, Object> mapParams) {
        try {
            InputStream is = new FileInputStream((String) mapParams.get("filename"));
            ByteArrayInputStream data = new ByteArrayInputStream(IOUtils.toByteArray(is));
           return   modelImportByInputStream(data,mapParams);
        }catch (Exception e) {
            String errorMsg = e.getMessage().replace(System.getProperty("line.separator"), "<br/>");
            /*rs.failed(errorMsg);*/
            throw new RuntimeException(e);
        }
    }


    public Response modelImportByInputStream(ByteArrayInputStream data, Map<String, Object> mapParams) throws IOException, XMLStreamException {
        InputStreamReader in = null;
        XMLStreamReader xtr = null;
        try {
            String fileName = (String) mapParams.get("filename");
            if (fileName.endsWith(".bpmn20.xml") || fileName.endsWith(".bpmn")) {
                XMLInputFactory xif = XmlUtil.createSafeXmlInputFactory();
                in = new InputStreamReader(data, "UTF-8");
                xtr = xif.createXMLStreamReader(in);

                BpmnModel bpmnModel = new BpmnXMLConverter().convertToBpmnModel(xtr);

                if (bpmnModel.getMainProcess() == null || bpmnModel.getMainProcess().getId() == null) {
                    /*rs.failed("process id 不能为空");*/
                    return new Response().fail("0","process id 不能为空");
                }
                else {
                    if (bpmnModel.getLocationMap().isEmpty()) {
                        /*rs.failed("location map不能为空");*/
                        return new Response().fail("0","location map不能为空");
                    }
                    else {
                        String processName = null;
                        if (StringUtils.isNotEmpty(bpmnModel.getMainProcess().getName())) {
                            processName = bpmnModel.getMainProcess().getName();
                        }
                        else {
                            processName = bpmnModel.getMainProcess().getId();
                        }

                        Model modelData = repositoryService.newModel();
                        ObjectNode modelObjectNode = new ObjectMapper().createObjectNode();
                        modelObjectNode.put(MODEL_NAME, processName);
                        modelObjectNode.put(MODEL_REVISION, 1);
                        modelData.setMetaInfo(modelObjectNode.toString());
                        modelData.setName(processName);

                        repositoryService.saveModel(modelData);

                        BpmnJsonConverter jsonConverter = new BpmnJsonConverter();
                        ObjectNode editorNode = jsonConverter.convertToJson(bpmnModel);

                        repositoryService.addModelEditorSource(modelData.getId(),
                                editorNode.toString().getBytes("utf-8"));
                        Map resMap = new HashMap(2);
                        resMap.put(MID,modelData.getId());
                        return  new Response().success("1","模型上传成功",resMap);
                        /*ResultUtil.makerSuccessResult(rs,"模型上传成功",resMap);*/
                    }
                }
            }
            else if (fileName.endsWith(".json")) {
                ObjectMapper objectMapper =new ObjectMapper();
                ObjectNode editorNode = (ObjectNode) objectMapper.readTree(data);
                JsonNode properties = editorNode.get("properties");
                String processName = null;
                if (properties != null) {
                    processName = StringUtils.defaultString(properties.get("name").asText(), "");
                }

                Model modelData = repositoryService.newModel();
                ObjectNode modelObjectNode = new ObjectMapper().createObjectNode();
                modelObjectNode.put(MODEL_NAME, processName);
                modelObjectNode.put(MODEL_REVISION, 1);
                modelData.setMetaInfo(modelObjectNode.toString());
                modelData.setName(processName);

                repositoryService.saveModel(modelData);
                repositoryService.addModelEditorSource(modelData.getId(), editorNode.toString().getBytes("utf-8"));
                Map resMap = new HashMap(2);
                resMap.put(MID,modelData.getId());
                return  new Response().success("1","模型上传成功",resMap);
                /*ResultUtil.makerSuccessResult(rs,"模型上传成功",resMap);*/
            }
            else {
                return  new Response().fail("0","模型只支持[.bpmn20.xml]、[.bpmn]和[.json]后缀文件");
                /*rs.failed("模型只支持[.bpmn20.xml]、[.bpmn]和[.json]后缀文件");*/
            }
        } finally {
            if (in != null) {
                try {
                    in.close();
                }
                catch (IOException e) {

                }
            }
            if (xtr != null) {
                try {
                    xtr.close();
                }
                catch (XMLStreamException e) {

                }
            }
        }
    }

}
