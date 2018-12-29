package com.msjf.finance.pas.bpm.service;

import com.msjf.finance.pas.common.response.Response;

import java.util.Map;

/**
 * Created by 成俊平 on 2018/12/27.
 */
public interface ProcessDefinitionService {
    /**
     * 转换为模型
     *
     * @param mapParams
     * @param rs
     */
    public void convertToModel(Map<String, Object> mapParams, Response rs);

    /**
     * 获取流程部署，根据状态或者所有
     *
     * @param mapParams
     * @param rs
     */
    public void listProcessDefinitionByState(Map<String, Object> mapParams, Response rs);

    /**
     * 获取流流程部署，根据状态或者所有
     *
     * @param mapParams
     * @param rs
     */
    public void listProcessDefinitionByStateAndPage(Map<String, Object> mapParams, Response rs);

    /**
     *
     *
     * @param mapParams
     * @param rs
     */
    public void listProcessDeploymentByState(Map<String, Object> mapParams, Response rs);

    /**
     *
     *
     * @param mapParams
     * @param rs
     */
    public void listProcessDeploymentByStateAndPage(Map<String, Object> mapParams, Response rs);

    /**
     * 读取资源，通过部署ID
     *
     * @param mapParams
     * @param rs
     */
    public void resourceReadByProcessDefinitionId(Map<String, Object> mapParams, Response rs) throws Exception;

    /**
     * 查询发起流程的表单
     *
     * @param mapParams
     * @param rs
     */
    public void findStartForm(Map<String, Object> mapParams, Response rs);

    /**
     * 查询流程定义详情
     *
     * @param mapParams
     * @param rs
     */
    public void findProcessDefinitionDetails(Map<String, Object> mapParams, Response rs);

    /**
     * 获取流程第一批用户活动
     *
     * @param mapParams
     * @param rs
     */
    public void findStartUserActivities(Map<String, Object> mapParams, Response rs);

    /**
     * 删除流程实例
     *
     * @param mapParams
     * @param rs
     */
    public void delete(Map<String, Object> mapParams, Response rs);

    /**
     * 查询流程定义详情
     *
     * @param mapParams
     * @param rs
     */
    public void findProcessDefinitionDetail(Map<String, Object> mapParams, Response rs);

    /**
     * 查询流程配置详情
     *
     * @param mapParams
     * @param rs
     */
    public void getDiagram (Map<String, Object> mapParams, Response rs);

    /**
     * 获取流程实例数据-（高亮节点）
     *
     * @param mapParams
     * @param rs
     */
    public void getHighlighted (Map<String, Object> mapParams, Response rs);

}
