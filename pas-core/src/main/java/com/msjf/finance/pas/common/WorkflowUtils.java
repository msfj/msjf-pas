package com.msjf.finance.pas.common;

import com.msjf.finance.pas.bpm.common.ParametersConstant;
import org.activiti.bpmn.model.*;
import org.activiti.engine.EngineServices;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.delegate.event.ActivitiEvent;
import org.activiti.engine.form.FormData;
import org.activiti.engine.impl.persistence.entity.VariableInstance;
import org.activiti.engine.repository.ProcessDefinition;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by chengjunping on 2018/12/27.
 */
public class WorkflowUtils {

    public  static final String COMMON_LIST_DEFAULT_ITEM_VALUE = "######";
    private static Logger logger = LoggerFactory.getLogger(WorkflowUtils.class);
    public static final List<String> DEFAULT_COMMON_LIST = Arrays.asList(new String[]{COMMON_LIST_DEFAULT_ITEM_VALUE});

    //由于修改多实例审核人设置逻辑，这里无需进行默认值判断了
    public static boolean isDefaultAssigneeList(List setList){
//        if(setList == null){
//            return false;
//        }
//        if(setList.size() == 1){
//            return DEFAULT_COMMON_LIST.get(0).equals(setList.get(0));
//        }
        return false;
    }

    public static boolean isDefaultDepartmentList(List setList){
//        if(setList == null){
//            return false;
//        }
//        if(setList.size() == 1){
//            return DEFAULT_COMMON_LIST.get(0).equals(setList.get(0));
//        }
        return false;
    }

    /**
     * 转换流程节点类型为中文说明
     *
     * @param type 英文名称
     * @return 翻译后的中文名称
     */
    public static String parseToZhType(String type) {
        Map<String, String> types = new HashMap<String, String>();
        types.put("userTask", "用户任务");
        types.put("serviceTask", "系统任务");
        types.put("startEvent", "开始节点");
        types.put("endEvent", "结束节点");
        types.put("exclusiveGateway", "条件判断节点(系统自动根据条件处理)");
        types.put("inclusiveGateway", "并行处理任务");
        types.put("callActivity", "子流程");
        return types.get(type) == null ? type : types.get(type);
    }

    /**
     * 导出图片文件到硬盘
     *
     * @return 文件的全路径
     */
    public static String exportDiagramToFile(RepositoryService repositoryService, ProcessDefinition processDefinition, String exportDir) throws IOException {
        String diagramResourceName = processDefinition.getDiagramResourceName();
        String key = processDefinition.getKey();
        int version = processDefinition.getVersion();
        String diagramPath = "";

        InputStream resourceAsStream = repositoryService.getResourceAsStream(processDefinition.getDeploymentId(), diagramResourceName);
        byte[] b = new byte[resourceAsStream.available()];

        @SuppressWarnings("unused")
        int len = -1;
        resourceAsStream.read(b, 0, b.length);

        // create file if not exist
        String diagramDir = exportDir + "/" + key + "/" + version;
        File diagramDirFile = new File(diagramDir);
        if (!diagramDirFile.exists()) {
            diagramDirFile.mkdirs();
        }
        diagramPath = diagramDir + "/" + diagramResourceName;
        File file = new File(diagramPath);

        // 文件存在退出
        if (file.exists()) {
            // 文件大小相同时直接返回否则重新创建文件(可能损坏)
            logger.debug("diagram exist, ignore... : {}", diagramPath);
            return diagramPath;
        } else {
            file.createNewFile();
        }

        logger.debug("export diagram to : {}", diagramPath);

        // wirte bytes to file
        FileUtils.writeByteArrayToFile(file, b, true);
        return diagramPath;
    }

    public static Activity getFirstOrderUserTask(BpmnModel model){
        return getOrderUserTask(model).get(0);
    }

    //获取指定节点
    public static UserTask getUserTask(String taskDefinitionKey,BpmnModel model){
        FlowElement flowElement = model.getFlowElement(taskDefinitionKey);
        if(flowElement instanceof UserTask ){
            return (UserTask) flowElement;
        }
        return null;
    }

