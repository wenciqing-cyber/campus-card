package com.lwb.campuscard.model.enums;

/**
 * 交易类型，同时作为交易工厂的入参
 */
public enum TradeType {

    RECHARGE("充值"),
    CONSUME("消费"),
    REFUND("退款");

    private final String label;

    TradeType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public static TradeType of(String name) {
        for (TradeType t : values()) {
            if (t.name().equalsIgnoreCase(name)) {
                return t;
            }
        }
        throw new IllegalArgumentException("未知交易类型: " + name);
    }
}
