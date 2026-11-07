package com.lwb.campuscard.pattern.decorator;

import com.lwb.campuscard.model.dto.PricingRequest;
import com.lwb.campuscard.model.dto.PricingResult;
import com.lwb.campuscard.pattern.strategy.PricingStrategy;
import com.lwb.campuscard.pattern.support.Money;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * 叠加积分抵扣：折后金额再拿积分抵
 */
public class PointsDecorator extends PricingDecorator {

    private final int pointsPerYuan;

    public PointsDecorator(PricingStrategy delegate, int pointsPerYuan) {
        super(delegate);
        this.pointsPerYuan = pointsPerYuan;
    }

    @Override
    public String code() {
        return delegate.code() + "+POINTS";
    }

    @Override
    protected PricingResult apply(PricingResult inner, PricingRequest request) {
        int owned = request.getPoints() == null ? 0 : request.getPoints();
        BigDecimal maxOffset = BigDecimal.valueOf(owned)
                .divide(BigDecimal.valueOf(pointsPerYuan), 2, RoundingMode.DOWN);
        BigDecimal ceil = Money.atLeastCent(inner.getPayAmount()).subtract(new BigDecimal("0.01"));
        BigDecimal offset = maxOffset.min(ceil);
        if (offset.compareTo(BigDecimal.ZERO) <= 0) {
            return inner;
        }
        BigDecimal pay = Money.atLeastCent(inner.getPayAmount().subtract(offset));
        int used = offset.multiply(BigDecimal.valueOf(pointsPerYuan)).intValue();

        inner.setPayAmount(pay);
        inner.setDiscountAmount(Money.of(inner.getOriginAmount().subtract(pay)));
        inner.setPointsDelta(inner.getPointsDelta() - used);
        inner.setStrategyCode(code());
        inner.setDescription(inner.getDescription() + "，积分抵 " + offset.toPlainString() + " 元");
        return inner;
    }
}
