package com.lwb.campuscard.controller;

import com.lwb.campuscard.common.Result;
import com.lwb.campuscard.model.dto.ConsumeCommand;
import com.lwb.campuscard.model.dto.RechargeCommand;
import com.lwb.campuscard.model.dto.RefundCommand;
import com.lwb.campuscard.model.entity.Trade;
import com.lwb.campuscard.service.PayFacade;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * 交易入口：消费 / 充值 / 退款
 * 这里注入的是 PayFacade，实际拿到的是代理对象，限流和日志在代理里做
 */
@RestController
@RequestMapping("/api/pay")
public class PayController {

    private final PayFacade payFacade;

    public PayController(PayFacade payFacade) {
        this.payFacade = payFacade;
    }

    @PostMapping("/consume")
    public Result<Trade> consume(@RequestBody ConsumeCommand cmd) {
        if (cmd.getRequestNo() == null || cmd.getRequestNo().isEmpty()) {
            cmd.setRequestNo("REQ" + UUID.randomUUID().toString().replace("-", "").substring(0, 16));
        }
        return Result.ok(payFacade.consume(cmd));
    }

    @PostMapping("/recharge")
    public Result<Trade> recharge(@RequestBody RechargeCommand cmd) {
        if (cmd.getRequestNo() == null || cmd.getRequestNo().isEmpty()) {
            cmd.setRequestNo("REQ" + UUID.randomUUID().toString().replace("-", "").substring(0, 16));
        }
        return Result.ok(payFacade.recharge(cmd));
    }

    @PostMapping("/refund")
    public Result<Trade> refund(@RequestBody RefundCommand cmd) {
        if (cmd.getRequestNo() == null || cmd.getRequestNo().isEmpty()) {
            cmd.setRequestNo("REQ" + UUID.randomUUID().toString().replace("-", "").substring(0, 16));
        }
        return Result.ok(payFacade.refund(cmd));
    }
}
