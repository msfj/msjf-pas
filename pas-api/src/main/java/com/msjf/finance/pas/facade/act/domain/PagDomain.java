package com.msjf.finance.pas.facade.act.domain;

import com.msjf.finance.msjf.core.domian.BaseDomain;

public class PagDomain extends BaseDomain {
    private Integer pageSize;
    private Integer pageNumber;

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
}
