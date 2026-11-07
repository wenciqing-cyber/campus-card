package com.lwb.campuscard.pattern.decorator;

import com.lwb.campuscard.model.dto.PricingRequest;
import com.lwb.campuscard.model.dto.PricingResult;
import com.lwb.campuscard.pattern.strategy.PricingStrategy;
import com.lwb.campuscard.pattern.support.Money;

import java.math.BigDecimal;

/**
 * 优惠封顶：无论怎么叠加，总优惠不超过上限，防止规则冲突导致亏损
 */
public class CapDecorator extends PricingDecorator {

    private final BigDecimal cap;

    public CapDecorator(PricingStrategy delegate, BigDecimal cap) {
        super(delegate);
        this.cap = Money.of(cap);
    }

    @Override
    public String code() {
        return delegate.code() + "+CAP";
    }

    @Override
    protected PricingResult apply(PricingResult inner, PricingRequest request) {
        BigDecimal discount = Money.of(inner.getOriginAmount().subtract(inner.getPayAmount()));
        if (discount.compareTo(cap) <= 0) {
            return inner;
        }
        BigDecimal pay = Money.atLeastCent(inner.getOriginAmount().subtract(cap));
        inner.setPayAmount(pay);
        inner.setDiscountAmount(cap);
        inner.setStrategyCode(code());
        inner.setDescription(inner.getDescription() + "（优惠封顶 " + cap.toPlainString() + " 元）");
        return inner;
    }
}
