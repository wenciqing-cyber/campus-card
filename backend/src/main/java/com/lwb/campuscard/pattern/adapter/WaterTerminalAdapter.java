package com.lwb.campuscard.pattern.adapter;

import com.lwb.campuscard.common.BizException;
import com.lwb.campuscard.model.entity.Terminal;
import com.lwb.campuscard.model.enums.TerminalType;
import com.lwb.campuscard.pattern.support.Money;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Map;

/**
 * 水控适配：报文里是用量（升），乘以终端倍率折算成金额
 */
@Component
public class WaterTerminalAdapter implements TerminalAdapter {

    @Override
    public TerminalType support() {
        return TerminalType.WATER;
    }

    @Override
    public PayRequest adapt(RawTerminalMessage raw, Terminal terminal) {
        Map<String, Object> p = raw.getPayload();
        if (p == null || p.get("volume") == null) {
            throw new BizException("水控报文缺少 volume 字段");
        }
        BigDecimal volume = new BigDecimal(String.valueOf(p.get("volume")));
        if (volume.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BizException("用水量必须大于 0");
        }
        BigDecimal rate = terminal.getRate() == null ? BigDecimal.ONE : terminal.getRate();
        BigDecimal amount = Money.of(volume.multiply(rate));

        PayRequest req = new PayRequest();
        req.setTerminalNo(raw.getTerminalNo());
        req.setTerminalType(support());
        req.setCardNo(raw.getCardNo());
        req.setPayPwd(raw.getPayPwd());
        req.setRequestNo(raw.getRequestNo());
        req.setAmount(amount);
        req.setAdaptNote(volume.toPlainString() + " 升 × " + rate.toPlainString() + " 元/升 = "
                + amount.toPlainString() + " 元");
        return req;
    }
}
