package com.msjf.finance.pas.facade.act.domain;

import com.msjf.finance.msjf.core.domian.BaseDomain;

public class ProDeployPageDomain extends BaseDomain {
    private String state;
    private String lastVersion;
    private Integer pageSize;
    private Integer pageNumber;
    private String name;

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getLastVersion() {
        return lastVersion;
    }

    public void setLastVersion(String lastVersion) {
        this.lastVersion = lastVersion;
    }

    public Integer getPageSize() {
        return pageSize;
    }

    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }

    public Integer getPageNumber() {
        return pageNumber;
    }

    public void setPageNumber(Integer pageNumber) {
        this.pageNumber = pageNumber;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
