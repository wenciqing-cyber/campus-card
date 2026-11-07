package com.lwb.campuscard.pattern.chain;

import org.springframework.stereotype.Component;

/**
 * 责任链装配：顺序写在这里，调顺序只改这一处
 * 卡状态 → 密码 → 单笔限额 → 日累计限额 → 余额 → 扣款
 */
@Component
public class PayChainFactory {

    private final PayHandler head;

    public PayChainFactory(CardStatusHandler cardStatus,
                           PasswordHandler password,
                           SingleLimitHandler singleLimit,
                           DailyLimitHandler dailyLimit,
                           BalanceHandler balance,
                           DeductHandler deduct) {
        cardStatus.next(password)
                .next(singleLimit)
                .next(dailyLimit)
                .next(balance)
                .next(deduct);
        this.head = cardStatus;
    }

    public void execute(PayContext ctx) {
        head.doHandle(ctx);
    }
}
