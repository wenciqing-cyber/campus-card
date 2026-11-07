package com.lwb.campuscard.pattern.singleton;

import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 单例：交易单号生成器
 * 双重检查锁 + 原子自增，保证多线程下不重号
 */
@Component
public class TradeNoGenerator {

    private static volatile TradeNoGenerator instance;

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    private final AtomicInteger seq = new AtomicInteger(0);

    private TradeNoGenerator() {
    }

    public static TradeNoGenerator getInstance() {
        if (instance == null) {
            synchronized (TradeNoGenerator.class) {
                if (instance == null) {
                    instance = new TradeNoGenerator();
                }
            }
        }
        return instance;
    }

    /** 形如 LWB202511052130010001 */
    public String next() {
        int n = seq.incrementAndGet() % 10000;
        return "LWB" + LocalDateTime.now().format(FMT) + String.format("%04d", n);
    }
}
