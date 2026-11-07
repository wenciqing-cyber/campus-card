package com.lwb.campuscard.pattern.factory;

import com.lwb.campuscard.dao.AccountMapper;
import com.lwb.campuscard.model.enums.TradeType;

import java.math.BigDecimal;
import org.springframework.stereotype.Component;

/**
 * 退款冲正：金额原路退回，不改原单，只新增一张反向单
 */
@Component
public class RefundTransaction extends AbstractTransaction {

    @Override
    public TradeType type() {
        return TradeType.REFUND;
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
        return "消费冲正退款";
    }
}
