package com.eddy.request;


import com.eddy.models.RequestLimitRule;

import java.util.*;
import java.util.stream.Collectors;

public interface RequestLimitRulesSupplier<T> {
    static Set<RequestLimitRule> buildDefaultRuleSet(Set<RequestLimitRule> rules) {
        return (Set)rules.stream().filter((rule) -> {
            return rule.getKeys() == null;
        }).collect(Collectors.toSet());
    }

    static Map<String, Set<RequestLimitRule>> buildRuleMap(Set<RequestLimitRule> rules) {
        Map<String, Set<RequestLimitRule>> ruleMap = new HashMap();
        Iterator var2 = rules.iterator();

        while(true) {
            RequestLimitRule rule;
            do {
                if (!var2.hasNext()) {
                    return ruleMap;
                }

                rule = (RequestLimitRule)var2.next();
            } while(rule.getKeys() == null);

            Iterator var4 = rule.getKeys().iterator();

            while(var4.hasNext()) {
                String key = (String)var4.next();
                ((Set)ruleMap.computeIfAbsent(key, (k) -> {
                    return new HashSet();
                })).add(rule);
            }
        }
    }

    T getRules(String var1);
}
