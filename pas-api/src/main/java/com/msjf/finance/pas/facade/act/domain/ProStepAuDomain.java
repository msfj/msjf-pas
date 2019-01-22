package com.msjf.finance.pas.facade.act.domain;

import com.msjf.finance.msjf.core.domian.BaseDomain;

public class ProStepAuDomain extends BaseDomain {
    private String proDefKey;
    private String stepId;
    private String areaNo;

    public String getProDefKey() {
        return proDefKey;
    }

    public void setProDefKey(String proDefKey) {
        this.proDefKey = proDefKey;
    }

    public String getStepId() {
        return stepId;
    }

    public void setStepId(String stepId) {
        this.stepId = stepId;
    }

    public String getAreaNo() {
        return areaNo;
    }

    public void setAreaNo(String areaNo) {
        this.areaNo = areaNo;
    }
}
