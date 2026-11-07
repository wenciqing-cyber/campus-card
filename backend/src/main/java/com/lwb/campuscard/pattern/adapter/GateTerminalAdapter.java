package com.lwb.campuscard.pattern.adapter;

import com.lwb.campuscard.model.entity.Terminal;
import com.lwb.campuscard.model.enums.TerminalType;
import com.lwb.campuscard.pattern.support.Money;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Map;

/**
 * 门禁适配：通常按次计费，没传金额就取终端上配的单次费率
 */
@Component
public class GateTerminalAdapter implements TerminalAdapter {

    @Override
    public TerminalType support() {
        return TerminalType.GATE;
    }

    @Override
    public PayRequest adapt(RawTerminalMessage raw, Terminal terminal) {
        Map<String, Object> p = raw.getPayload();
        Object amountObj = p == null ? null : p.get("amount");
        BigDecimal amount = amountObj == null
                ? Money.of(terminal.getRate() == null ? BigDecimal.ONE : terminal.getRate())
                : Money.of(new BigDecimal(String.valueOf(amountObj)));

        PayRequest req = new PayRequest();
        req.setTerminalNo(raw.getTerminalNo());
        req.setTerminalType(support());
        req.setCardNo(raw.getCardNo());
        req.setPayPwd(raw.getPayPwd());
        req.setRequestNo(raw.getRequestNo());
        req.setAmount(amount);
        req.setAdaptNote(amountObj == null
                ? "门禁按次计费 " + amount.toPlainString() + " 元"
                : "门禁刷卡 " + amount.toPlainString() + " 元");
        return req;
    }
}
