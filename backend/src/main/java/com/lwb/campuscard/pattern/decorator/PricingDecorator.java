package com.lwb.campuscard.pattern.decorator;

import com.lwb.campuscard.model.dto.PricingRequest;
import com.lwb.campuscard.model.dto.PricingResult;
import com.lwb.campuscard.pattern.strategy.PricingStrategy;

/**
 * 装饰器基类：在已有优惠结果上继续加工
 * 和策略的区别——策略是"选一种算法"，装饰是"在结果上叠加若干层"
 */
public abstract class PricingDecorator implements PricingStrategy {

    protected final PricingStrategy delegate;

    protected PricingDecorator(PricingStrategy delegate) {
        this.delegate = delegate;
    }

    @Override
    public PricingResult calculate(PricingRequest request) {
        PricingResult inner = delegate.calculate(request);
        // 内层算完，外层在这基础上继续改
        return apply(inner, request);
    }

    protected abstract PricingResult apply(PricingResult inner, PricingRequest request);
}
