package com.lwb.campuscard.model.enums;

/**
 * 交易状态
 */
public enum TradeStatus {

    SUCCESS(1),
    FAIL(2),
    REVERSED(3);

    private final int code;

    TradeStatus(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
