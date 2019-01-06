package com.msjf.finance.pas.bpm.entity;



/**
 * Created by chengjunping on 2019/1/4.
 */
public class CustProStateEntity  {

    private String id;
    private String custNo;
    private String cust_Name;
    private String proInstance;
    private String proDefKey;
    private String proDefName;
    private String startTime;
    private String endTime;
    private String auditResult;
    private String proSate;


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCustNo() {
        return custNo;
    }

    public void setCustNo(String custNo) {
        this.custNo = custNo;
    }

    public String getCust_Name() {
        return cust_Name;
    }

    public void setCust_Name(String cust_Name) {
        this.cust_Name = cust_Name;
    }

    public String getProInstance() {
        return proInstance;
    }

    public void setProInstance(String proInstance) {
        this.proInstance = proInstance;
    }

    public String getProDefKey() {
        return proDefKey;
    }

    public void setProDefKey(String proDefKey) {
        this.proDefKey = proDefKey;
    }

    public String getProDefName() {
        return proDefName;
    }

    public void setProDefName(String proDefName) {
        this.proDefName = proDefName;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public String getAuditResult() {
        return auditResult;
    }

    public void setAuditResult(String auditResult) {
        this.auditResult = auditResult;
    }

    public String getProSate() {
        return proSate;
    }

    public void setProSate(String proSate) {
        this.proSate = proSate;
    }
}
