package com.lwb.campuscard.model.enums;

/**
 * 卡片状态
 */
public enum CardStatus {

    NORMAL(1, "正常"),
    LOST(2, "挂失"),
    CANCELLED(3, "注销");

    private final int code;
    private final String label;

    CardStatus(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public int getCode() {
        return code;
    }

    public String getLabel() {
        return label;
    }

    public static CardStatus of(int code) {
        for (CardStatus s : values()) {
            if (s.code == code) {
                return s;
            }
        }
        return NORMAL;
    }
}
