package com.msjf.finance.pas.bpm.service;



import com.msjf.finance.msjf.core.response.Response;

import java.util.Map;

/**
 * Created by 成俊平 on 2019/1/3.
 */
public interface ProcessInstanceService {

    /**
     * 读取资源，通过流程实例ID
     *
     * @param mapParams
     * @param rs
     */
    public void resourceReadByProcessInstanceId(Map<String, Object> mapParams, Response rs);

    /**
     * 挂起、激活流程定义
     *
     * @param mapParams
     * @param rs
     */
    public void updateProcessDefinitionState(Map<String, Object> mapParams, Response rs);


    /**
     * 挂起、激活流程实例
     *
     * @param mapParams
     * @param rs
     */
    public void updateProcessInstanceState(Map<String, Object> mapParams, Response rs);


    /**
     * 流程追踪
     *
     * @param mapParams
     * @param rs
     */
    public void traceProcess(Map<String, Object> mapParams, Response rs);

    /**
     * 流程实例启动（内置表单）
     *
     * @param mapParams
     * @param rs
     */
    public void submitStartFormAndStartProcessInstance(Map<String, Object> mapParams, Response rs);

    /**
     * 流程实例启动（关键业务主键）
     *
     * @param mapParams
     * @param rs
     */
    public void startWorkflow(Map<String, Object> mapParams, Response rs);

    /**
     * 查询流程实例
     *
     * @param mapParams
     * @param rs
     */
    public void listRunning(Map<String, Object> mapParams, Response rs);

    /**
     * 查询流程实例
     *
     * @param mapParams
     * @param rs
     */
    public void listRunningByPage(Map<String, Object> mapParams, Response rs);

    /**
     * 我发起的流程
     *
     * @param mapParams
     * @param rs
     */
    public void myProcessInstance(Map<String, Object> mapParams, Response rs);

    /**
     * 用户参与的流程实例
     *
     * @param mapParams
     * @param rs
     */
    public void involvedList(Map<String, Object> mapParams, Response rs);

    /**
     * 获取流程实例所有变量
     *
     * @param mapParams
     * @param rs
     */
    public void getVariables(Map<String, Object> mapParams, Response rs);

    /**
     * 获取流程实例的审核意见
     *
     * @param mapParams
     * @param rs
     */
    public void getComments(Map<String, Object> mapParams, Response rs);

    /**
     * 查询发起流程的表单
     *
     * @param mapParams
     * @param rs
     */
    public void findStartForm(Map<String, Object> mapParams, Response rs);

    /**
     * 列出所有实例（正在运行的、已经结束的）
     *
     * @param mapParams
     * @param rs
     */
    public void listInstance(Map<String, Object> mapParams, Response rs);

    /**
     * 查询流程实例中，有某人参与审核但还没有到达的
     * @param params
     * @param rs
     */
    public void listInstanceContainSomeone(Map<String, Object> params, Response rs);

    /**
     * 流程是否完成
     *
     * @param mapParams
     * @param rs
     */
    public void isInstanceCompleted(Map<String, Object> mapParams, Response rs);
}
