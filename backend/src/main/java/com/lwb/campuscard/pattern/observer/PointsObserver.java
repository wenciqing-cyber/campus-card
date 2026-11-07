package com.lwb.campuscard.pattern.observer;

import com.lwb.campuscard.dao.AccountMapper;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * 观察者之一：消费送积分，1 元 = 1 分，向下取整
 */
@Component
public class PointsObserver implements TradeObserver {

    private final AccountMapper accountMapper;

    public PointsObserver(AccountMapper accountMapper) {
        this.accountMapper = accountMapper;
    }

    @Override
    public void onTrade(TradeEvent event) {
        if (!"CONSUME".equals(event.getTrade().getTradeType())) {
            return;
        }
        BigDecimal pay = event.getTrade().getPayAmount();
        if (pay.compareTo(BigDecimal.ONE) < 0) {
            return;
        }
        int gain = pay.intValue();
        if (gain > 0) {
            accountMapper.addPoints(event.getAccount().getId(), gain);
        }
    }

    @Override
    public String name() {
        return "积分累计";
    }
}
