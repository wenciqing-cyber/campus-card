package com.lwb.campuscard.pattern.builder;

import com.lwb.campuscard.model.dto.PricingResult;
import com.lwb.campuscard.model.entity.Terminal;
import com.lwb.campuscard.model.entity.Trade;
import com.lwb.campuscard.model.enums.TradeStatus;
import com.lwb.campuscard.model.enums.TradeType;
import com.lwb.campuscard.pattern.support.Money;

import java.math.BigDecimal;

/**
 * 交易单建造者：字段多、且有些必填有些可选，用链式一步步拼
 * 好处是调用处不会出现一长串 setXxx，也不会漏字段
 */
public class TransactionTicketBuilder {

    private final TransactionTicket ticket = new TransactionTicket();
    private final Trade trade = new Trade();

    public TransactionTicketBuilder tradeNo(String tradeNo) {
        trade.setTradeNo(tradeNo);
        return this;
    }

    public TransactionTicketBuilder requestNo(String requestNo) {
        trade.setRequestNo(requestNo);
        return this;
    }

    public TransactionTicketBuilder card(String cardNo, Long accountId) {
        trade.setCardNo(cardNo);
        trade.setAccountId(accountId);
        return this;
    }

    public TransactionTicketBuilder type(TradeType type) {
        trade.setTradeType(type.name());
        return this;
    }

    public TransactionTicketBuilder terminal(Terminal terminal) {
        if (terminal != null) {
            trade.setTerminalNo(terminal.getTerminalNo());
            ticket.setTerminalName(terminal.getTerminalName());
        }
        return this;
    }

    public TransactionTicketBuilder pricing(PricingResult pricing) {
        trade.setOriginAmount(Money.of(pricing.getOriginAmount()));
        trade.setDiscountAmount(Money.of(pricing.getDiscountAmount()));
        trade.setPayAmount(Money.of(pricing.getPayAmount()));
        trade.setPointsDelta(pricing.getPointsDelta() == null ? 0 : pricing.getPointsDelta());
        trade.setStrategyCode(pricing.getStrategyCode());
        ticket.setDiscountDetail(pricing.getDescription());
        return this;
    }

    /** 充值和退款没有优惠，直接给金额 */
    public TransactionTicketBuilder amount(BigDecimal amount) {
        trade.setOriginAmount(Money.of(amount));
        trade.setDiscountAmount(Money.ZERO);
        trade.setPayAmount(Money.of(amount));
        trade.setPointsDelta(0);
        return this;
    }

    public TransactionTicketBuilder status(TradeStatus status) {
        trade.setStatus(status.getCode());
        return this;
    }

    public TransactionTicketBuilder originTradeNo(String originTradeNo) {
        trade.setOriginTradeNo(originTradeNo);
        return this;
    }

    public TransactionTicketBuilder remark(String remark) {
        trade.setRemark(remark);
        return this;
    }

    public TransactionTicketBuilder step(String step) {
        ticket.getSteps().add(step);
        return this;
    }

    public TransactionTicket build() {
        ticket.setTrade(trade);
        return ticket;
    }
}
