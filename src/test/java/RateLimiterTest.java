import com.eddy.model.RequestLimitRule;
import com.eddy.request.RateLimiter;
import org.junit.Ignore;
import org.junit.Test;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class RateLimiterTest {
    @Test
    public void rateLimiter_validRequest_defaultDefinition() {
        Set<String> keys = Stream.of("User1:/api/v1/developers")
                .collect(Collectors.toCollection(HashSet::new));
        RequestLimitRule rule = new RequestLimitRule(keys);
        Set<RequestLimitRule> rules = Stream.of(rule)
                .collect(Collectors.toCollection(HashSet::new));
        RateLimiter requestRateLimiter = new RateLimiter(rules);
        boolean overLimit = requestRateLimiter.overLimitWhenIncremented("User1:/api/v1/developers");
        assertFalse(overLimit);
    }

    @Test
    public void rateLimiter_validRequest_simpleDefinition() {
        Set<String> keys = Stream.of("User1:/api/v1/developers")
                .collect(Collectors.toCollection(HashSet::new));
        RequestLimitRule rule = new RequestLimitRule(60, 4, keys);
        Set<RequestLimitRule> rules = Stream.of(rule)
                .collect(Collectors.toCollection(HashSet::new));
        RateLimiter requestRateLimiter = new RateLimiter(rules);
        boolean overLimit = requestRateLimiter.overLimitWhenIncremented("User1:/api/v1/developers");
        assertFalse(overLimit);
    }

    @Test
    public void rateLimiter_invalidRequest_simpleDefinition_overLimitSimple() {
        Set<String> keys = Stream.of("User1:/api/v1/developers")
                .collect(Collectors.toCollection(HashSet::new));
        RequestLimitRule rule = new RequestLimitRule(60, 4, keys);
        Set<RequestLimitRule> rules = Stream.of(rule)
                .collect(Collectors.toCollection(HashSet::new));
        RateLimiter requestRateLimiter = new RateLimiter(rules);
        assertFalse(requestRateLimiter.overLimitWhenIncremented("User1:/api/v1/developers"));
        assertFalse(requestRateLimiter.overLimitWhenIncremented("User1:/api/v1/developers"));
        assertFalse(requestRateLimiter.overLimitWhenIncremented("User1:/api/v1/developers"));
        assertFalse(requestRateLimiter.overLimitWhenIncremented("User1:/api/v1/developers"));
        assertTrue(requestRateLimiter.overLimitWhenIncremented("User1:/api/v1/developers"));
    }

    @Test
    public void rateLimiter_invalidRequest_simpleDefinition_overLimitMultiple() {
        Set<String> keys = Stream.of("User1:/api/v1/developers")
                .collect(Collectors.toCollection(HashSet::new));
        RequestLimitRule rule = new RequestLimitRule(60, 4, keys);
        Set<RequestLimitRule> rules = Stream.of(rule)
                .collect(Collectors.toCollection(HashSet::new));
        RateLimiter requestRateLimiter = new RateLimiter(rules);
        assertFalse(requestRateLimiter.overLimitWhenIncremented("User1:/api/v1/developers", 2));
        assertFalse(requestRateLimiter.overLimitWhenIncremented("User1:/api/v1/developers", 2));
        assertTrue(requestRateLimiter.overLimitWhenIncremented("User1:/api/v1/developers", 2));
    }

    @Test
    public void rateLimiter_invalidRequest_simpleDefinition_underLimitSimple() {
        Set<String> keys = Stream.of("User1:/api/v1/developers")
                .collect(Collectors.toCollection(HashSet::new));
        RequestLimitRule rule = new RequestLimitRule(60, 4, keys);
        Set<RequestLimitRule> rules = Stream.of(rule)
                .collect(Collectors.toCollection(HashSet::new));
        RateLimiter requestRateLimiter = new RateLimiter(rules);
        assertFalse(requestRateLimiter.geLimitWhenIncremented("User1:/api/v1/developers"));
        assertFalse(requestRateLimiter.geLimitWhenIncremented("User1:/api/v1/developers"));
        assertFalse(requestRateLimiter.geLimitWhenIncremented("User1:/api/v1/developers"));
        assertTrue(requestRateLimiter.geLimitWhenIncremented("User1:/api/v1/developers"));
        assertTrue(requestRateLimiter.geLimitWhenIncremented("User1:/api/v1/developers"));
    }

    @Test
    public void rateLimiter_invalidRequest_simpleDefinition_underLimitMultiple() {
        Set<String> keys = Stream.of("User1:/api/v1/developers")
                .collect(Collectors.toCollection(HashSet::new));
        RequestLimitRule rule = new RequestLimitRule(60, 4, keys);
        Set<RequestLimitRule> rules = Stream.of(rule)
                .collect(Collectors.toCollection(HashSet::new));
        RateLimiter requestRateLimiter = new RateLimiter(rules);
        assertFalse(requestRateLimiter.geLimitWhenIncremented("User1:/api/v1/developers"));
        assertFalse(requestRateLimiter.geLimitWhenIncremented("User1:/api/v1/developers"));
        assertTrue(requestRateLimiter.geLimitWhenIncremented("User1:/api/v1/developers", 2));
    }

    @Test
    public void rateLimiter_validRequest_complexDefinition() {
        Set<String> keysUser1 = Stream.of("User1:/api/v1/developers", "User1:/api/v1/organizations")
                .collect(Collectors.toCollection(HashSet::new));
        Set<String> keysUser2 = Stream.of("User2:/api/v1/developers", "User2:/api/v1/organizations")
                .collect(Collectors.toCollection(HashSet::new));
        RequestLimitRule rule = new RequestLimitRule(60, 4, keysUser1);
        RequestLimitRule rule2 = new RequestLimitRule(30, 5, keysUser2);
        Set<RequestLimitRule> rules = Stream.of(rule, rule2)
                .collect(Collectors.toCollection(HashSet::new));
        RateLimiter requestRateLimiter = new RateLimiter(rules);
        assertFalse(requestRateLimiter.overLimitWhenIncremented("User1:/api/v1/developers"));
        assertFalse(requestRateLimiter.overLimitWhenIncremented("User2:/api/v1/developers"));
        assertFalse(requestRateLimiter.overLimitWhenIncremented("User1:/api/v1/organizations"));
        assertFalse(requestRateLimiter.overLimitWhenIncremented("User2:/api/v1/organizations"));
    }

    @Test
    public void rateLimiter_invalidRequest_complexDefinition() {
        Set<String> keysUser1 = Stream.of("User1:/api/v1/developers", "User1:/api/v1/organizations")
                .collect(Collectors.toCollection(HashSet::new));
        Set<String> keysUser2 = Stream.of("User2:/api/v1/developers", "User2:/api/v1/organizations")
                .collect(Collectors.toCollection(HashSet::new));
        RequestLimitRule rule = new RequestLimitRule(60, 4, keysUser1);
        RequestLimitRule rule2 = new RequestLimitRule(30, 5, keysUser2);
        Set<RequestLimitRule> rules = Stream.of(rule, rule2)
                .collect(Collectors.toCollection(HashSet::new));
        RateLimiter requestRateLimiter = new RateLimiter(rules);
        assertFalse(requestRateLimiter.overLimitWhenIncremented("User1:/api/v1/developers", 2));
        assertFalse(requestRateLimiter.overLimitWhenIncremented("User1:/api/v1/developers", 2));
        assertTrue(requestRateLimiter.overLimitWhenIncremented("User1:/api/v1/developers", 2));

        assertFalse(requestRateLimiter.overLimitWhenIncremented("User2:/api/v1/developers", 2));
        assertFalse(requestRateLimiter.overLimitWhenIncremented("User2:/api/v1/developers", 2));
        assertTrue(requestRateLimiter.overLimitWhenIncremented("User2:/api/v1/developers", 2));

        assertFalse(requestRateLimiter.overLimitWhenIncremented("User1:/api/v1/organizations", 2));
        assertFalse(requestRateLimiter.overLimitWhenIncremented("User1:/api/v1/organizations", 2));
        assertTrue(requestRateLimiter.overLimitWhenIncremented("User1:/api/v1/organizations", 2));

        assertFalse(requestRateLimiter.overLimitWhenIncremented("User2:/api/v1/organizations", 2));
        assertFalse(requestRateLimiter.overLimitWhenIncremented("User2:/api/v1/organizations", 2));
        assertTrue(requestRateLimiter.overLimitWhenIncremented("User2:/api/v1/organizations", 2));
    }

    //Execute the following test only in demand to test RateLimiter with time
    @Test
    @Ignore
    public void rateLimiter_invalidRequest_simpleDefinition_overLimitMultipleWithTime() throws InterruptedException {
        Set<String> keys = Stream.of("User1:/api/v1/developers")
                .collect(Collectors.toCollection(HashSet::new));
        RequestLimitRule rule = new RequestLimitRule(10, 4, keys);
        Set<RequestLimitRule> rules = Stream.of(rule)
                .collect(Collectors.toCollection(HashSet::new));
        RateLimiter requestRateLimiter = new RateLimiter(rules);
        assertFalse(requestRateLimiter.overLimitWhenIncremented("User1:/api/v1/developers", 2));
        assertFalse(requestRateLimiter.overLimitWhenIncremented("User1:/api/v1/developers", 2));
        assertTrue(requestRateLimiter.overLimitWhenIncremented("User1:/api/v1/developers", 2));
        Thread.sleep(10000);
        boolean overLimit4 = requestRateLimiter.overLimitWhenIncremented("User1:/api/v1/developers", 2);
        assertFalse(overLimit4);
    }

    //Execute the following test only in demand to test RateLimiter with time
    @Test
    @Ignore
    public void rateLimiter_invalidRequest_complexDefinition_overLimitMultipleWithTime() throws InterruptedException {
        Set<String> keysUser1 = Stream.of("User1:/api/v1/developers")
                .collect(Collectors.toCollection(HashSet::new));
        Set<String> keysUser2 = Stream.of("User2:/api/v1/developers")
                .collect(Collectors.toCollection(HashSet::new));
        RequestLimitRule rule = new RequestLimitRule(10, 4, keysUser1);
        RequestLimitRule rule2 = new RequestLimitRule(10, 5, keysUser2);
        Set<RequestLimitRule> rules = Stream.of(rule, rule2)
                .collect(Collectors.toCollection(HashSet::new));
        RateLimiter requestRateLimiter = new RateLimiter(rules);

        assertFalse(requestRateLimiter.overLimitWhenIncremented("User1:/api/v1/developers", 2));
        assertFalse(requestRateLimiter.overLimitWhenIncremented("User1:/api/v1/developers", 2));
        assertTrue(requestRateLimiter.overLimitWhenIncremented("User1:/api/v1/developers", 2));

        assertFalse(requestRateLimiter.overLimitWhenIncremented("User2:/api/v1/developers", 2));
        assertFalse(requestRateLimiter.overLimitWhenIncremented("User2:/api/v1/developers", 2));
        assertTrue(requestRateLimiter.overLimitWhenIncremented("User2:/api/v1/developers", 2));

        Thread.sleep(10000);

        assertFalse(requestRateLimiter.overLimitWhenIncremented("User1:/api/v1/developers", 2));
        assertFalse(requestRateLimiter.overLimitWhenIncremented("User2:/api/v1/developers", 2));
    }

    @Test
    public void rateLimiter_invalidRequest_simpleDefinition_overLimitMultipleWithTimeReset() throws InterruptedException {
        Set<String> keys = Stream.of("User1:/api/v1/developers")
                .collect(Collectors.toCollection(HashSet::new));
        RequestLimitRule rule = new RequestLimitRule(10, 4, keys);
        Set<RequestLimitRule> rules = Stream.of(rule)
                .collect(Collectors.toCollection(HashSet::new));
        RateLimiter requestRateLimiter = new RateLimiter(rules);
        assertFalse(requestRateLimiter.overLimitWhenIncremented("User1:/api/v1/developers", 2));
        assertFalse(requestRateLimiter.overLimitWhenIncremented("User1:/api/v1/developers", 2));
        assertTrue(requestRateLimiter.overLimitWhenIncremented("User1:/api/v1/developers", 2));
        requestRateLimiter.resetLimit("User1:/api/v1/developers");
        assertFalse(requestRateLimiter.overLimitWhenIncremented("User1:/api/v1/developers", 2));
    }

    @Test
    public void rateLimiter_invalidRequest_complexDefinition_overLimitMultipleWithTimeReset() throws InterruptedException {
        Set<String> keysUser1 = Stream.of("User1:/api/v1/developers")
                .collect(Collectors.toCollection(HashSet::new));
        Set<String> keysUser2 = Stream.of("User2:/api/v1/developers")
                .collect(Collectors.toCollection(HashSet::new));
        RequestLimitRule rule = new RequestLimitRule(10, 4, keysUser1);
        RequestLimitRule rule2 = new RequestLimitRule(10, 5, keysUser2);
        Set<RequestLimitRule> rules = Stream.of(rule, rule2)
                .collect(Collectors.toCollection(HashSet::new));
        RateLimiter requestRateLimiter = new RateLimiter(rules);

        assertFalse(requestRateLimiter.overLimitWhenIncremented("User1:/api/v1/developers", 2));
        assertFalse(requestRateLimiter.overLimitWhenIncremented("User1:/api/v1/developers", 2));
        assertTrue(requestRateLimiter.overLimitWhenIncremented("User1:/api/v1/developers", 2));

        assertFalse(requestRateLimiter.overLimitWhenIncremented("User2:/api/v1/developers", 2));
        assertFalse(requestRateLimiter.overLimitWhenIncremented("User2:/api/v1/developers", 2));
        assertTrue(requestRateLimiter.overLimitWhenIncremented("User2:/api/v1/developers", 2));

        requestRateLimiter.resetLimit("User1:/api/v1/developers");
        requestRateLimiter.resetLimit("User2:/api/v1/developers");

        assertFalse(requestRateLimiter.overLimitWhenIncremented("User1:/api/v1/developers", 2));
        assertFalse(requestRateLimiter.overLimitWhenIncremented("User2:/api/v1/developers", 2));
    }
}
