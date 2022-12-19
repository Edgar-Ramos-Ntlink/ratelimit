package com.eddy.request;

import com.eddy.models.DefaultRequestLimitRulesSupplier;
import com.eddy.models.RequestLimitRule;
import com.eddy.models.SavedKey;

import com.eddy.models.SystemTimeSupplier;
import net.jodah.expiringmap.ExpirationPolicy;
import net.jodah.expiringmap.ExpiringMap;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

public class InMemoryRequestRateLimiter  {
    private final SystemTimeSupplier timeSupplier;
    private final ExpiringMap<String, ConcurrentMap<String, Long>> expiringKeyMap;
    private final DefaultRequestLimitRulesSupplier rulesSupplier;

    public InMemoryRequestRateLimiter(RequestLimitRule rule) {
        this(Collections.singleton(rule), new SystemTimeSupplier());
    }

    public InMemoryRequestRateLimiter(Set<RequestLimitRule> rules) {
        this(rules, new SystemTimeSupplier());
    }

    public InMemoryRequestRateLimiter(Set<RequestLimitRule> rules, SystemTimeSupplier timeSupplier) {
        this(ExpiringMap.builder().variableExpiration().build(), rules, timeSupplier);
    }

    public InMemoryRequestRateLimiter(ExpiringMap<String, ConcurrentMap<String, Long>> expiringKeyMap, Set<RequestLimitRule> rules, SystemTimeSupplier timeSupplier) {
        Objects.requireNonNull(rules, "rules can not be null");
        Objects.requireNonNull(timeSupplier, "time supplier can not be null");
        if (rules.isEmpty()) {
            throw new IllegalArgumentException("at least one rule must be provided");
        } else {
            this.expiringKeyMap = expiringKeyMap;
            this.timeSupplier = timeSupplier;
            this.rulesSupplier = new DefaultRequestLimitRulesSupplier(rules);
        }
    }
    public boolean overLimitWhenIncremented(String key) {
        return this.overLimitWhenIncremented(key, 1);
    }

    public boolean overLimitWhenIncremented(String key, int weight) {
        return this.eqOrGeLimit(key, weight, true);
    }

    public boolean geLimitWhenIncremented(String key) {
        return this.geLimitWhenIncremented(key, 1);
    }

    public boolean geLimitWhenIncremented(String key, int weight) {
        return this.eqOrGeLimit(key, weight, false);
    }

    public boolean resetLimit(String key) {
        return this.expiringKeyMap.remove(key) != null;
    }

    private ConcurrentMap<String, Long> getMap(String key, int longestDuration) {
            ConcurrentMap<String, Long> keyMap = this.expiringKeyMap.get(key);
            if (keyMap == null) {
                keyMap = new ConcurrentHashMap();
                this.expiringKeyMap.put(key, keyMap, ExpirationPolicy.CREATED, (long)longestDuration, TimeUnit.SECONDS);
            }
            return (ConcurrentMap)keyMap;
    }

    private boolean eqOrGeLimit(String key, int weight, boolean strictlyGreater) {
        long now = this.timeSupplier.get();
        Set<RequestLimitRule> rules = this.rulesSupplier.getRules(key);
        int longestDurationSeconds = rules.stream().map(RequestLimitRule::getDuration).reduce(Integer::max).orElse(0);
        List<SavedKey> savedKeys = new ArrayList();
        Map<String, Long> keyMap = this.getMap(key, longestDurationSeconds);
        boolean geLimit = false;

        Long computedCountKeyBlockIdValue;
        for(RequestLimitRule rule:rules) {
            SavedKey savedKey = new SavedKey(now, rule.getDuration(), rule.getPrecision());
            savedKeys.add(savedKey);
            computedCountKeyBlockIdValue = keyMap.get(savedKey.tsKey);
            computedCountKeyBlockIdValue = computedCountKeyBlockIdValue != null ? computedCountKeyBlockIdValue : savedKey.trimBefore;
            if (computedCountKeyBlockIdValue > now) {
                return true;
            }

            long decr = 0L;
            List<String> dele = new ArrayList();
            long trim = Math.min(savedKey.trimBefore, computedCountKeyBlockIdValue + savedKey.blocks);

            for(long oldBlock = computedCountKeyBlockIdValue; oldBlock <= trim - 1L; ++oldBlock) {
                String bkey = savedKey.countKey + oldBlock;
                Long bcount = keyMap.get(bkey);
                if (bcount != null) {
                    decr += bcount;
                    dele.add(bkey);
                }
            }
            Long cur;
            final long decrTemp =decr;
            if (!dele.isEmpty()) {
                Objects.requireNonNull(keyMap);
                dele.forEach(keyMap::remove);
                cur = keyMap.compute(savedKey.countKey, (k, v) -> v - decrTemp);
            } else {
                cur = keyMap.get(savedKey.countKey);
            }

            long count =  (cur == null ?  0L : cur )+ (long)weight;
            if (count > rule.getLimit()) {
                return true;
            }
            if (!strictlyGreater && count == rule.getLimit()) {
                geLimit = true;
            }
        }

        if (weight != 0) {
            for(SavedKey savedKey :savedKeys) {
                keyMap.put(savedKey.tsKey, savedKey.trimBefore);
                keyMap.compute(savedKey.countKey, (k, v) -> (v == null ? 0L : v)  + (long)weight);
            }
        }

        return geLimit;
    }
}
