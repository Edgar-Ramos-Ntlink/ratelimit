package com.eddy.request;

import com.eddy.model.DefaultRequestLimitRulesSupplier;
import com.eddy.model.RequestLimitRule;
import com.eddy.model.SavedKey;
import com.eddy.tool.SystemTimeSupplier;
import net.jodah.expiringmap.ExpirationPolicy;
import net.jodah.expiringmap.ExpiringMap;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

public class RateLimiter {
    private final SystemTimeSupplier timeSupplier;
    private final ExpiringMap<String, ConcurrentMap<String, Long>> expiringKeyMap;
    private final DefaultRequestLimitRulesSupplier rulesSupplier;

    /**
     * Build Rate limiter with only one rule and default extra configs
     *
     * @param RateLimiter rule
     * @return Returns RateLimiter instance
     */
    public RateLimiter(RequestLimitRule rule) {
        this(Collections.singleton(rule), new SystemTimeSupplier());
    }

    /**
     * Build Rate limiter with multiple rules and default extra configs
     *
     * @param Set<RequestLimitRule> rule
     * @return Returns RateLimiter instance
     */
    public RateLimiter(Set<RequestLimitRule> rules) {
        this(rules, new SystemTimeSupplier());
    }

    /**
     * Build Rate limiter with multiple rules, specific SystemTimeSupplier  and default extra configs
     *
     * @param Set<RequestLimitRule> rule
     * @param SystemTimeSupplier    timeSupplier
     * @return Returns RateLimiter instance
     */
    public RateLimiter(Set<RequestLimitRule> rules, SystemTimeSupplier timeSupplier) {
        this(ExpiringMap.builder().variableExpiration().build(), rules, timeSupplier);
    }

    /**
     * Build Rate limiter with multiple rules, specific SystemTimeSupplier  and specific ExpiringMap
     *
     * @param ExpiringMap<String,   ConcurrentMap<String, Long>> expiringKeyMap
     * @param Set<RequestLimitRule> rule
     * @param SystemTimeSupplier    timeSupplier
     * @return Returns RateLimiter instance
     */
    public RateLimiter(ExpiringMap<String, ConcurrentMap<String, Long>> expiringKeyMap, Set<RequestLimitRule> rules, SystemTimeSupplier timeSupplier) {
        Objects.requireNonNull(rules, "rules can not be null");
        Objects.requireNonNull(timeSupplier, "time supplier can not be null");
        if(rules.isEmpty()) {
            throw new IllegalArgumentException("at least one rule must be provided");
        } else {
            this.expiringKeyMap = expiringKeyMap;
            this.timeSupplier = timeSupplier;
            this.rulesSupplier = new DefaultRequestLimitRulesSupplier(rules);
        }
    }

    /**
     * Add an iteration for the key provided, returns true when over pass the limit
     *
     * @param String  key
     * @return Returns boolean
     */
    public boolean overLimitWhenIncremented(String key) {
        return this.overLimitWhenIncremented(key, 1);
    }

    /**
     * Add weight iterations for the key provided, returns true when over pass the limit
     *
     * @param String  key
     * @param int  weight
     * @return Returns boolean
     */
    public boolean overLimitWhenIncremented(String key, int weight) {
        return this.eqOrGeLimit(key, weight, true);
    }

    /**
     * Add an iteration for the key provided, returns true when is in the limit
     *
     * @param String  key
     * @return Returns boolean
     */
    public boolean geLimitWhenIncremented(String key) {
        return this.geLimitWhenIncremented(key, 1);
    }

    /**
     * Add weight iterations for the key provided, returns true when is in the limit
     *
     * @param String  key
     * @param int  weight
     * @return Returns boolean
     */
    public boolean geLimitWhenIncremented(String key, int weight) {
        return this.eqOrGeLimit(key, weight, false);
    }

    /**
     * Removes the key provided and returns if was deleted
     *
     * @param String  key
     * @return Returns boolean
     */
    public boolean resetLimit(String key) {
        return this.expiringKeyMap.remove(key) != null;
    }

    private ConcurrentMap<String, Long> getMap(String key, int longestDuration) {
        ConcurrentMap<String, Long> keyMap = this.expiringKeyMap.get(key);
        if(keyMap == null) {
            keyMap = new ConcurrentHashMap();
            this.expiringKeyMap.put(key, keyMap, ExpirationPolicy.CREATED, (long) longestDuration, TimeUnit.SECONDS);
        }
        return (ConcurrentMap) keyMap;
    }

    private boolean eqOrGeLimit(String key, int weight, boolean strictlyGreater) {
        long now = this.timeSupplier.get();
        Set<RequestLimitRule> rules = this.rulesSupplier.getRules(key);
        int longestDurationSeconds = rules.stream().map(RequestLimitRule::getDuration).reduce(Integer::max).orElse(0);
        List<SavedKey> savedKeys = new ArrayList();
        Map<String, Long> keyMap = this.getMap(key, longestDurationSeconds);
        boolean geLimit = false;

        Long computedCountKeyBlockIdValue;
        for(RequestLimitRule rule : rules) {
            SavedKey savedKey = new SavedKey(now, rule.getDuration(), rule.getPrecision());
            savedKeys.add(savedKey);
            computedCountKeyBlockIdValue = keyMap.get(savedKey.getTsKey());
            computedCountKeyBlockIdValue = computedCountKeyBlockIdValue != null ? computedCountKeyBlockIdValue : savedKey.getTrimBefore();
            if(computedCountKeyBlockIdValue > now) {
                return true;
            }

            long decr = 0L;
            List<String> dele = new ArrayList();
            long trim = Math.min(savedKey.getTrimBefore(), computedCountKeyBlockIdValue + savedKey.getBlocks());

            for(long oldBlock = computedCountKeyBlockIdValue; oldBlock <= trim - 1L; ++oldBlock) {
                String bkey = savedKey.getCountKey() + oldBlock;
                Long bcount = keyMap.get(bkey);
                if(bcount != null) {
                    decr += bcount;
                    dele.add(bkey);
                }
            }
            Long cur;
            final long decrTemp = decr;
            if(!dele.isEmpty()) {
                Objects.requireNonNull(keyMap);
                dele.forEach(keyMap::remove);
                cur = keyMap.compute(savedKey.getCountKey(), (k, v) -> v - decrTemp);
            } else {
                cur = keyMap.get(savedKey.getCountKey());
            }

            long count = (cur == null ? 0L : cur) + (long) weight;
            if(count > rule.getLimit()) {
                return true;
            }
            if(!strictlyGreater && count == rule.getLimit()) {
                geLimit = true;
            }
        }

        if(weight != 0) {
            for(SavedKey savedKey : savedKeys) {
                keyMap.put(savedKey.getTsKey(), savedKey.getTrimBefore());
                keyMap.compute(savedKey.getCountKey(), (k, v) -> (v == null ? 0L : v) + (long) weight);
            }
        }

        return geLimit;
    }
}
