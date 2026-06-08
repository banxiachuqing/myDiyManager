package com.ruoyi.system.ai.service;

public interface IWhitelistService {
    void add(String sessionId, String fingerprint, int ttlSec);
    boolean contains(String sessionId, String fingerprint);
    void clear(String sessionId);
}
