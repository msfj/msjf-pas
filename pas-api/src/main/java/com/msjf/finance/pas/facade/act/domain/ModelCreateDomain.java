package com.msjf.finance.pas.facade.act.domain;


import com.msjf.finance.msjf.core.domian.BaseDomain;

public class ModelCreateDomain extends BaseDomain {
    private String name;
    private String description;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
