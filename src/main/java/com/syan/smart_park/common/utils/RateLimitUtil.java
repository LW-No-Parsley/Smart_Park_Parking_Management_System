package com.syan.smart_park.common.utils;

import java.util.concurrent.ConcurrentHashMap;

/**
 * 简易登录频率限制工具
 */
public class RateLimitUtil {

    private static final ConcurrentHashMap<String, long[]> cache = new ConcurrentHashMap<>();
    private static final int MAX_ATTEMPTS = 5;
    private static final long WINDOW_MS = 60_000; // 1分钟窗口

    /**
     * 检查指定key是否在频率限制内
     * @return true=允许，false=被限流
     */
    public static synchronized boolean tryAcquire(String key) {
        long now = System.currentTimeMillis();
        long[] timestamps = cache.computeIfAbsent(key, k -> new long[MAX_ATTEMPTS]);

        int count = 0;
        for (int i = 0; i < timestamps.length; i++) {
            if (timestamps[i] > 0 && now - timestamps[i] < WINDOW_MS) {
                count++;
            }
        }

        if (count >= MAX_ATTEMPTS) {
            return false;
        }

        // 写入最近一次时间戳
        for (int i = 0; i < timestamps.length; i++) {
            if (timestamps[i] == 0 || now - timestamps[i] >= WINDOW_MS) {
                timestamps[i] = now;
                break;
            }
        }
        return true;
    }
}
