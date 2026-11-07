package com.lwb.campuscard.pattern.singleton;

import java.math.BigDecimal;

/**
 * 单例：系统运行参数（限额、费率开关等）
 * 静态内部类写法，懒加载且天然线程安全
 */
public class SystemConfig {

    private SystemConfig() {
    }

    private static class Holder {
        private static final SystemConfig INSTANCE = new SystemConfig();
    }

    public static SystemConfig getInstance() {
        return Holder.INSTANCE;
    }

    private volatile BigDecimal singleLimit = new BigDecimal("200.00");
    private volatile BigDecimal dailyLimit = new BigDecimal("500.00");
    private volatile int pointsPerYuan = 100;
    private volatile boolean notifyEnabled = true;

    public BigDecimal getSingleLimit() { return singleLimit; }

    public void setSingleLimit(BigDecimal singleLimit) { this.singleLimit = singleLimit; }

    public BigDecimal getDailyLimit() { return dailyLimit; }

    public void setDailyLimit(BigDecimal dailyLimit) { this.dailyLimit = dailyLimit; }

    public int getPointsPerYuan() { return pointsPerYuan; }

    public void setPointsPerYuan(int pointsPerYuan) { this.pointsPerYuan = pointsPerYuan; }

    public boolean isNotifyEnabled() { return notifyEnabled; }

    public void setNotifyEnabled(boolean notifyEnabled) { this.notifyEnabled = notifyEnabled; }
}
