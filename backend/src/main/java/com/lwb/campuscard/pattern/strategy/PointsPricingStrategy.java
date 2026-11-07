package com.lwb.campuscard.pattern.strategy;

import com.lwb.campuscard.model.dto.PricingRequest;
import com.lwb.campuscard.model.dto.PricingResult;
import com.lwb.campuscard.pattern.support.Money;

import java.math.BigDecimal;
import java.math.RoundingMode;
import org.springframework.stereotype.Component;

/**
 * 积分抵扣：params 是"多少积分抵 1 元"，如 100
 */
@Component
public class PointsPricingStrategy implements PricingStrategy {

    @Override
    public String code() {
        return "POINTS";
    }

    @Override
    public PricingResult calculate(PricingRequest request) {
        int perYuan = Integer.parseInt(request.getParams() == null ? "100" : request.getParams());
        int owned = request.getPoints() == null ? 0 : request.getPoints();

        // 最多能抵扣多少分（按积分折算）
        BigDecimal maxOffset = BigDecimal.valueOf(owned)
                .divide(BigDecimal.valueOf(perYuan), 2, RoundingMode.DOWN);
        // 不能抵到比原价还低，留一分
        BigDecimal ceilOffset = Money.atLeastCent(request.getOriginAmount()).subtract(new BigDecimal("0.01"));
        BigDecimal offset = maxOffset.min(ceilOffset);
        if (offset.compareTo(BigDecimal.ZERO) < 0) {
            offset = BigDecimal.ZERO;
        }

        BigDecimal pay = request.getOriginAmount().subtract(offset).setScale(2, RoundingMode.HALF_UP);
        int usedPoints = offset.multiply(BigDecimal.valueOf(perYuan)).intValue();

        PricingResult result = PricingResult.of(request.getOriginAmount(), pay, code(),
                "积分抵扣 " + offset.toPlainString() + " 元");
        result.setPointsDelta(-usedPoints);
        return result;
    }
}
