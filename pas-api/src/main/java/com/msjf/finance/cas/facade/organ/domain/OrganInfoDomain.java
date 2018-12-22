package com.msjf.finance.cas.facade.organ.domain;

import com.msjf.finance.cas.common.response.BaseDomain;

/**
 * Created by 11509 on 2018/12/18.
 */
public class OrganInfoDomain  extends BaseDomain {

    private String customerno;//客户号
    private String membername;//企业名称
    private String organtype;//企业类型

    public String getCustomerno() {
        return customerno;
    }

    public void setCustomerno(String customerno) {
        this.customerno = customerno;
    }

    public String getMembername() {
        return membername;
    }

    public void setMembername(String membername) {
        this.membername = membername;
    }

    public String getOrgantype() {
        return organtype;
    }

    public void setOrgantype(String organtype) {
        this.organtype = organtype;
    }
}
