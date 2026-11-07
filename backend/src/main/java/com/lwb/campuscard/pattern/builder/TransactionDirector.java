package com.lwb.campuscard.pattern.builder;

import com.lwb.campuscard.model.dto.PricingResult;
import com.lwb.campuscard.model.entity.Terminal;
import com.lwb.campuscard.model.enums.TradeStatus;
import com.lwb.campuscard.model.enums.TradeType;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * 导演类：规定"一张消费单长什么样"
 * 建造顺序固定，服务层只管给料
 */
@Component
public class TransactionDirector {

    public TransactionTicket buildConsume(String tradeNo, String requestNo, String cardNo, Long accountId,
                                          Terminal terminal, PricingResult pricing, String remark) {
        return new TransactionTicketBuilder()
                .tradeNo(tradeNo)
                .requestNo(requestNo)
                .card(cardNo, accountId)
                .type(TradeType.CONSUME)
                .terminal(terminal)
                .pricing(pricing)
                .status(TradeStatus.SUCCESS)
                .remark(remark)
                .step("终端报文适配")
                .step("优惠计算 " + pricing.getPayAmount().toPlainString() + " 元")
                .step("责任链校验通过")
                .step("余额已扣减")
                .build();
    }

    public TransactionTicket buildRecharge(String tradeNo, String requestNo, String cardNo, Long accountId,
                                           BigDecimal amount, String remark) {
        return new TransactionTicketBuilder()
                .tradeNo(tradeNo)
                .requestNo(requestNo)
                .card(cardNo, accountId)
                .type(TradeType.RECHARGE)
                .amount(amount)
                .status(TradeStatus.SUCCESS)
                .remark(remark)
                .step("充值金额 " + amount.toPlainString() + " 元")
                .step("余额已入账")
                .build();
    }

    public TransactionTicket buildRefund(String tradeNo, String requestNo, String cardNo, Long accountId,
                                         BigDecimal amount, String originTradeNo, String remark) {
        return new TransactionTicketBuilder()
                .tradeNo(tradeNo)
                .requestNo(requestNo)
                .card(cardNo, accountId)
                .type(TradeType.REFUND)
                .amount(amount)
                .status(TradeStatus.SUCCESS)
                .originTradeNo(originTradeNo)
                .remark(remark)
                .step("冲正原单 " + originTradeNo)
                .step("退款 " + amount.toPlainString() + " 元已入账")
                .build();
    }
}
