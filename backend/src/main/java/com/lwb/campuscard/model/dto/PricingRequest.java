package com.lwb.campuscard.model.dto;

import java.math.BigDecimal;

/**
 * 定价入参：策略计算只依赖这些，不碰数据库
 */
public class PricingRequest {

    private String cardNo;
    private BigDecimal originAmount;
    /** 账户当前积分，积分抵扣策略要读 */
    private Integer points;
    /** 规则里的参数串，比如 0.85 / 2.00 / 100 */
    private String params;

    public String getCardNo() { return cardNo; }

    public void setCardNo(String cardNo) { this.cardNo = cardNo; }

    public BigDecimal getOriginAmount() { return originAmount; }

    public void setOriginAmount(BigDecimal originAmount) { this.originAmount = originAmount; }

    public Integer getPoints() { return points; }

    public void setPoints(Integer points) { this.points = points; }

    public String getParams() { return params; }

    public void setParams(String params) { this.params = params; }
}
