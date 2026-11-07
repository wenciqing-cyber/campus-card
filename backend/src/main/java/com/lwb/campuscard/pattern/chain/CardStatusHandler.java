package com.lwb.campuscard.pattern.chain;

import com.lwb.campuscard.common.BizException;
import com.lwb.campuscard.common.ErrorCode;
import com.lwb.campuscard.model.enums.CardStatus;
import org.springframework.stereotype.Component;

/**
 * 第一环：卡片状态。挂失/注销直接拒
 */
@Component
public class CardStatusHandler extends PayHandler {

    @Override
    protected void check(PayContext ctx) {
        if (ctx.getCard() == null) {
            throw new BizException(ErrorCode.ACCOUNT_NOT_FOUND, "卡号不存在");
        }
        // 卡面状态和账户状态任一异常都不放行：销户只改账户时也不能漏
        CardStatus[] states = {
                CardStatus.of(ctx.getCard().getCardStatus()),
                CardStatus.of(ctx.getAccount() == null ? null : ctx.getAccount().getStatus())
        };
        for (CardStatus s : states) {
            if (s == CardStatus.LOST) {
                throw new BizException(ErrorCode.CARD_LOST, "该卡已挂失，无法交易");
            }
            if (s == CardStatus.CANCELLED) {
                throw new BizException(ErrorCode.CARD_CANCELLED, "该卡已注销");
            }
        }
    }
}
