package com.lwb.campuscard.pattern.builder;

import com.lwb.campuscard.model.entity.Trade;

import java.util.ArrayList;
import java.util.List;

/**
 * 建造者模式的产品：一张完整的交易单
 * 除了落库字段，还带了给前端展示用的处理轨迹
 */
public class TransactionTicket {

    private Trade trade;
    private String terminalName;
    private String discountDetail;
    private final List<String> steps = new ArrayList<>();

    public Trade getTrade() { return trade; }

    public void setTrade(Trade trade) { this.trade = trade; }

    public String getTerminalName() { return terminalName; }

    public void setTerminalName(String terminalName) { this.terminalName = terminalName; }

    public String getDiscountDetail() { return discountDetail; }

    public void setDiscountDetail(String discountDetail) { this.discountDetail = discountDetail; }

    public List<String> getSteps() { return steps; }
}
