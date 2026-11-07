package com.lwb.campuscard.pattern.support;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * 金额工具，全部按 2 位小数 HALF_UP，避免各处手写 setScale 不一致
 */
public final class Money {

    public static final BigDecimal CENT = new BigDecimal("0.01");
    public static final BigDecimal ZERO = new BigDecimal("0.00");

    private Money() {
    }

    public static BigDecimal of(BigDecimal v) {
        return (v == null ? ZERO : v).setScale(2, RoundingMode.HALF_UP);
    }

    public static BigDecimal of(String v) {
        return new BigDecimal(v).setScale(2, RoundingMode.HALF_UP);
    }

    /** 最低一分，防止优惠算到最后变成 0 元交易 */
    public static BigDecimal atLeastCent(BigDecimal v) {
        BigDecimal r = of(v);
        return r.compareTo(CENT) < 0 ? CENT : r;
    }

    public static BigDecimal notNegative(BigDecimal v) {
        BigDecimal r = of(v);
        return r.compareTo(ZERO) < 0 ? ZERO : r;
    }

    public static boolean positive(BigDecimal v) {
        return v != null && v.compareTo(ZERO) > 0;
    }
}
