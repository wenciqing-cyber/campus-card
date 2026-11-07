package com.lwb.campuscard.model.entity;

import java.math.BigDecimal;

/**
 * 终端实体
 */
public class Terminal {

    private Long id;
    private String terminalNo;
    private String terminalName;
    private String terminalType;
    private String merchant;
    /** 水控倍率，水控按用量计价时乘这个系数 */
    private BigDecimal rate;
    private Integer status;

    public Long getId() { return id; }

    public void setId(Long id) { this.id = id; }

    public String getTerminalNo() { return terminalNo; }

    public void setTerminalNo(String terminalNo) { this.terminalNo = terminalNo; }

    public String getTerminalName() { return terminalName; }

    public void setTerminalName(String terminalName) { this.terminalName = terminalName; }

    public String getTerminalType() { return terminalType; }

    public void setTerminalType(String terminalType) { this.terminalType = terminalType; }

    public String getMerchant() { return merchant; }

    public void setMerchant(String merchant) { this.merchant = merchant; }

    public BigDecimal getRate() { return rate; }

    public void setRate(BigDecimal rate) { this.rate = rate; }

    public Integer getStatus() { return status; }

    public void setStatus(Integer status) { this.status = status; }
}
