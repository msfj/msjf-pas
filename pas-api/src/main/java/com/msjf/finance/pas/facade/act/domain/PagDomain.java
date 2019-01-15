package com.msjf.finance.pas.facade.act.domain;

import com.msjf.finance.pas.common.response.BaseDomain;

public class PagDomain extends BaseDomain {
    private String pageSize;
    private String pageNumber;

    public String getPageSize() {
        return pageSize;
    }

    public void setPageSize(String pageSize) {
        this.pageSize = pageSize;
    }

    public String getPageNumber() {
        return pageNumber;
    }

    public void setPageNumber(String pageNumber) {
        this.pageNumber = pageNumber;
    }
}
