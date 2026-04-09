package com.help.mp.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class RateLimiterTest {

    private RateLimiter rateLimiter;
    private TestStringRedisTemplate redisTemplate;

    @BeforeEach
    void setUp() {
        redisTemplate = new TestStringRedisTemplate();
        rateLimiter = new RateLimiter(redisTemplate);
        ReflectionTestUtils.setField(rateLimiter, "enabled", true);
    }

    @Test
    void isAllowed_withinLimit_returnsTrue() {
        boolean result = rateLimiter.isAllowed("test:key", 5, Duration.ofMinutes(1));

        assertTrue(result);
        assertEquals(1L, redisTemplate.getCounter("rate:test:key"));
        assertTrue(redisTemplate.hasExpiration("rate:test:key"));
    }

    @Test
    void isAllowed_exceedsLimit_returnsFalse() {
        for (int i = 0; i < 5; i++) {
            assertTrue(rateLimiter.isAllowed("test:key", 5, Duration.ofMinutes(1)));
        }

        boolean result = rateLimiter.isAllowed("test:key", 5, Duration.ofMinutes(1));

        assertFalse(result);
        assertEquals(6L, redisTemplate.getCounter("rate:test:key"));
    }

    @Test
    void isAllowed_atLimit_returnsTrue() {
        for (int i = 0; i < 4; i++) {
            rateLimiter.isAllowed("test:key", 5, Duration.ofMinutes(1));
        }

        boolean result = rateLimiter.isAllowed("test:key", 5, Duration.ofMinutes(1));

        assertTrue(result);
        assertEquals(5L, redisTemplate.getCounter("rate:test:key"));
    }

    @Test
    void isAllowed_disabled_alwaysReturnsTrue() {
        ReflectionTestUtils.setField(rateLimiter, "enabled", false);

        for (int i = 0; i < 10; i++) {
            assertTrue(rateLimiter.isAllowed("test:key", 1, Duration.ofMinutes(1)));
        }

        assertEquals(0, redisTemplate.getOperationCount());
    }

    @Test
    void isAllowed_redisException_returnsTrue() {
        redisTemplate.setThrowException(true);

        boolean result = rateLimiter.isAllowed("test:key", 5, Duration.ofMinutes(1));

        assertTrue(result);
    }

    @Test
    void isAllowed_firstCall_setsExpiration() {
        rateLimiter.isAllowed("test:key", 5, Duration.ofMinutes(1));

        assertTrue(redisTemplate.hasExpiration("rate:test:key"));
    }

    @Test
    void isAllowed_subsequentCall_doesNotResetExpiration() {
        rateLimiter.isAllowed("test:key", 5, Duration.ofMinutes(1));
        long firstExpireTime = redisTemplate.getExpireTime("rate:test:key");

        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        rateLimiter.isAllowed("test:key", 5, Duration.ofMinutes(1));
        long secondExpireTime = redisTemplate.getExpireTime("rate:test:key");

        assertEquals(firstExpireTime, secondExpireTime);
    }

    @Test
    void isAllowed_concurrentAccess_limitsCorrectly() throws InterruptedException {
        int threadCount = 100;
        int limit = 10;
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch endLatch = new CountDownLatch(threadCount);

        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        AtomicInteger allowedCount = new AtomicInteger(0);
        AtomicInteger deniedCount = new AtomicInteger(0);

        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    startLatch.await();
                    boolean allowed = rateLimiter.isAllowed("concurrent:test", limit, Duration.ofMinutes(1));
                    if (allowed) {
                        allowedCount.incrementAndGet();
                    } else {
                        deniedCount.incrementAndGet();
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    endLatch.countDown();
                }
            });
        }

        startLatch.countDown();
        endLatch.await();
        executor.shutdown();

        assertEquals(limit, allowedCount.get());
        assertEquals(threadCount - limit, deniedCount.get());
    }

    @Test
    void isAllowed_differentKeys_independentLimits() {
        for (int i = 0; i < 3; i++) {
            assertTrue(rateLimiter.isAllowed("key1", 5, Duration.ofMinutes(1)));
            assertTrue(rateLimiter.isAllowed("key2", 3, Duration.ofMinutes(1)));
        }

        assertTrue(rateLimiter.isAllowed("key1", 5, Duration.ofMinutes(1)));
        assertTrue(rateLimiter.isAllowed("key1", 5, Duration.ofMinutes(1)));
        assertFalse(rateLimiter.isAllowed("key2", 3, Duration.ofMinutes(1)));
    }

    @Test
    void isAllowed_nullKey_usesNullInRedisKey() {
        rateLimiter.isAllowed(null, 5, Duration.ofMinutes(1));

        assertEquals(1L, redisTemplate.getCounter("rate:null"));
    }

    @Test
    void isAllowed_emptyKey_usesEmptyInRedisKey() {
        rateLimiter.isAllowed("", 5, Duration.ofMinutes(1));

        assertEquals(1L, redisTemplate.getCounter("rate:"));
    }

    @Test
    void isAllowed_zeroLimit_alwaysDeniedAfterFirstCall() {
        boolean result = rateLimiter.isAllowed("test:key", 0, Duration.ofMinutes(1));

        assertFalse(result);
    }

    @Test
    void isAllowed_negativeLimit_alwaysDeniedAfterFirstCall() {
        boolean result = rateLimiter.isAllowed("test:key", -1, Duration.ofMinutes(1));

        assertFalse(result);
    }

    private static class TestStringRedisTemplate extends StringRedisTemplate {
        private final ConcurrentHashMap<String, AtomicLong> counters = new ConcurrentHashMap<>();
        private final ConcurrentHashMap<String, Long> expireTimes = new ConcurrentHashMap<>();
        private boolean throwException = false;
        private final AtomicInteger operationCount = new AtomicInteger(0);
        private final ValueOperations<String, String> valueOps;

        public TestStringRedisTemplate() {
            super(mock(RedisConnectionFactory.class));
            this.valueOps = createValueOperations();
        }

        @SuppressWarnings("unchecked")
        private ValueOperations<String, String> createValueOperations() {
            return mock(ValueOperations.class, invocation -> {
                String methodName = invocation.getMethod().getName();
                if ("increment".equals(methodName)) {
                    if (throwException) {
                        throw new RuntimeException("Redis connection failed");
                    }
                    operationCount.incrementAndGet();
                    String key = invocation.getArgument(0);
                    return counters.computeIfAbsent(key, k -> new AtomicLong(0)).incrementAndGet();
                }
                throw new UnsupportedOperationException("Method not implemented: " + methodName);
            });
        }

        @Override
        public ValueOperations<String, String> opsForValue() {
            return valueOps;
        }

        @Override
        public Boolean expire(String key, Duration timeout) {
            operationCount.incrementAndGet();
            expireTimes.put(key, System.currentTimeMillis() + timeout.toMillis());
            return true;
        }

        public void setThrowException(boolean throwException) {
            this.throwException = throwException;
        }

        public long getCounter(String key) {
            AtomicLong counter = counters.get(key);
            return counter != null ? counter.get() : 0L;
        }

        public boolean hasExpiration(String key) {
            return expireTimes.containsKey(key);
        }

        public long getExpireTime(String key) {
            Long time = expireTimes.get(key);
            return time != null ? time : 0L;
        }

        public int getOperationCount() {
            return operationCount.get();
        }
    }
}
