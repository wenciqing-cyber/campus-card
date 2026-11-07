package com.lwb.campuscard.service;

import com.lwb.campuscard.model.dto.ConsumeCommand;
import com.lwb.campuscard.model.dto.RechargeCommand;
import com.lwb.campuscard.model.dto.RefundCommand;
import com.lwb.campuscard.model.entity.Trade;

/**
 * 支付门面：代理和被代理对象实现同一个接口，客户端只认这个
 */
public interface PayFacade {

    Trade consume(ConsumeCommand cmd);

    Trade recharge(RechargeCommand cmd);

    Trade refund(RefundCommand cmd);
}
