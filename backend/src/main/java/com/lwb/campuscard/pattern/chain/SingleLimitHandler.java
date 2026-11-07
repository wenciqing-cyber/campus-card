package com.lwb.campuscard.pattern.chain;

import com.lwb.campuscard.common.BizException;
import com.lwb.campuscard.common.ErrorCode;
import com.lwb.campuscard.pattern.singleton.SystemConfig;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * 第三环：单笔限额，配置读单例
 */
@Component
public class SingleLimitHandler extends PayHandler {

    @Override
    protected void check(PayContext ctx) {
        BigDecimal limit = SystemConfig.getInstance().getSingleLimit();
        if (ctx.getPayAmount().compareTo(limit) > 0) {
            throw new BizException(ErrorCode.SINGLE_LIMIT_EXCEED, "单笔消费不得超过 " + limit.toPlainString() + " 元");
        }
    }
}
