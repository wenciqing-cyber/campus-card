package com.lwb.campuscard.pattern.observer;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 观察者之一：实时统计，首页大盘直接读这里，不必每次查库
 */
@Component
public class StatisticsObserver implements TradeObserver {

    private final AtomicInteger consumeCount = new AtomicInteger();
    private final AtomicReference<BigDecimal> consumeTotal = new AtomicReference<>(BigDecimal.ZERO);

    @Override
    public void onTrade(TradeEvent event) {
        if ("CONSUME".equals(event.getTrade().getTradeType())) {
            consumeCount.incrementAndGet();
            consumeTotal.updateAndGet(v -> v.add(event.getTrade().getPayAmount()));
        }
    }

    @Override
    public String name() {
        return "流量统计";
    }

    public int getConsumeCount() {
        return consumeCount.get();
    }

    public BigDecimal getConsumeTotal() {
        return consumeTotal.get();
    }
}
