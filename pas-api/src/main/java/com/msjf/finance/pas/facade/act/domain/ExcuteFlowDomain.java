package com.msjf.finance.pas.facade.act.domain;

import com.msjf.finance.msjf.core.domian.BaseDomain;

public class ExcuteFlowDomain extends BaseDomain {

    /**
     * 流程实例
     */
    private String processInstanceId;

    /**
     * 当前用户ID
     */
    private String userId;

    /**
     * 企业名称
     */
    private String custName;



    /**
     * 企业客户号
     */
    private String custNo;


    /**
     * 当前用户名称
     */
    private String userName;

    /**
     * 审核意见
     */
    private String comment;


    /**
     * 任务id
     */
    private String taskId;



    /**
     * 审核
     */
    private String approve;

    /**
     * 附件
     *
     */
    private String fileUrls;


    /**
     * 审核当前步骤id
     */
    private String taskDefinitionKey;


    public String getProcessInstanceId() {
        return processInstanceId;
    }

    public void setProcessInstanceId(String processInstanceId) {
        this.processInstanceId = processInstanceId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getCustName() {
        return custName;
    }

    public void setCustName(String custName) {
        this.custName = custName;
    }

    public String getCustNo() {
        return custNo;
    }

    public void setCustNo(String custNo) {
        this.custNo = custNo;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public String getApprove() {
        return approve;
    }

    public void setApprove(String approve) {
        this.approve = approve;
    }

    public String getTaskDefinitionKey() {
        return taskDefinitionKey;
    }

    public void setTaskDefinitionKey(String taskDefinitionKey) {
        this.taskDefinitionKey = taskDefinitionKey;
    }

    public String getFileUrls() {
        return fileUrls;
    }

    public void setFileUrls(String fileUrls) {
        this.fileUrls = fileUrls;
    }
}
