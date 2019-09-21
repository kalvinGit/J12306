package com.kalvin.J12306.dto;

/**
 * 提交订单相关参数
 * Create by Kalvin on 2019/9/20.
 */
public class SubmitTicketInfoDTO {

    private String repeatSubmitToken; // 提交订单token
    private String keyCheckIsChange;
    private String purposeCodes;
    private String leftTicketStr;   // 车票标识

    public String getRepeatSubmitToken() {
        return repeatSubmitToken;
    }

    public void setRepeatSubmitToken(String repeatSubmitToken) {
        this.repeatSubmitToken = repeatSubmitToken;
    }

    public String getKeyCheckIsChange() {
        return keyCheckIsChange;
    }

    public void setKeyCheckIsChange(String keyCheckIsChange) {
        this.keyCheckIsChange = keyCheckIsChange;
    }

    public String getPurposeCodes() {
        return purposeCodes;
    }

    public void setPurposeCodes(String purposeCodes) {
        this.purposeCodes = purposeCodes;
    }

    public String getLeftTicketStr() {
        return leftTicketStr;
    }

    public void setLeftTicketStr(String leftTicketStr) {
        this.leftTicketStr = leftTicketStr;
    }

    @Override
    public String toString() {
        return "SubmitTicketInfoDTO{" +
                "repeatSubmitToken='" + repeatSubmitToken + '\'' +
                ", keyCheckIsChange='" + keyCheckIsChange + '\'' +
                ", purposeCodes='" + purposeCodes + '\'' +
                ", leftTicketStr='" + leftTicketStr + '\'' +
                '}';
    }
}
