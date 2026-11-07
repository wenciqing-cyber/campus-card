package com.lwb.campuscard.pattern.proxy;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 极简限流：同一张卡每秒最多 20 笔，防止脚本刷单或终端重发风暴
 */
@Component
public class RateLimiter {

    private static final int LIMIT_PER_SECOND = 20;

    private static class Window {
        private long second;
        private int count;
    }

    private final Map<String, Window> windows = new ConcurrentHashMap<>();

    public boolean tryAcquire(String key) {
        long now = System.currentTimeMillis() / 1000;
        Window w = windows.computeIfAbsent(key, k -> new Window());
        synchronized (w) {
            if (w.second != now) {
                w.second = now;
                w.count = 0;
            }
            w.count++;
            return w.count <= LIMIT_PER_SECOND;
        }
    }
}
