package com.msjf.finance.pas.modules.organ.entity;

import com.msjf.finance.pas.common.response.BaseEntity;

public class CustEntity extends BaseEntity {
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
