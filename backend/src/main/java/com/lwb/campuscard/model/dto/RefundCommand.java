package com.lwb.campuscard.model.dto;

/**
 * 退款冲正请求：按原交易单号退
 */
public class RefundCommand {

    private String originTradeNo;
    private String requestNo;
    private String remark;

    public String getOriginTradeNo() { return originTradeNo; }

    public void setOriginTradeNo(String originTradeNo) { this.originTradeNo = originTradeNo; }

    public String getRequestNo() { return requestNo; }

    public void setRequestNo(String requestNo) { this.requestNo = requestNo; }

    public String getRemark() { return remark; }

    public void setRemark(String remark) { this.remark = remark; }
}
