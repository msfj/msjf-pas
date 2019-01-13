package com.msjf.finance.pas.facade.organ.domain;


import com.msjf.finance.msjf.core.domian.BaseDomain;

public class CustInfoDomain extends BaseDomain {
    private String customerno; //客户号
    private String custno;  //企业号
    private String loginname;  //登录名

    public String getCustomerno() {
        return customerno;
    }

    public void setCustomerno(String customerno) {
        this.customerno = customerno;
    }

    public String getCustno() {
        return custno;
    }

    public void setCustno(String custno) {
        this.custno = custno;
    }

    public String getLoginname() {
        return loginname;
    }

    public void setLoginname(String loginname) {
        this.loginname = loginname;
    }
}
