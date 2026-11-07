package com.lwb.campuscard.model.dto;

import java.util.Map;

/**
 * 消费请求，前端/终端传过来的原始参数
 */
public class ConsumeCommand {

    private String terminalNo;
    private String cardNo;
    private String payPwd;
    private String requestNo;
    /** 指定优惠规则编码，不传就按终端默认 */
    private String ruleCode;
    /** 终端原始字段：POS 用 amount，水控用 volume */
    private Map<String, Object> payload;
    private String remark;

    public String getTerminalNo() { return terminalNo; }

    public void setTerminalNo(String terminalNo) { this.terminalNo = terminalNo; }

    public String getCardNo() { return cardNo; }

    public void setCardNo(String cardNo) { this.cardNo = cardNo; }

    public String getPayPwd() { return payPwd; }

    public void setPayPwd(String payPwd) { this.payPwd = payPwd; }

    public String getRequestNo() { return requestNo; }

    public void setRequestNo(String requestNo) { this.requestNo = requestNo; }

    public String getRuleCode() { return ruleCode; }

    public void setRuleCode(String ruleCode) { this.ruleCode = ruleCode; }

    public Map<String, Object> getPayload() { return payload; }

    public void setPayload(Map<String, Object> payload) { this.payload = payload; }

    public String getRemark() { return remark; }

    public void setRemark(String remark) { this.remark = remark; }
}
