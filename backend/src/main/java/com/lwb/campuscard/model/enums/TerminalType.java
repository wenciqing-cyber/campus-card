package com.lwb.campuscard.model.enums;

/**
 * 终端类型：适配器模式按这个类型挑选适配器
 */
public enum TerminalType {

    POS("食堂/超市收银", 1),
    GATE("门禁刷卡", 1),
    WATER("水控计量", 2);

    private final String label;
    /** 报文里金额字段的语义个数，比如水控传的是用量不是金额 */
    private final int fieldStyle;

    TerminalType(String label, int fieldStyle) {
        this.label = label;
        this.fieldStyle = fieldStyle;
    }

    public String getLabel() {
        return label;
    }

    public int getFieldStyle() {
        return fieldStyle;
    }

    public static TerminalType of(String name) {
        for (TerminalType t : values()) {
            if (t.name().equalsIgnoreCase(name)) {
                return t;
            }
        }
        throw new IllegalArgumentException("未知终端类型: " + name);
    }
}
