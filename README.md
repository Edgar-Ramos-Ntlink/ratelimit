RateLimit
============
A Java library for rate limiting, assembled using internal memory. The library's interfaces support thread-safe sync, async, and reactive usage patterns.

#### Features
-Uses an efficient approximated sliding window algorithm for rate limiting
-Multiple limit rules per instance

### Dependencies
* Java 11
* Maven

### External Dependencies installed with maven
* guava
* reactor-core
* expiringmap
* junit

### Install code in IntellIj

* Open Folder code as Maven Project
* Install code using the following maven Command ```java mvn clean install -U```

#### Import project
```xml
<dependency>
  <groupId>com.eddy</groupId>
  <artifactId>ratelimit</artifactId>
  <version>1.0.0</version>
</dependency>
```

#### Authors

Edgar Joseduardo Ramos Silveyra

#### Basic Synchronous Example

```java
    //Define keys to manage their limit
        Set<String> keys = Stream.of("User1:/api/v1/developers")
        .collect(Collectors.toCollection(HashSet::new));
        //Define ratelimit configs
        //First parameter is time in seconds
        //Second parameter is threshold limit
        //Third parameter are the key rules
        RequestLimitRule rule = new RequestLimitRule(60, 3, keys);
        Set<RequestLimitRule> rules = Stream.of(rule)
        .collect(Collectors.toCollection(HashSet::new));
        RateLimiter requestRateLimiter = new RateLimiter(rules);
        //Increment action by specific weight
        boolean overLimit = requestRateLimiter.overLimitWhenIncremented("User1:/api/v1/developers", 2);
        //will print false if the limit is not exceeded
        System.out.println(overLimit);
        //Increment action by 1
        overLimit = requestRateLimiter.overLimitWhenIncremented("User1:/api/v1/developers");
        //will print false because the limit is not exceeded
        System.out.println(overLimit);
        overLimit = requestRateLimiter.overLimitWhenIncremented("User1:/api/v1/developers");
        //will print true because the limit is not exceeded
        System.out.println(overLimit);
```

#### Project versions

1.0.0 Initial Version