    public static Map<String,Activity> getOrderUserTaskMap(BpmnModel model){
        return getOrderUserTaskMap(model,model.getMainProcess().getFlowElements());
    }

    /**
     * 查找所有的UserTask(包括子流程里面的)
     * @param model
     * @param flowElements
     * @return
     */
    public static Map<String,Activity> getOrderUserTaskMap(BpmnModel model,Collection<FlowElement> flowElements){
        //先找到起始节点
        FlowElement flowElement = getStartElement(flowElements);

        if (flowElement == null){
            throw new RuntimeException("未找到起始节点:" + model.getTargetNamespace());
        }

        Map<String,Activity> orderUserTasks = new LinkedHashMap<>(flowElements.size() / 2 +1);
        List<SequenceFlow> searchedFlowElement = new ArrayList<SequenceFlow>(16);
        Map<String,FlowElement> flowElementMap = new HashMap<String, FlowElement>(flowElements.size());

        //缓存，避免查找usertask执行多个循环
        for(FlowElement inflowElement: flowElements) {
            flowElementMap.put(inflowElement.getId(),inflowElement);
        }

        List<SequenceFlow> outGoingFlow = ((StartEvent)flowElement).getOutgoingFlows();
        Deque<SequenceFlow> flowElementsStack = new ArrayDeque<SequenceFlow>(4);

        //加入队列
        flowElementsStack.addAll(outGoingFlow);

        //一直遍历所有的连接线
        while (flowElementsStack.size() != 0){
            //取出一个连接线
            SequenceFlow seqTemp = flowElementsStack.poll();

            FlowElement targetFlowElement = flowElementMap.get(seqTemp.getTargetRef());
            //保存已搜索的连接点，避免有环的情况下重复搜索
            if(!searchedFlowElement.contains(seqTemp)){
                searchedFlowElement.add(seqTemp);
            }else {
                continue;
            }
            //如果是用户节点，并且还没放入顺序列表，则放入
            if (targetFlowElement instanceof UserTask){
                if(!orderUserTasks.containsKey(targetFlowElement.getId())){
                    orderUserTasks.put(targetFlowElement.getId(),(UserTask)targetFlowElement);
                }
                //如果遇到子流程，则递归继续搜索
            }else if(targetFlowElement instanceof SubProcess){
                if(!orderUserTasks.containsKey(targetFlowElement.getId())){
                    orderUserTasks.put(targetFlowElement.getId(),(SubProcess)targetFlowElement);
                    //继续搜索子流程里的节点信息
                    orderUserTasks.putAll(getOrderUserTaskMap(
                            model,((SubProcess)targetFlowElement).getFlowElements()));
                }
            }
            //查询目标连接点的节点连接列表
            outGoingFlow = ((FlowNode) targetFlowElement).getOutgoingFlows();
            //加入队列以便下一步搜索
            for (SequenceFlow outGoingFlowEle : outGoingFlow){
                flowElementsStack.offer(outGoingFlowEle);
            }
        }
        return orderUserTasks;
    }

    public static List<Activity> getOrderUserTask(BpmnModel model) {
        return getOrderUserTaskMap(model).values().stream().collect(Collectors.toList());
    }

