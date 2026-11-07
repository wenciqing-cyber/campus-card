package com.lwb.campuscard.pattern.strategy;

import com.lwb.campuscard.model.dto.PricingRequest;
import com.lwb.campuscard.model.dto.PricingResult;
import com.lwb.campuscard.pattern.support.Money;

import java.math.BigDecimal;
import org.springframework.stereotype.Component;

/**
 * 定额补贴：params 里放补贴金额，如 2.00，直接减
 */
@Component
public class SubsidyPricingStrategy implements PricingStrategy {

    @Override
    public String code() {
        return "SUBSIDY";
    }

    @Override
    public PricingResult calculate(PricingRequest request) {
        BigDecimal sub = new BigDecimal(request.getParams() == null ? "0" : request.getParams());
        BigDecimal pay = Money.atLeastCent(request.getOriginAmount().subtract(sub));
        return PricingResult.of(request.getOriginAmount(), pay, code(), "补贴 " + sub.toPlainString() + " 元");
    }
}
