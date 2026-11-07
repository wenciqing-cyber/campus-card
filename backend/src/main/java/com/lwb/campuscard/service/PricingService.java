package com.lwb.campuscard.service;

import com.lwb.campuscard.dao.DiscountRuleMapper;
import com.lwb.campuscard.model.dto.PricingRequest;
import com.lwb.campuscard.model.dto.PricingResult;
import com.lwb.campuscard.model.entity.DiscountRule;
import com.lwb.campuscard.pattern.decorator.CapDecorator;
import com.lwb.campuscard.pattern.decorator.PointsDecorator;
import com.lwb.campuscard.pattern.decorator.SubsidyDecorator;
import com.lwb.campuscard.pattern.strategy.PricingContext;
import com.lwb.campuscard.pattern.strategy.PricingStrategy;
import com.lwb.campuscard.pattern.support.Money;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

/**
 * 优惠计算服务：策略选基础算法，装饰器往上叠附加优惠
 * 叠加顺序就是 ruleCodes 的书写顺序
 */
@Service
public class PricingService {

    /** 单笔优惠最多 20 元，防止规则叠加后价格失真 */
    private static final BigDecimal CAP = new BigDecimal("20.00");

    private final PricingContext pricingContext;
    private final DiscountRuleMapper ruleMapper;

    public PricingService(PricingContext pricingContext, DiscountRuleMapper ruleMapper) {
        this.pricingContext = pricingContext;
        this.ruleMapper = ruleMapper;
    }

    /**
     * @param ruleCodes 形如 "DISC-85,SUB-2,PTS-100"，第一个是基础策略
     */
    public PricingResult calculate(String ruleCodes, PricingRequest request) {
        String codes = (ruleCodes == null || ruleCodes.isBlank()) ? "NORMAL" : ruleCodes.trim();
        String[] parts = codes.split("[,，]");

        DiscountRule base = ruleMapper.findByCode(parts[0].trim());
        if (base == null) {
            base = ruleMapper.findByCode("NORMAL");
        }

        PricingStrategy strategy = pricingContext.get(base.getRuleType());
        for (int i = 1; i < parts.length; i++) {
            DiscountRule extra = ruleMapper.findByCode(parts[i].trim());
            if (extra == null || extra.getEnabled() == null || extra.getEnabled() != 1) {
                continue;
            }
            strategy = decorate(strategy, extra);
        }
        // 封顶永远在最外层
        strategy = new CapDecorator(strategy, CAP);

        return strategy.calculate(withParams(request, base.getParams()));
    }

    private PricingStrategy decorate(PricingStrategy inner, DiscountRule rule) {
        return switch (rule.getRuleType()) {
            case "SUBSIDY" -> new SubsidyDecorator(inner, Money.of(rule.getParams()));
            case "POINTS" -> new PointsDecorator(inner, Integer.parseInt(rule.getParams()));
            // 折扣只能当基础策略，硬叠会把价格算乱
            default -> inner;
        };
    }

    private PricingRequest withParams(PricingRequest src, String params) {
        PricingRequest r = new PricingRequest();
        r.setCardNo(src.getCardNo());
        r.setOriginAmount(src.getOriginAmount());
        r.setPoints(src.getPoints());
        r.setParams(params);
        return r;
    }
}