    /**
     * 找到指定节点，往右边查找
     * 指定节点右边所有节点(未审核节点)
     * @param activityId
     * @param model
     * @return
     */
    public static Map<String,UserTask> getRightUserTask(String activityId,BpmnModel model){
        List<FlowElement> flowElements = (List)model.getMainProcess().getFlowElements();
        //先找到最后一个节点（一定会找到）
        FlowElement endElement      = getEndElement(model);
        //找到id为activityId的节点
        FlowElement activityElement = getSpecifiedElement(activityId,model);

        if (activityElement == null || endElement == null ){
            throw new RuntimeException("未找到特定或结尾节点:" + model.toString());
        }

        Map<String,UserTask> orderUserTasks = new LinkedHashMap<String, UserTask>(flowElements.size() / 2 +1);
        List<SequenceFlow> searchedFlowElement = new ArrayList<SequenceFlow>(16);
        Map<String,FlowElement> flowElementMap = new HashMap<String, FlowElement>(flowElements.size());

        //缓存，避免查找usertask执行多个循环
        for(FlowElement inflowElement: flowElements) {
            flowElementMap.put(inflowElement.getId(),inflowElement);
        }

        List<SequenceFlow> outGoingFlow = ((FlowNode)activityElement).getOutgoingFlows();
        Deque<SequenceFlow> flowElementsStack = new ArrayDeque<SequenceFlow>(4);

        //加入队列
        flowElementsStack.addAll(outGoingFlow);

        //一直遍历所有的连接线
        while (flowElementsStack.size() != 0){
            //取出一个连接线
            SequenceFlow seqTemp = flowElementsStack.poll();

            FlowElement targetFlowElement = flowElementMap.get(seqTemp.getTargetRef());

            //找到目前匹配的activity时，则重新拉取节点
            if(targetFlowElement.getId().equals(endElement.getId())){
                continue;
            }

            //保存已搜索的连接点，避免有环的情况下重复搜索
            if(!searchedFlowElement.contains(seqTemp)){
                searchedFlowElement.add(seqTemp);
            }else {
                continue;
            }
            //如果是用户节点，并且还没放入顺序列表，则放入
            if (targetFlowElement instanceof UserTask){
                if(!orderUserTasks.containsKey(targetFlowElement.getId())){
                    orderUserTasks.put(targetFlowElement.getId(),(UserTask)targetFlowElement);
                }
            }
            //查询目标连接点的节点连接列表
            outGoingFlow = ((FlowNode) targetFlowElement).getOutgoingFlows();
            //加入队列以便下一步搜索
            for (SequenceFlow outGoingFlowEle : outGoingFlow){
                flowElementsStack.offer(outGoingFlowEle);
            }
        }
        return orderUserTasks;
    }

    /**
     * 就是找到目标节点，查左边已审核节点
     * 思想就是一条线，找到起始节点，
     * 这一条线所有节点就是已审核节点
     * @param activityId
     * @param model
     * @return
     */
    public static Map<String,UserTask> getLeftUserTask(String activityId,BpmnModel model){
        List<FlowElement> flowElements = (List)model.getMainProcess().getFlowElements();
        //找到id为activityId的节点
        FlowElement activityElement = getSpecifiedElement(activityId,model);
        //找到起始节点（一定会找到）
        FlowElement startElement = getStartElement(model);

        if (activityElement == null || startElement == null){
            throw new RuntimeException("未找到起始或特定节点:" + model.toString());
        }

        Map<String,UserTask> orderUserTasks = new LinkedHashMap<String, UserTask>(flowElements.size() / 2 +1);
        List<SequenceFlow> searchedFlowElement = new ArrayList<SequenceFlow>(16);
        Map<String,FlowElement> flowElementMap = new HashMap<String, FlowElement>(flowElements.size());

        //缓存，避免查找usertask执行多个循环
        for(FlowElement inflowElement: flowElements) {
            flowElementMap.put(inflowElement.getId(),inflowElement);
        }

        List<SequenceFlow> incomingFlows = ((FlowNode)activityElement).getIncomingFlows();
        Deque<SequenceFlow> flowElementsStack = new ArrayDeque<SequenceFlow>(4);

        //加入队列
        flowElementsStack.addAll(incomingFlows);

        //一直遍历所有的连接线
        while (flowElementsStack.size() != 0){
            //取出一个连接线
            SequenceFlow seqTemp = flowElementsStack.poll();

            FlowElement targetFlowElement = flowElementMap.get(seqTemp.getSourceRef());

            //如果已经碰到起始节点，则重新拉取节点
            if(targetFlowElement.getId().equals(startElement.getId())){
                continue;
            }

            //保存已搜索的连接点，避免有环的情况下重复搜索
            if(!searchedFlowElement.contains(seqTemp)){
                searchedFlowElement.add(seqTemp);
            }else {
                continue;
            }

            //如果是用户节点，并且还没放入顺序列表，则放入
            if (targetFlowElement instanceof UserTask){
                if(!orderUserTasks.containsKey(targetFlowElement.getId())){
                    orderUserTasks.put(targetFlowElement.getId(),(UserTask)targetFlowElement);
                }
            }

            //查询目标输入连接点的节点连接列表
            incomingFlows = ((FlowNode) targetFlowElement).getIncomingFlows();
            //加入队列以便下一步搜索
            for (SequenceFlow incomingFlow : incomingFlows){
                flowElementsStack.offer(incomingFlow);
            }
        }
        return orderUserTasks;
    }

