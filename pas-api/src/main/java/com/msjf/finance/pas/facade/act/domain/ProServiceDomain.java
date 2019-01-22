package com.msjf.finance.pas.facade.act.domain;

import com.msjf.finance.msjf.core.domian.BaseDomain;

public class ProServiceDomain extends BaseDomain {

    private String  proDefKey;

    private String serviceFlag;

    public String getProDefKey() {
        return proDefKey;
    }

    public void setProDefKey(String proDefKey) {
        this.proDefKey = proDefKey;
    }

    public String getServiceFlag() {
        return serviceFlag;
    }

    public void setServiceFlag(String serviceFlag) {
        this.serviceFlag = serviceFlag;
    }
}
