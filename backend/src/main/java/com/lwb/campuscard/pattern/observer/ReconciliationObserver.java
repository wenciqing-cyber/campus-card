package com.lwb.campuscard.pattern.observer;

import com.lwb.campuscard.model.entity.Trade;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 观察者之一：实时对账
 * 内存里按终端累计当日消费额，日终结算时和流水表对一遍
 */
@Component
public class ReconciliationObserver implements TradeObserver {

    private final Map<String, BigDecimal> terminalAmount = new ConcurrentHashMap<>();
    private final AtomicInteger tradeCount = new AtomicInteger();

    @Override
    public void onTrade(TradeEvent event) {
        Trade trade = event.getTrade();
        if (!"CONSUME".equals(trade.getTradeType())) {
            return;
        }
        tradeCount.incrementAndGet();
        terminalAmount.merge(trade.getTerminalNo() == null ? "UNKNOWN" : trade.getTerminalNo(),
                trade.getPayAmount(), BigDecimal::add);
    }

    @Override
    public String name() {
        return "实时对账";
    }

    public Map<String, BigDecimal> snapshot() {
        return Map.copyOf(terminalAmount);
    }

    public int count() {
        return tradeCount.get();
    }
}
