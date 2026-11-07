package com.lwb.campuscard.pattern.adapter;

import com.lwb.campuscard.common.BizException;
import com.lwb.campuscard.model.entity.Terminal;
import com.lwb.campuscard.model.enums.TerminalType;
import com.lwb.campuscard.pattern.support.Money;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Map;

/**
 * POS 收银适配：报文里直接是金额
 */
@Component
public class PosTerminalAdapter implements TerminalAdapter {

    @Override
    public TerminalType support() {
        return TerminalType.POS;
    }

    @Override
    public PayRequest adapt(RawTerminalMessage raw, Terminal terminal) {
        Map<String, Object> p = raw.getPayload();
        if (p == null || p.get("amount") == null) {
            throw new BizException("POS 报文缺少 amount 字段");
        }
        BigDecimal amount = Money.of(new BigDecimal(String.valueOf(p.get("amount"))));
        if (!Money.positive(amount)) {
            throw new BizException("消费金额必须大于 0");
        }

        PayRequest req = new PayRequest();
        req.setTerminalNo(raw.getTerminalNo());
        req.setTerminalType(support());
        req.setCardNo(raw.getCardNo());
        req.setPayPwd(raw.getPayPwd());
        req.setRequestNo(raw.getRequestNo());
        req.setAmount(amount);
        req.setAdaptNote("POS 金额直取 " + amount.toPlainString() + " 元");
        return req;
    }
}
