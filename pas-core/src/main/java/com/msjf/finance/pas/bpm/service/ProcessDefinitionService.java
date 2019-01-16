package com.msjf.finance.pas.bpm.service;



import com.msjf.finance.msjf.core.response.Response;

import java.util.Map;

/**
 * Created by 成俊平 on 2018/12/27.
 */
public interface ProcessDefinitionService {
    /**
     * 转换为模型
     *
     * @param mapParams
     */
    public Response convertToModel(Map<String, Object> mapParams);

    /**
     * 获取流程部署，根据状态或者所有
     *
     * @param mapParams
     */
    public Response listProcessDefinitionByState(Map<String, Object> mapParams);

    /**
     * 获取流流程部署，根据状态或者所有
     *
     * @param mapParams
     */
    public Response listProcessDefinitionByStateAndPage(Map<String, Object> mapParams);

    /**
     *
     *
     * @param mapParams
     */
    public Response listProcessDeploymentByState(Map<String, Object> mapParams);

    /**
     *
     *
     * @param mapParams
     */
    public Response listProcessDeploymentByStateAndPage(Map<String, Object> mapParams);

    /**
     * 读取资源，通过部署ID
     *
     * @param mapParams
     */
    public Response resourceReadByProcessDefinitionId(Map<String, Object> mapParams) throws Exception;

    /**
     * 查询发起流程的表单
     *
     * @param mapParams
     */
    public Response findStartForm(Map<String, Object> mapParams);

    /**
     * 查询流程定义详情
     *
     * @param mapParams
     */
    public Response findProcessDefinitionDetails(Map<String, Object> mapParams);

    /**
     * 获取流程第一批用户活动
     *
     * @param mapParams
     */
    public Response findStartUserActivities(Map<String, Object> mapParams);

    /**
     * 删除流程实例
     *
     * @param mapParams
     */
    public Response delete(Map<String, Object> mapParams);

    /**
     * 查询流程定义详情
     *
     * @param mapParams
     */
    public Response findProcessDefinitionDetail(Map<String, Object> mapParams);

    /**
     * 查询流程配置详情
     *
     * @param mapParams
     */
    public Response getDiagram (Map<String, Object> mapParams);

    /**
     * 获取流程实例数据-（高亮节点）
     *
     * @param mapParams
     */
    public Response getHighlighted (Map<String, Object> mapParams);

}
