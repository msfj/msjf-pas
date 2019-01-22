package com.msjf.finance.pas.facade.act.domain;

import com.msjf.finance.msjf.core.domian.BaseDomain;

import java.util.List;
import java.util.Map;

public class ProStepAuditDomain extends BaseDomain {

    private List<Map> list;

    public List<Map> getList() {
        return list;
    }

    public void setList(List<Map> list) {
        this.list = list;
    }
}
