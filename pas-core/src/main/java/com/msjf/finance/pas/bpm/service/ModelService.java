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
     * @param rs
     */
    public List<Model> modelListPage(Map<String, Object> mapParams, Response rs);

    /**
     * 模型创建
     *
     * @param mapParams
     * @param rs
     */
    public void create(Map<String, Object> mapParams, Response rs);

    /**
     * 流程复制
     *
     * @param mapParams
     * @param rs
     */
    public void copy(Map<String, Object> mapParams, Response rs);

    /**
     * 模型详情数据
     *
     * @param mapParams
     * @param rs
     */
    public void editorJson(Map<String, Object> mapParams, Response rs);

    /**
     * 保存模型
     *
     * @param mapParams
     * @param rs
     */
    public void saveModel(Map<String, Object> mapParams, Response rs);

    /**
     * 根据模型发布流程
     *
     * @param mapParams
     * @param rs
     */
    public void deploy(Map<String, Object> mapParams, Response rs);

    /**
     * 删除模型
     *
     * @param mapParams
     * @param rs
     */
    public void delete(Map<String, Object> mapParams, Response rs);

    /**
     * 模型数据导出
     *
     * @param mapParams
     * @param rs
     */
    public void export(Map<String, Object> mapParams, Response rs);

    /**
     * 模型的图片
     *
     * @param mapParams
     * @param rs
     */
    public void initImage(Map<String, Object> mapParams, Response rs);

    /**
     * 模型数据导出为文件流
     *
     * @param mapParams
     * @param rs
     */
    public void export(ServletRequest request, ServletResponse response, Map<String, Object> mapParams,
                       Response rs);

    /**
     * 导入模型
     *
     * @param mapParams
     * @param rs
     */
    public void modelImport(Map<String, Object> mapParams, Response rs);

    /**
     * 导入模型
     *
     * @param mapParams
     * @param rs
     */
    public void modelImportByLocalFile(Map<String, Object> mapParams, Response rs);
}
