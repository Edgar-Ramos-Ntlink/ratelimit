package com.eddy.model;

import com.eddy.request.LimitRulesSupplier;

import java.util.Map;
import java.util.Set;

public class DefaultRequestLimitRulesSupplier {
    private final Map<String, Set<RequestLimitRule>> ruleMap;
    private final Set<RequestLimitRule> defaultRules;

    public DefaultRequestLimitRulesSupplier(Set<RequestLimitRule> rules) {
        this.defaultRules = LimitRulesSupplier.buildDefaultRuleSet(rules);
        this.ruleMap = LimitRulesSupplier.buildRuleMap(rules);
    }

    public Set<RequestLimitRule> getRules(String key) {
        Set<RequestLimitRule> ruleSet = (Set) this.ruleMap.get(key);
        return ruleSet != null ? ruleSet : this.defaultRules;
    }
}
