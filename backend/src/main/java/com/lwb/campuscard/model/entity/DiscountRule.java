package com.lwb.campuscard.model.entity;

/**
 * 优惠规则，策略模式的配置载体
 */
public class DiscountRule {

    private Long id;
    private String code;
    private String ruleName;
    private String ruleType;
    private String params;
    private Integer priority;
    private Integer enabled;

    public Long getId() { return id; }

    public void setId(Long id) { this.id = id; }

    public String getCode() { return code; }

    public void setCode(String code) { this.code = code; }

    public String getRuleName() { return ruleName; }

    public void setRuleName(String ruleName) { this.ruleName = ruleName; }

    public String getRuleType() { return ruleType; }

    public void setRuleType(String ruleType) { this.ruleType = ruleType; }

    public String getParams() { return params; }

    public void setParams(String params) { this.params = params; }

    public Integer getPriority() { return priority; }

    public void setPriority(Integer priority) { this.priority = priority; }

    public Integer getEnabled() { return enabled; }

    public void setEnabled(Integer enabled) { this.enabled = enabled; }
}
