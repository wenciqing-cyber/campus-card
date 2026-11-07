package com.lwb.campuscard.pattern.factory;

import com.lwb.campuscard.common.BizException;
import com.lwb.campuscard.dao.AccountMapper;
import com.lwb.campuscard.model.enums.TradeType;

import java.math.BigDecimal;
import org.springframework.stereotype.Component;

/**
 * 消费交易：扣余额，要密码
 */
@Component
public class ConsumeTransaction extends AbstractTransaction {

    @Override
    public TradeType type() {
        return TradeType.CONSUME;
    }

    @Override
    public void apply(AccountMapper accountMapper, Long accountId, BigDecimal amount) {
        int rows = accountMapper.deduct(accountId, amount);
        if (rows == 0) {
            throw new BizException("余额不足");
        }
    }

    @Override
    public boolean needPayPassword() {
        return true;
    }

    @Override
    public String summary() {
        return "消费扣款";
    }
}
