package com.msjf.finance.pas.bpm.entity;


import com.msjf.finance.msjf.core.entity.BaseEntity;

public class ProStepAuditEntity  extends BaseEntity {
    private String proDefKey;
    private String proDefName;
    private String stepId;
    private String stepName;
    private String auditorId;
    private String auditorName;
    private String auditorType;
    private String areaNo;

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

    public String getStepId() {
        return stepId;
    }

    public void setStepId(String stepId) {
        this.stepId = stepId;
    }

    public String getStepName() {
        return stepName;
    }

    public void setStepName(String stepName) {
        this.stepName = stepName;
    }

    public String getAuditorId() {
        return auditorId;
    }

    public void setAuditorId(String auditorId) {
        this.auditorId = auditorId;
    }

    public String getAuditorName() {
        return auditorName;
    }

    public void setAuditorName(String auditorName) {
        this.auditorName = auditorName;
    }

    public String getAuditorType() {
        return auditorType;
    }

    public void setAuditorType(String auditorType) {
        this.auditorType = auditorType;
    }

    public String getAreaNo() {
        return areaNo;
    }

    public void setAreaNo(String areaNo) {
        this.areaNo = areaNo;
    }
}
