RateLimit
============
A Java library for rate limiting, assembled using internal memory. The library's interfaces support thread-safe sync, async, and reactive usage patterns.

#### Features
-Uses an efficient approximated sliding window algorithm for rate limiting
-Multiple limit rules per instance

### Dependencies
-Java 11


```xml
<dependency>
  <groupId>com.eddy</groupId>
  <artifactId>ratelimit</artifactId>
  <version>${com.eddy.version}</version>
</dependency>
```

#### Authors

Edgar Joseduardo Ramos Silveyra

#### Basic Synchronous Example
```java
    //Define keys to calculate their limit
    Set<String> keys = Stream.of("edgar:POST", "edgar:PUT")
        .collect(Collectors.toCollection(HashSet::new));
    //Define ratelimit configs
    RequestLimitRule rule = new RequestLimitRule(60,4,keys);
    Set<RequestLimitRule> rules = Stream.of(rule)
        .collect(Collectors.toCollection(HashSet::new));
    InMemoryRequestRateLimiter requestRateLimiter = new InMemoryRequestRateLimiter(rules);
    //example use
    boolean overLimit = requestRateLimiter.overLimitWhenIncremented("PUT");
```

#### Import project

1.0.0 Initial Version
