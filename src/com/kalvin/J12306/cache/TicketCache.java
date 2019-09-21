package com.kalvin.J12306.cache;

import java.util.HashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 自定义车票信息缓存类
 * Create by Kalvin on 2019/9/20.
 */
public class TicketCache {

    private static volatile TicketCache ticketCacheInstance;

    private HashMap<String, Object> cacheMap = new HashMap<>();
    private ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();

    /**
     * 获取缓存实例（单例）
     * @return ticketCacheInstance
     */
    public static TicketCache getInstance() {
        if (ticketCacheInstance == null) {
            synchronized (TicketCache.class) {
                if (ticketCacheInstance == null) {
                    ticketCacheInstance = new TicketCache();
                }
            }
        }
        return ticketCacheInstance;
    }

    public void put(String key, Object value) {
        this.put(key, value, 0);
    }

    public synchronized void put(String key, Object value, long expire) {

        if (expire > 0) {   // 过期缓存
            scheduledExecutorService.schedule(() -> {
                this.cacheMap.remove(key);
            }, expire, TimeUnit.SECONDS);

            this.cacheMap.put(key, value);
        } else {    // 不过期缓存
            this.cacheMap.put(key, value);
        }
    }

    public Object get(String key) {
        return this.cacheMap.get(key);
    }

    public int size() {
        return this.cacheMap.size();
    }

}
