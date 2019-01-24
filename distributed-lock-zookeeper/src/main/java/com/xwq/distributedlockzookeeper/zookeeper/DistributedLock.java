package com.xwq.distributedlockzookeeper.zookeeper;

/**
 * 定义分布式锁方法的类
 * @author by Joney on 2019/1/24 16:20
 */
public abstract class DistributedLock {

    /**
     * 尝试获得锁，一直阻塞，直到获得锁为止
     * @return
     */
    public abstract void acquire();

    /**
     * 尝试获得锁，如果不能获得也立即返回
     */
    public abstract boolean tryAcquire();

    /**
     * 在有效期时间内尝试获取锁，如果不能获得也立即返回
     * @param timeout
     * @return
     */
    public abstract boolean tryAcquire(int timeout);

    /**
     * 释放锁
     */
    public abstract void release();
}
