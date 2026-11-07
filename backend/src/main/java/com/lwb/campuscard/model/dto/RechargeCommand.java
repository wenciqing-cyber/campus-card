package com.lwb.campuscard.model.dto;

import java.math.BigDecimal;

/**
 * 充值请求
 */
public class RechargeCommand {

    private String cardNo;
    private BigDecimal amount;
    private String requestNo;
    private String remark;

    public String getCardNo() { return cardNo; }

    public void setCardNo(String cardNo) { this.cardNo = cardNo; }

    public BigDecimal getAmount() { return amount; }

    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public String getRequestNo() { return requestNo; }

    public void setRequestNo(String requestNo) { this.requestNo = requestNo; }

    public String getRemark() { return remark; }

    public void setRemark(String remark) { this.remark = remark; }
}
