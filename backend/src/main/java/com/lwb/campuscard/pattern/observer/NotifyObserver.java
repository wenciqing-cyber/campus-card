package com.lwb.campuscard.pattern.observer;

import com.lwb.campuscard.pattern.singleton.SystemConfig;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedDeque;

/**
 * 观察者之一：消息通知
 * 只保留最近 50 条，前端轮询拿来做通知气泡
 */
@Component
public class NotifyObserver implements TradeObserver {

    private static final int MAX = 50;

    private final Deque<Map<String, Object>> box = new ConcurrentLinkedDeque<>();

    @Override
    public void onTrade(TradeEvent event) {
        if (!SystemConfig.getInstance().isNotifyEnabled()) {
            return;
        }
        String type = event.getTrade().getTradeType();
        String text = switch (type) {
            case "CONSUME" -> "消费 " + event.getTrade().getPayAmount().toPlainString() + " 元 @ "
                    + (event.getTerminalName() == null ? event.getTrade().getTerminalNo() : event.getTerminalName());
            case "RECHARGE" -> "充值 " + event.getTrade().getPayAmount().toPlainString() + " 元";
            default -> "退款 " + event.getTrade().getPayAmount().toPlainString() + " 元";
        };
        box.addFirst(Map.of(
                "text", text,
                "cardNo", event.getTrade().getCardNo(),
                "tradeNo", event.getTrade().getTradeNo(),
                "time", LocalDateTime.now().toString()
        ));
        while (box.size() > MAX) {
            box.pollLast();
        }
    }

    @Override
    public String name() {
        return "消息通知";
    }

    public List<Map<String, Object>> recent() {
        return new ArrayList<>(box);
    }
}
