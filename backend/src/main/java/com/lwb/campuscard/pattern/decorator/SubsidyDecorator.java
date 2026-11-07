package com.lwb.campuscard.pattern.decorator;

import com.lwb.campuscard.model.dto.PricingRequest;
import com.lwb.campuscard.model.dto.PricingResult;
import com.lwb.campuscard.pattern.strategy.PricingStrategy;
import com.lwb.campuscard.pattern.support.Money;

import java.math.BigDecimal;

/**
 * 叠加定额补贴：比如已经打了折，早餐再补 2 元
 */
public class SubsidyDecorator extends PricingDecorator {

    private final BigDecimal amount;

    public SubsidyDecorator(PricingStrategy delegate, BigDecimal amount) {
        super(delegate);
        this.amount = Money.of(amount);
    }

    @Override
    public String code() {
        return delegate.code() + "+SUBSIDY";
    }

    @Override
    protected PricingResult apply(PricingResult inner, PricingRequest request) {
        BigDecimal pay = Money.atLeastCent(inner.getPayAmount().subtract(amount));
        inner.setPayAmount(pay);
        inner.setDiscountAmount(Money.of(inner.getOriginAmount().subtract(pay)));
        inner.setStrategyCode(code());
        inner.setDescription(inner.getDescription() + "，再补贴 " + amount.toPlainString() + " 元");
        return inner;
    }
}
