package com.eddy;

import es.moki.ratelimitj.core.limiter.request.RequestLimitRule;
import es.moki.ratelimitj.core.limiter.request.RequestRateLimiter;
import es.moki.ratelimitj.inmemory.request.InMemorySlidingWindowRequestRateLimiter;

import java.time.Duration;
import java.util.*;

public class Main {
    public static void main(String[] args) {
        Set<RequestLimitRule> rules = new HashSet<RequestLimitRule>();
        Set<String> keys = new HashSet<String>();
        Set<String> keys2 = new HashSet<String>();
        keys.add("PUT");
        keys.add("POST");
        keys2.add("GET");
        RequestLimitRule rule = RequestLimitRule.of(Duration.ofMinutes(1), 5);
        RequestLimitRule rule2 = RequestLimitRule.of(Duration.ofMinutes(1), 5);
        rule = rule.matchingKeys(keys);
        rule2 = rule2.matchingKeys(keys2);
        rules.add(rule);
        rules.add(rule2);
        RequestRateLimiter requestRateLimiter = new InMemorySlidingWindowRequestRateLimiter(rules);


        List<String> list = Arrays.asList("1","2","3","4","5","6","7","8");
        requestRateLimiter.overLimitWhenIncremented("POST");
        requestRateLimiter.overLimitWhenIncremented("POST");
        System.out.println(list);
        list.parallelStream().forEach(a-> {
            boolean overLimit = requestRateLimiter.overLimitWhenIncremented("POST");
            System.out.println(overLimit);});

        System.out.println("-------------");
        boolean overLimit = requestRateLimiter.overLimitWhenIncremented("POST");
        System.out.println(overLimit);
        System.out.println("-------------");
        overLimit = requestRateLimiter.overLimitWhenIncremented("PUT");
        System.out.println(overLimit);
        for(int i = 0; i < 10 ; i++) {
            overLimit = requestRateLimiter.overLimitWhenIncremented("PUT");
            System.out.println(overLimit);
        }

    }

}