package com.eddy;


import com.eddy.models.RequestLimitRule;
import com.eddy.request.InMemoryRequestRateLimiter;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Main2 {
    public static void main(String[] args) {
        Set<RequestLimitRule> rules = new HashSet<>();
        Set<String> keys = new HashSet<>();
        keys.add("PUT");
        keys.add("POST");
        RequestLimitRule rule = new RequestLimitRule(60,4,keys);
        rules.add(rule);
        InMemoryRequestRateLimiter requestRateLimiter = new InMemoryRequestRateLimiter(rules);

        List<String> list = Arrays.asList("1","2","3","4","5","6","7","8");

        boolean overLimit = requestRateLimiter.overLimitWhenIncremented("PUT");
        System.out.println(overLimit);
        for(int i = 0; i < 10 ; i++) {
            overLimit = requestRateLimiter.overLimitWhenIncremented("PUT");
            System.out.println(overLimit);
        }
    }
}
