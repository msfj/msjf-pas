package com.msjf.finance.pas.bpm.entity;


import com.msjf.finance.msjf.core.entity.BaseEntity;

public class ProServiceEntity extends BaseEntity {
    private String proDefKey;
    private String serviceName;
    private String serviceFlag;

    public String getProDefKey() {
        return proDefKey;
    }

    public void setProDefKey(String proDefKey) {
        this.proDefKey = proDefKey;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getServiceFlag() {
        return serviceFlag;
    }

    public void setServiceFlag(String serviceFlag) {
        this.serviceFlag = serviceFlag;
    }
}