    public static FlowElement getStartElement(BpmnModel model){
        Collection<FlowElement> flowElements = model.getMainProcess().getFlowElements();
        for(FlowElement flowElement: flowElements) {
            if(flowElement instanceof StartEvent){
                return flowElement;
            }
        }
        return null;
    }

    public static FlowElement getStartElement(Collection<FlowElement> flowElements){
        for(FlowElement flowElement: flowElements) {
            if(flowElement instanceof StartEvent){
                return flowElement;
            }
        }
        return null;
    }

    public static FlowElement getEndElement(BpmnModel model){
        Collection<FlowElement> flowElements = model.getMainProcess().getFlowElements();
        for(FlowElement flowElement: flowElements) {
            if(flowElement instanceof EndEvent){
                return flowElement;
            }
        }
        return null;
    }

    public static FlowElement getEndElement(Collection<FlowElement> flowElements){
        for(FlowElement flowElement: flowElements) {
            if(flowElement instanceof EndEvent){
                return flowElement;
            }
        }
        return null;
    }

    public static boolean isEndElement(String activityId,BpmnModel model){
        FlowElement flowElement = getSpecifiedElement(activityId,model);
        if (flowElement == null){
            return false;
        }else if(flowElement instanceof EndEvent){
            return true;
        }
        return false;
    }

    /**
     * activityId为空则获取尾节点
     * @param activityId
     * @param model
     * @return
     */
    public static FlowElement getSpecifiedElement(String activityId,BpmnModel model){
        if(activityId == null){
            return getEndElement(model);
        }
        return model.getFlowElement(activityId);
    }

    /**
     * 获取当前流程的所有基础变量
     * @param engineServices
     * @param activitiEvent
     * @return
     */
    public static Map getVariablesFromProcessInstance(EngineServices engineServices, ActivitiEvent activitiEvent){
        Map<String,VariableInstance> variableInstances =
                engineServices.getRuntimeService().getVariableInstances(activitiEvent.getExecutionId());
        final Map variables = new HashMap(variableInstances.size());
        variableInstances.forEach((K,V) -> {
            if( V.getTypeName() == null || ("string").equals(V.getTypeName())
                    || ("null").equals(V.getTypeName())
                    || ("double").equals(V.getTypeName())
                    || ("long").equals(V.getTypeName())
                    || ("integer").equals(V.getTypeName())){
                if(!(K.contains(ParametersConstant.ASSIGNEE_LIST_SUFFIX)  //去掉业务系统的业务List变量
                        || K.contains(ParametersConstant.DEPARTMENT_LIST_SUFFIX))){
                    variables.put(K,V.getValue());
                }
            }
        });
        return variables;
    }

   /* *//**
     * 此方法不具有通用性，可能被用户变量污染
     * 导致计算失败
     * @param mapParam
     * @param condition
     * @return
     *//*
    public static Object getCheckCondition(Map<String, Object> mapParam, String condition) {
        Iterator<Map.Entry<String, Object>> it = mapParam.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, Object> entry = it.next();
            if (!entry.getKey().toString().equals("user") && !"wsuser".equals(entry.getKey().toString())
                    && entry.getValue() != null) {
                condition = condition.replaceAll(entry.getKey().toString(), entry.getValue().toString());
            }
        }
        return ExpressUtil.eval(condition.substring(2, condition.length() - 1));
    }*/

