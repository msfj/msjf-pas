package com.msjf.finance.pas.facade.act.domain;

import com.msjf.finance.pas.common.response.BaseDomain;

public class ModelCopyDomain extends BaseDomain {
    private String name;
    private String oldModelId;
    private String description;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getOldModelId() {
        return oldModelId;
    }

    public void setOldModelId(String oldModelId) {
        this.oldModelId = oldModelId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
