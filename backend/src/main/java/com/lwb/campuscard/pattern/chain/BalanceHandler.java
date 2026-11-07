package com.lwb.campuscard.pattern.chain;

import com.lwb.campuscard.common.BizException;
import com.lwb.campuscard.common.ErrorCode;
import org.springframework.stereotype.Component;

/**
 * 第五环：余额是否够（先查一次，真正的原子扣减在最后一环）
 */
@Component
public class BalanceHandler extends PayHandler {

    @Override
    protected void check(PayContext ctx) {
        if (ctx.getAccount().getBalance().compareTo(ctx.getPayAmount()) < 0) {
            throw new BizException(ErrorCode.BALANCE_NOT_ENOUGH,
                    "余额不足，当前余额 " + ctx.getAccount().getBalance().toPlainString() + " 元");
        }
    }
}