    /**
     * 从流程模型中获取对应propertyKey的参数值
     * @param activityId
     * @param processDefinitionId
     * @param propertyKey
     * @param repositoryService
     * @return
     */
    public static String getFormPropertyFromBpmnModel(String activityId,String processDefinitionId,
                                                      final String propertyKey,RepositoryService repositoryService){

        BpmnModel bpmnModel = repositoryService.getBpmnModel(processDefinitionId);
        UserTask userTask = (UserTask) WorkflowUtils.getSpecifiedElement(activityId,bpmnModel);
        List<FormProperty> formProperties = userTask.getFormProperties();
        Optional<FormProperty> formPropertyOptional = formProperties.stream().filter(e -> e.getId().equals(propertyKey)).findFirst();
        String value;
        if(formPropertyOptional.isPresent()){
            value = formPropertyOptional.get().getExpression();
            if(value == null){
                //数据结构详细参见本类@see setAndAddTaskFormData(FormData taskFormData, Map variables) 函数
                Optional<FormValue> formValueOptional = formPropertyOptional.get().getFormValues().
                        stream().filter(e -> "DEFAULT".equals(e.getName())).findFirst();
                if(formValueOptional.isPresent()){
                    value = formValueOptional.get().getId();
                    return value;
                }
            }else {
                return value;
            }
        }
        return null;
    }

    /**
     * 设置表单数据
     * @param taskFormData
     * @param variables
     */
    public static void setAndAddTaskFormData(FormData taskFormData, Map variables){

        if (taskFormData == null
                || taskFormData.getFormProperties() == null) {
            return;
        }

        List<org.activiti.engine.form.FormProperty> taskFormProperties = taskFormData.getFormProperties();

        for (org.activiti.engine.form.FormProperty taskFormProperty : taskFormProperties) {
            // 如果是必须提交的枚举类型，并且请求参数里面没有，使用枚举值里面的value填充默认值
            String sysFormPropertyId = taskFormProperty.getId();

            if ("enum".equals(taskFormProperty.getType().getName())
                    && taskFormProperty.isRequired()
                    && !variables.containsKey(sysFormPropertyId)) {

                Map<String, String> values = (Map<String, String>) taskFormProperty.getType()
                        .getInformation("values");
                /**
                 * 特别说明：enum的取值范围是values所有key，在模型保存时，特别要求前端额外传递了：
                 *  {id:'（需要设定的默认值）',name:'DEFAULT'},{id:'value',name:'（需要设定的默认值）'}
                 *  value对应的值同时也是默认值的key，所以通过设置value对应的值也是合法值；
                 *  下方注释的代码直接取name:'DEFAULT'的枚举项也是可以的
                 */
                if (values != null) {
                    String value = values.get("value");
                    variables.put(sysFormPropertyId, value);
                    if (logger.isDebugEnabled()) {
                        logger.debug("Add submit form property : {} => {}", sysFormPropertyId, value);
                    }
                }
            }
        }
    }

    /**
     * 多实例设置默认变量值
     * @param activity
     * @param variables
     */
    public static void setMultiInstanceCommonVariable(Activity activity,Map<String, Object> variables){

        if(activity == null){
            return;
        }

        if(activity instanceof UserTask){
            String assigneeListCollectionName = activity.getId() + ParametersConstant.ASSIGNEE_LIST_SUFFIX;
            variables.put(assigneeListCollectionName,WorkflowUtils.COMMON_LIST_DEFAULT_ITEM_VALUE);
        }else if(activity instanceof SubProcess){
            String assigneeListCollectionName = activity.getId() + ParametersConstant.ASSIGNEE_LIST_SUFFIX;
            variables.put(assigneeListCollectionName,WorkflowUtils.COMMON_LIST_DEFAULT_ITEM_VALUE);

            SubProcess subProcess = (SubProcess)activity;
            //子流程至少有3个节点，否则无意义
            if(subProcess.getFlowElements().size() < 3){
                throw new RuntimeException("子流程内应该大于3个节点:" + subProcess.getName());
            }
            //查找开始节点后的第一个节点，继续设置
            FlowElement flowElement = ((List<FlowElement>)subProcess.getFlowElements()).get(1);
            if(activity instanceof UserTask || activity instanceof SubProcess){
                if(isActivityMultiInstance(activity)){
                    setMultiInstanceCommonVariable((Activity) flowElement,variables);
                }
            }
        }
    }

    public static boolean isActivityMultiInstance(Activity activity){
        MultiInstanceLoopCharacteristics loopCharacteristics = activity.getLoopCharacteristics();
        if(loopCharacteristics != null){
            //默认都是多实例
            return true;
        }else {
            return false;
        }
    }
}
