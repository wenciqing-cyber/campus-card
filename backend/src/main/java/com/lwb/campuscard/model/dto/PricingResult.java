package com.lwb.campuscard.model.dto;

import java.math.BigDecimal;

/**
 * 定价结果，装饰器叠加时也是拿这个往上加
 */
public class PricingResult {

    private BigDecimal originAmount;
    private BigDecimal discountAmount;
    private BigDecimal payAmount;
    /** 积分变动：负数表示扣了积分 */
    private Integer pointsDelta;
    private String strategyCode;
    private String description;

    public static PricingResult of(BigDecimal origin, BigDecimal pay, String code, String desc) {
        PricingResult r = new PricingResult();
        r.setOriginAmount(origin);
        r.setPayAmount(pay);
        r.setDiscountAmount(origin.subtract(pay));
        r.setPointsDelta(0);
        r.setStrategyCode(code);
        r.setDescription(desc);
        return r;
    }

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

    public String getDescription() { return description; }

    public void setDescription(String description) { this.description = description; }
}
