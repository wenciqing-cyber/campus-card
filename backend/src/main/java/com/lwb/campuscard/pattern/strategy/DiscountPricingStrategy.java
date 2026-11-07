package com.lwb.campuscard.pattern.strategy;

import com.lwb.campuscard.model.dto.PricingRequest;
import com.lwb.campuscard.model.dto.PricingResult;
import com.lwb.campuscard.pattern.support.Money;

import java.math.BigDecimal;
import java.math.RoundingMode;
import org.springframework.stereotype.Component;

/**
 * 折扣扣款：params 里放折扣率，如 0.85
 */
@Component
public class DiscountPricingStrategy implements PricingStrategy {

    @Override
    public String code() {
        return "DISCOUNT";
    }

    @Override
    public PricingResult calculate(PricingRequest request) {
        BigDecimal rate = new BigDecimal(request.getParams() == null ? "1.0" : request.getParams());
        BigDecimal pay = request.getOriginAmount().multiply(rate).setScale(2, RoundingMode.HALF_UP);
        // 折后为 0 视为异常，最低留一分
        pay = Money.atLeastCent(pay);
        return PricingResult.of(request.getOriginAmount(), pay, code(),
                "折扣 " + rate.multiply(BigDecimal.TEN).multiply(BigDecimal.TEN).stripTrailingZeros().toPlainString() + " 折");
    }
}
