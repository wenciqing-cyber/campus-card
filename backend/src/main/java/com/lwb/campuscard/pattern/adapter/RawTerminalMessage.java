package com.lwb.campuscard.pattern.adapter;

import java.util.Map;

/**
 * 终端原始报文：各终端上报格式不一样，先收进来再适配
 */
public class RawTerminalMessage {

    private String terminalNo;
    private String cardNo;
    private String payPwd;
    private String requestNo;
    /** POS 传 amount，水控传 volume，门禁可能什么都不传 */
    private Map<String, Object> payload;

    public String getTerminalNo() { return terminalNo; }

    public void setTerminalNo(String terminalNo) { this.terminalNo = terminalNo; }

    public String getCardNo() { return cardNo; }

    public void setCardNo(String cardNo) { this.cardNo = cardNo; }

    public String getPayPwd() { return payPwd; }

    public void setPayPwd(String payPwd) { this.payPwd = payPwd; }

    public String getRequestNo() { return requestNo; }

    public void setRequestNo(String requestNo) { this.requestNo = requestNo; }

    public Map<String, Object> getPayload() { return payload; }

    public void setPayload(Map<String, Object> payload) { this.payload = payload; }
}
