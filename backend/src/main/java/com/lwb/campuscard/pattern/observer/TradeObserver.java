package com.lwb.campuscard.pattern.observer;

/**
 * 观察者：交易完成后要被通知的角色
 * 新增一个"要跟着交易做事"的功能，只要实现这个接口并注册
 */
public interface TradeObserver {

    void onTrade(TradeEvent event);

    String name();
}
