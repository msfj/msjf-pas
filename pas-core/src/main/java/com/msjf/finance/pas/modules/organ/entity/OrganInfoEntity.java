package com.msjf.finance.pas.modules.organ.entity;

import com.msjf.finance.msjf.core.entity.BaseEntity;

/**
 * Created by 11509 on 2018/12/18.
 */
public class OrganInfoEntity  extends BaseEntity {
    private String customerno; //客户号
    private String membername;  //企业名称
    private String organtype;  //企业类型
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
