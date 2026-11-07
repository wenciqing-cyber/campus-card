package com.lwb.campuscard.pattern.chain;

import com.lwb.campuscard.common.BizException;
import com.lwb.campuscard.common.ErrorCode;
import com.lwb.campuscard.dao.TradeMapper;
import com.lwb.campuscard.pattern.singleton.SystemConfig;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * 第四环：当日累计限额，防止卡被刷爆
 */
@Component
public class DailyLimitHandler extends PayHandler {

    private final TradeMapper tradeMapper;

    public DailyLimitHandler(TradeMapper tradeMapper) {
        this.tradeMapper = tradeMapper;
    }

    @Override
    protected void check(PayContext ctx) {
        BigDecimal spentToday = tradeMapper.sumConsumeToday(ctx.getAccount().getId());
        BigDecimal limit = SystemConfig.getInstance().getDailyLimit();
        if (spentToday.add(ctx.getPayAmount()).compareTo(limit) > 0) {
            throw new BizException(ErrorCode.DAILY_LIMIT_EXCEED,
                    "今日累计消费将超过 " + limit.toPlainString() + " 元，当前已消费 " + spentToday.toPlainString() + " 元");
        }
    }
}
