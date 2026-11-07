package com.lwb.campuscard.pattern.adapter;

import com.lwb.campuscard.model.enums.TerminalType;

import java.math.BigDecimal;

/**
 * 适配后的统一支付请求，后面的业务只认这个
 */
public class PayRequest {

    private String terminalNo;
    private TerminalType terminalType;
    private String cardNo;
    private String payPwd;
    private String requestNo;
    private BigDecimal amount;
    /** 适配过程产生的说明，比如"20 升 × 0.5 元/升" */
    private String adaptNote;

    public String getTerminalNo() { return terminalNo; }

    public void setTerminalNo(String terminalNo) { this.terminalNo = terminalNo; }

    public TerminalType getTerminalType() { return terminalType; }

    public void setTerminalType(TerminalType terminalType) { this.terminalType = terminalType; }

    public String getCardNo() { return cardNo; }

    public void setCardNo(String cardNo) { this.cardNo = cardNo; }

    public String getPayPwd() { return payPwd; }

    public void setPayPwd(String payPwd) { this.payPwd = payPwd; }

    public String getRequestNo() { return requestNo; }

    public void setRequestNo(String requestNo) { this.requestNo = requestNo; }

    public BigDecimal getAmount() { return amount; }

    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public String getAdaptNote() { return adaptNote; }

    public void setAdaptNote(String adaptNote) { this.adaptNote = adaptNote; }
}
