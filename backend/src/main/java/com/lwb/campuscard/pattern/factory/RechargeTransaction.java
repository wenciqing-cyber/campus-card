package com.lwb.campuscard.pattern.factory;

import com.lwb.campuscard.dao.AccountMapper;
import com.lwb.campuscard.model.enums.TradeType;

import java.math.BigDecimal;
import org.springframework.stereotype.Component;

/**
 * 充值交易：加余额，不需要密码
 */
@Component
public class RechargeTransaction extends AbstractTransaction {

    @Override
    public TradeType type() {
        return TradeType.RECHARGE;
    }

    @Override
    public void apply(AccountMapper accountMapper, Long accountId, BigDecimal amount) {
        accountMapper.addBalance(accountId, amount);
    }

    @Override
    public boolean needPayPassword() {
        return false;
    }

    @Override
    public String summary() {
        return "账户充值";
    }
}
