package com.lwb.campuscard.model.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 交易流水实体，金额字段只追加不修改
 */
public class Trade {

    private Long id;
    private String tradeNo;
    private String requestNo;
    private Long accountId;
    private String cardNo;
    private String tradeType;
    private String terminalNo;
    private BigDecimal originAmount;
    private BigDecimal discountAmount;
    private BigDecimal payAmount;
    private Integer pointsDelta;
    private String strategyCode;
    private Integer status;
    private String originTradeNo;
    private String remark;
    private LocalDateTime createdAt;

    public Long getId() { return id; }

    public void setId(Long id) { this.id = id; }

    public String getTradeNo() { return tradeNo; }

    public void setTradeNo(String tradeNo) { this.tradeNo = tradeNo; }

    public String getRequestNo() { return requestNo; }

    public void setRequestNo(String requestNo) { this.requestNo = requestNo; }

    public Long getAccountId() { return accountId; }

    public void setAccountId(Long accountId) { this.accountId = accountId; }

    public String getCardNo() { return cardNo; }

    public void setCardNo(String cardNo) { this.cardNo = cardNo; }

    public String getTradeType() { return tradeType; }

    public void setTradeType(String tradeType) { this.tradeType = tradeType; }

    public String getTerminalNo() { return terminalNo; }

    public void setTerminalNo(String terminalNo) { this.terminalNo = terminalNo; }

    public BigDecimal getOriginAmount() { return originAmount; }

    public void setOriginAmount(BigDecimal originAmount) { this.originAmount = originAmount; }

    public BigDecimal getDiscountAmount() { return discountAmount; }

    public void setDiscountAmount(BigDecimal discountAmount) { this.discountAmount = discountAmount; }

    public BigDecimal getPayAmount() { return payAmount; }

    public void setPayAmount(BigDecimal payAmount) { this.payAmount = payAmount; }

    public Integer getPointsDelta() { return pointsDelta; }

    public void setPointsDelta(Integer pointsDelta) { this.pointsDelta = pointsDelta; }

    public String getStrategyCode() { return strategyCode; }

    public void setStrategyCode(String strategyCode) { this.strategyCode = strategyCode; }

    public Integer getStatus() { return status; }

    public void setStatus(Integer status) { this.status = status; }

    public String getOriginTradeNo() { return originTradeNo; }

    public void setOriginTradeNo(String originTradeNo) { this.originTradeNo = originTradeNo; }

    public String getRemark() { return remark; }

    public void setRemark(String remark) { this.remark = remark; }

    public LocalDateTime getCreatedAt() { return createdAt; }

    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
