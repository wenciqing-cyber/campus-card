package com.lwb.campuscard.pattern.strategy;

import com.lwb.campuscard.model.dto.PricingRequest;
import com.lwb.campuscard.model.dto.PricingResult;
import com.lwb.campuscard.model.entity.DiscountRule;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 策略上下文：按规则类型挑策略，调用方不再写 if-else
 */
@Component
public class PricingContext {

    private final Map<String, PricingStrategy> strategyMap = new HashMap<>();

    public PricingContext(List<PricingStrategy> strategies) {
        for (PricingStrategy s : strategies) {
            strategyMap.put(s.code(), s);
        }
    }

    public PricingStrategy get(String code) {
        PricingStrategy s = strategyMap.get(code);
        return s == null ? strategyMap.get("NORMAL") : s;
    }

    public PricingResult price(DiscountRule rule, PricingRequest request) {
        PricingStrategy strategy = get(rule.getRuleType());
        PricingRequest req = new PricingRequest();
        req.setCardNo(request.getCardNo());
        req.setOriginAmount(request.getOriginAmount());
        req.setPoints(request.getPoints());
        req.setParams(rule.getParams());
        return strategy.calculate(req);
    }

    public Map<String, PricingStrategy> all() {
        return strategyMap;
    }
}
