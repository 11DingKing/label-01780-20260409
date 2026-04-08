package com.help.mp.util;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * 基于 Redis 的滑动窗口限流器。
 * 用于接口防刷和反滥用。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RateLimiter {

    private final StringRedisTemplate redisTemplate;

    @Value("${app.rate-limit.enabled:true}")
    private boolean enabled;

    /**
     * 检查是否允许操作。
     * @param key     限流 key，如 "publish:userId:123"
     * @param limit   窗口内最大次数
     * @param window  窗口时长
     * @return true=允许, false=被限流
     */
    public boolean isAllowed(String key, int limit, Duration window) {
        if (!enabled) return true;
        try {
            String redisKey = "rate:" + key;
            Long count = redisTemplate.opsForValue().increment(redisKey);
            if (count == null) return true;
            if (count == 1) {
                redisTemplate.expire(redisKey, window);
            }
            if (count > limit) {
                log.warn("Rate limit exceeded: key={}, count={}, limit={}", key, count, limit);
                return false;
            }
            return true;
        } catch (Exception e) {
            // Redis 不可用时放行，不影响业务
            log.warn("Rate limiter error (allowing request): {}", e.getMessage());
            return true;
        }
    }
}
