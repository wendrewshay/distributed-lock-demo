package com.xwq.distributedlockredisson.redisson;

import org.redisson.Redisson;
import org.redisson.api.RLock;

import java.util.concurrent.TimeUnit;

/**
 * Redisson锁实现工具类
 * @author by Joney on 2019/1/19 18:37
 */
public class RedissonTool {
    /**
     * 锁名称前缀
     */
    private final static String LOCK_PREFIX = "redisson_lock_";

    /**
     * 获取锁
     * @param lockName 锁名
     * @return boolean
     * @author by Joney on 2019/1/19 18:42
     */
    public static boolean acquire(Redisson redisson, String lockName) {
        String key = LOCK_PREFIX + lockName;
        RLock lock = redisson.getLock(key);
        lock.lockAsync(3, TimeUnit.MINUTES);
        System.out.println("===lock===" + Thread.currentThread().getName());
        return true;
    }

    /**
     * 释放锁
     * @param lockName 锁名
     * @author by Joney on 2019/1/19 18:43
     */
    public static void release(Redisson redisson, String lockName) {
        String key = LOCK_PREFIX + lockName;
        RLock lock = redisson.getLock(key);
        lock.unlock();
        System.out.println("===unlock===" + Thread.currentThread().getName());
    }
}
