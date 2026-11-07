package com.lwb.campuscard.pattern.strategy;

import com.lwb.campuscard.model.dto.PricingRequest;
import com.lwb.campuscard.model.dto.PricingResult;

/**
 * 策略模式：优惠算法接口
 * 变化点是优惠规则的种类，新增规则只加实现类，不动调用方
 */
public interface PricingStrategy {

    /** 策略编码，对应 discount_rule.rule_type */
    String code();

    PricingResult calculate(PricingRequest request);
}
