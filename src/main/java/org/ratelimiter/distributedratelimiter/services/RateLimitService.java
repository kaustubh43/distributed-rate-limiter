package org.ratelimiter.distributedratelimiter.services;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Collections;
import java.util.List;

@Service
public class RateLimitService {
    private final RedisTemplate<String, Object> redisTemplate;
    private final RedisScript<Long> rateLimitLuaScript;

    private static final String BUCKET_CAPACITY = "15";
    private static final String BUCKET_REFILL_RATE_PER_SEC = "1";
    private static final String REDIS_RATE_LIMIT_KEY = "rate_limit:client:";

    public RateLimitService(RedisTemplate<String, Object> redisTemplate, RedisScript<Long> rateLimitLuaScript) {
        this.redisTemplate = redisTemplate;
        this.rateLimitLuaScript = rateLimitLuaScript;
    }

    public boolean isAllowed(String clientId) {
        if(clientId == null) {
            return false;
        }
        List<String> keys = Collections.singletonList(REDIS_RATE_LIMIT_KEY +  clientId);
        String currentUnixTime = String.valueOf(Instant.now().getEpochSecond());

        // Execute the atomic Lua script in Redis
        Long result = redisTemplate.execute(rateLimitLuaScript,
                keys,
                BUCKET_CAPACITY,
                BUCKET_REFILL_RATE_PER_SEC,
                currentUnixTime
        );

        // 1 means script is executed successfully and token is decremented, 0 means bucket empty.
        return result != null && result == 1L;
    }
}
