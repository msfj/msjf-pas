package com.msjf.finance.pas.bpm.service;

import com.msjf.finance.pas.common.response.Response;
import org.activiti.engine.repository.Model;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import java.util.List;
import java.util.Map;

/**
 * Created by Jsan on 2018/12/24.
 */
public interface ModelService {

    /**
     * 模型查询
     *

     */
    public String modelList();

    /**
     *
     *
     * @param mapParams
     */
    public Response modelListPage(Map<String, Object> mapParams);

    /**
     * 模型创建
     *
     * @param mapParams
     */
    public Response create(Map<String, Object> mapParams);

    /**
     * 流程复制
     *
     * @param mapParams
     */
    public Response copy(Map<String, Object> mapParams);

    /**
     * 模型详情数据
     *
     * @param mapParams
     */
    public Response editorJson(Map<String, Object> mapParams);

    /**
     * 保存模型
     *
     * @param mapParams
     */
    public Response saveModel(Map<String, Object> mapParams);

    /**
     * 根据模型发布流程
     *
     * @param mapParams
     */
    public Response deploy(Map<String, Object> mapParams);

    /**
     * 删除模型
     *
     * @param mapParams
     */
    public Response delete(Map<String, Object> mapParams);

    /**
     * 模型数据导出
     *
     * @param mapParams
     */
    public Response export(Map<String, Object> mapParams);

    /**
     * 模型的图片
     *
     * @param mapParams
     */
    public Response initImage(Map<String, Object> mapParams);

    /**
     * 模型数据导出为文件流
     *
     * @param mapParams
     */
    public Response export(ServletRequest request, ServletResponse response, Map<String, Object> mapParams);

    /**
     * 导入模型
     *
     * @param mapParams
     */
    public Response modelImport(Map<String, Object> mapParams);

    /**
     * 导入模型
     *
     * @param mapParams
     */
    public Response modelImportByLocalFile(Map<String, Object> mapParams);
}
