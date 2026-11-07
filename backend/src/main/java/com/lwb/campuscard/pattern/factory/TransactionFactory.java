package com.lwb.campuscard.pattern.factory;

import com.lwb.campuscard.common.BizException;
import com.lwb.campuscard.model.enums.TradeType;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * 交易工厂：调用方给类型，拿对象，不关心怎么记账
 */
@Component
public class TransactionFactory {

    private final Map<TradeType, AbstractTransaction> pool = new EnumMap<>(TradeType.class);

    public TransactionFactory() {
        register(new ConsumeTransaction());
        register(new RechargeTransaction());
        register(new RefundTransaction());
    }

    private void register(AbstractTransaction t) {
        pool.put(t.type(), t);
    }

    public AbstractTransaction create(TradeType type) {
        AbstractTransaction t = pool.get(type);
        if (t == null) {
            throw new BizException("不支持的交易类型: " + type);
        }
        return t;
    }

    public List<AbstractTransaction> all() {
        return List.copyOf(pool.values());
    }
}
