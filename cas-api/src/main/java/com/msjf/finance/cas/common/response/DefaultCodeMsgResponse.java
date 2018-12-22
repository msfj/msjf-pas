package com.msjf.finance.cas.common.response;

/**
 * Created by 11509 on 2018/12/19.
 */
public class DefaultCodeMsgResponse implements ResponseService{

    private String responseCode;
    private String responseMessage;

    public DefaultCodeMsgResponse(String responseCode, String responseMessage) {
        this.responseCode = responseCode;
        this.responseMessage = responseMessage;
    }


    public String getResponseCode() {
        return this.responseCode;
    }

    public String getResponseMessage() {
        return this.responseMessage;
    }
}
