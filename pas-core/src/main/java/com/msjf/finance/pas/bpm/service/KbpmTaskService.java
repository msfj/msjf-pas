package com.msjf.finance.pas.bpm.service;

import com.alibaba.fastjson.JSONArray;
import com.msjf.finance.msjf.core.response.Response;
import org.activiti.engine.impl.pvm.PvmTransition;

import java.util.List;
import java.util.Map;

/**
 * Created by chengjunping on 2018/12/27.
 */
public interface KbpmTaskService {
    /**
     * 任务签收
     *
     * @param mapParams
     * @param rs
     */
    public void claim(Map<String, Object> mapParams, Response rs);

    /**
     * 任务反签收
     *
     * @param mapParams
     * @param rs
     */
    public void unClaim(Map<String, Object> mapParams, Response rs);

    /**
     * 用户task列表
     *
     * @param mapParams
     * @param rs
     */
    public void userTaskList(Map<String, Object> mapParams, Response rs);

    /**
     * Task详情，包括表单和变量
     *
     * @param mapParams
     * @param rs
     */
    public void userTaskDetails(Map<String, Object> mapParams, Response rs);

    /**
     * 获取任务的表单数据
     *
     * @param mapParams
     * @param rs
     */
    public void getTaskForm(Map<String, Object> mapParams, Response rs);

    /**
     * 提交任务表单数据修改
     *
     * @param mapParams
     * @param rs
     */
    public void submitTaskFormData(Map<String, Object> mapParams, Response rs);

    /**
     * 保存任务表单数据
     *
     * @param mapParams
     * @param rs
     */
    public void saveFormData(Map<String, Object> mapParams, Response rs);

    /**
     * 流程实例任务列表
     *
     * @param mapParams
     * @param rs
     */
    public void processInstanceTaskList(Map<String, Object> mapParams, Response rs);

    /**
     * 通过流程演示获取下一步
     *
     * @param mapParams
     * @param rs
     */
    public void getNextTask(Map<String, Object> mapParams, Response rs);

    /**
     * 根据出口获取下一批userTask
     *
     * @param transitions
     * @param map
     * @return
     */
    public JSONArray nextTaskDefinition(List<PvmTransition> transitions, Map<String, Object> map);


    /**
     *
     *
     * @param mapParams
     * @param rs
     */
    public void getNextGateway(Map<String, Object> mapParams, Response rs);


    public Response getAllNextTask(Map<String, Object> mapParams);
}
