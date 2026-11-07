package com.lwb.campuscard.pattern.chain;

import com.lwb.campuscard.common.BizException;
import com.lwb.campuscard.common.ErrorCode;
import com.lwb.campuscard.dao.AccountMapper;
import org.springframework.stereotype.Component;

/**
 * 末环：真正扣钱
 * 用带条件的 UPDATE 做原子扣减，靠数据库行锁保证并发下不会扣成负数
 */
@Component
public class DeductHandler extends PayHandler {

    private final AccountMapper accountMapper;

    public DeductHandler(AccountMapper accountMapper) {
        this.accountMapper = accountMapper;
    }

    @Override
    protected void check(PayContext ctx) {
        int rows = accountMapper.deduct(ctx.getAccount().getId(), ctx.getPayAmount());
        if (rows == 0) {
            // 走到这说明前面的余额检查通过后，被别的请求抢先扣走了
            throw new BizException(ErrorCode.BALANCE_NOT_ENOUGH, "余额不足（并发扣减失败，请重试）");
        }
    }
}
