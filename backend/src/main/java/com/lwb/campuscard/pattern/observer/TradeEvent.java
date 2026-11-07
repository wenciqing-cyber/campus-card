package com.lwb.campuscard.pattern.observer;

import com.lwb.campuscard.model.entity.Account;
import com.lwb.campuscard.model.entity.Trade;

import java.time.LocalDateTime;

/**
 * 交易完成事件，观察者拿到的就是它
 */
public class TradeEvent {

    private final Trade trade;
    private final Account account;
    private final String terminalName;
    private final LocalDateTime happenedAt = LocalDateTime.now();

    public TradeEvent(Trade trade, Account account, String terminalName) {
        this.trade = trade;
        this.account = account;
        this.terminalName = terminalName;
    }

    public Trade getTrade() { return trade; }

    public Account getAccount() { return account; }

    public String getTerminalName() { return terminalName; }

    public LocalDateTime getHappenedAt() { return happenedAt; }
}
