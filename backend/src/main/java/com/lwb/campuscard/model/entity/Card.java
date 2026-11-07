package com.lwb.campuscard.model.entity;

import java.time.LocalDateTime;

/**
 * 卡片实体
 */
public class Card {

    private Long id;
    private String cardNo;
    private Long accountId;
    private Integer cardStatus;
    private String payPwd;
    private LocalDateTime issuedAt;

    public Long getId() { return id; }

    public void setId(Long id) { this.id = id; }

    public String getCardNo() { return cardNo; }

    public void setCardNo(String cardNo) { this.cardNo = cardNo; }

    public Long getAccountId() { return accountId; }

    public void setAccountId(Long accountId) { this.accountId = accountId; }

    public Integer getCardStatus() { return cardStatus; }

    public void setCardStatus(Integer cardStatus) { this.cardStatus = cardStatus; }

    public String getPayPwd() { return payPwd; }

    public void setPayPwd(String payPwd) { this.payPwd = payPwd; }

    public LocalDateTime getIssuedAt() { return issuedAt; }

    public void setIssuedAt(LocalDateTime issuedAt) { this.issuedAt = issuedAt; }
}
