package com.ruoyi.system.ai.service.impl;

import com.ruoyi.system.ai.service.IWhitelistService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class WhitelistServiceImpl implements IWhitelistService {

    private static final String KEY_PREFIX = "ai:session:whitelist:";
    private final RedisTemplate<String, String> redis;

    @Autowired
    public WhitelistServiceImpl(RedisTemplate<String, String> redis) {
        this.redis = redis;
    }

    @Override
    public void add(String sessionId, String fingerprint, int ttlSec) {
        String key = KEY_PREFIX + sessionId;
        redis.opsForHash().put(key, fingerprint, "1");
        redis.expire(key, ttlSec, TimeUnit.SECONDS);
    }

    @Override
    public boolean contains(String sessionId, String fingerprint) {
        String key = KEY_PREFIX + sessionId;
        Map<Object, Object> entries = redis.opsForHash().entries(key);
        return entries != null && entries.containsKey(fingerprint);
    }

    @Override
    public void clear(String sessionId) {
        redis.delete(KEY_PREFIX + sessionId);
    }
}
