package com.lwb.campuscard.pattern.proxy;

import com.lwb.campuscard.common.BizException;
import com.lwb.campuscard.dao.OperationLogMapper;
import com.lwb.campuscard.model.dto.ConsumeCommand;
import com.lwb.campuscard.model.dto.RechargeCommand;
import com.lwb.campuscard.model.dto.RefundCommand;
import com.lwb.campuscard.model.entity.Trade;
import com.lwb.campuscard.service.CardServiceImpl;
import com.lwb.campuscard.service.PayFacade;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

/**
 * 代理模式：包在真实业务外面，统一做限流、耗时统计、操作日志
 * 业务代码里就不用到处插日志了，改这些横切逻辑也只改这一处
 */
@Component
@Primary
public class CardServiceProxy implements PayFacade {

    private final CardServiceImpl target;
    private final RateLimiter rateLimiter;
    private final OperationLogMapper logMapper;

    public CardServiceProxy(CardServiceImpl target, RateLimiter rateLimiter, OperationLogMapper logMapper) {
        this.target = target;
        this.rateLimiter = rateLimiter;
        this.logMapper = logMapper;
    }

    @Override
    public Trade consume(ConsumeCommand cmd) {
        if (!rateLimiter.tryAcquire(cmd.getCardNo())) {
            throw new BizException("操作太频繁，请稍后再试");
        }
        long start = System.currentTimeMillis();
        try {
            Trade trade = target.consume(cmd);
            log("CONSUME", cmd.getCardNo(),
                    "终端 " + cmd.getTerminalNo() + " 扣款 " + trade.getPayAmount() + " 元", start);
            return trade;
        } catch (RuntimeException e) {
            log("CONSUME_FAIL", cmd.getCardNo(), e.getMessage(), start);
            throw e;
        }
    }

    @Override
    public Trade recharge(RechargeCommand cmd) {
        long start = System.currentTimeMillis();
        try {
            Trade trade = target.recharge(cmd);
            log("RECHARGE", cmd.getCardNo(), "充值 " + trade.getPayAmount() + " 元", start);
            return trade;
        } catch (RuntimeException e) {
            log("RECHARGE_FAIL", cmd.getCardNo(), e.getMessage(), start);
            throw e;
        }
    }

    @Override
    public Trade refund(RefundCommand cmd) {
        long start = System.currentTimeMillis();
        try {
            Trade trade = target.refund(cmd);
            log("REFUND", cmd.getOriginTradeNo(), "冲正 " + trade.getPayAmount() + " 元", start);
            return trade;
        } catch (RuntimeException e) {
            log("REFUND_FAIL", cmd.getOriginTradeNo(), e.getMessage(), start);
            throw e;
        }
    }

    /** 日志写失败不能影响交易本身 */
    private void log(String type, String target, String detail, long start) {
        try {
            logMapper.insert(type, target,
                    detail == null ? "" : detail.substring(0, Math.min(detail.length(), 280)),
                    (int) (System.currentTimeMillis() - start));
        } catch (Exception ignored) {
            // 日志丢了就丢了
        }
    }
}
