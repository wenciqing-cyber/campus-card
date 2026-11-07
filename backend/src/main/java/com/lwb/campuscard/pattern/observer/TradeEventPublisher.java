package com.lwb.campuscard.pattern.observer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 被观察者：交易成功后统一广播
 * 用 CopyOnWriteArrayList，发布时不用加锁
 */
@Component
public class TradeEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(TradeEventPublisher.class);

    private final List<TradeObserver> observers = new CopyOnWriteArrayList<>();

    public TradeEventPublisher(List<TradeObserver> beans) {
        observers.addAll(beans);
    }

    public void register(TradeObserver observer) {
        observers.add(observer);
    }

    public void unregister(TradeObserver observer) {
        observers.remove(observer);
    }

    /**
     * 广播：单个观察者抛异常不能影响主交易，这里吞掉只记日志
     */
    public void publish(TradeEvent event) {
        for (TradeObserver o : observers) {
            try {
                o.onTrade(event);
            } catch (Exception e) {
                log.warn("观察者 {} 执行失败: {}", o.name(), e.getMessage());
            }
        }
    }

    public List<TradeObserver> list() {
        return List.copyOf(observers);
    }
}
