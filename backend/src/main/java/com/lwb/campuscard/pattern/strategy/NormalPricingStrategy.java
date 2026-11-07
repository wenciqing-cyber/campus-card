package com.lwb.campuscard.pattern.strategy;

import com.lwb.campuscard.model.dto.PricingRequest;
import com.lwb.campuscard.model.dto.PricingResult;
import com.lwb.campuscard.pattern.support.Money;
import org.springframework.stereotype.Component;

/**
 * 普通扣款：原价即实付
 */
@Component
public class NormalPricingStrategy implements PricingStrategy {

    @Override
    public String code() {
        return "NORMAL";
    }

    @Override
    public PricingResult calculate(PricingRequest request) {
        return PricingResult.of(request.getOriginAmount(), request.getOriginAmount(), code(), "普通扣款");
    }
}
