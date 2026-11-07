package com.lwb.campuscard.model.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 日终结算记录
 */
public class Settlement {

    private Long id;
    private LocalDate settleDate;
    private String terminalNo;
    private Integer tradeCount;
    private BigDecimal totalAmount;
    private BigDecimal refundAmount;
    private Integer status;
    private LocalDateTime createdAt;

    public Long getId() { return id; }

    public void setId(Long id) { this.id = id; }

    public LocalDate getSettleDate() { return settleDate; }

    public void setSettleDate(LocalDate settleDate) { this.settleDate = settleDate; }

    public String getTerminalNo() { return terminalNo; }

    public void setTerminalNo(String terminalNo) { this.terminalNo = terminalNo; }

    public Integer getTradeCount() { return tradeCount; }

    public void setTradeCount(Integer tradeCount) { this.tradeCount = tradeCount; }

    public BigDecimal getTotalAmount() { return totalAmount; }

    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }

    public BigDecimal getRefundAmount() { return refundAmount; }

    public void setRefundAmount(BigDecimal refundAmount) { this.refundAmount = refundAmount; }

    public Integer getStatus() { return status; }

    public void setStatus(Integer status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }

    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
