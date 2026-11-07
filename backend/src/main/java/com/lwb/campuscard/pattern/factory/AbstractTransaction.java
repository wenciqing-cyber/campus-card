package com.lwb.campuscard.pattern.factory;

import com.lwb.campuscard.dao.AccountMapper;
import com.lwb.campuscard.model.enums.TradeType;

import java.math.BigDecimal;

/**
 * 工厂模式：把"交易类型"和"如何记账"解耦
 * 每个子类就是一种交易，新增退款方式不用改已有代码
 */
public abstract class AbstractTransaction {

    public abstract TradeType type();

    /** 对账户余额的记账动作，金额永远传正数 */
    public abstract void apply(AccountMapper accountMapper, Long accountId, BigDecimal amount);

    /** 是否需要交易密码：充值和退款不需要 */
    public abstract boolean needPayPassword();

    public abstract String summary();
}
