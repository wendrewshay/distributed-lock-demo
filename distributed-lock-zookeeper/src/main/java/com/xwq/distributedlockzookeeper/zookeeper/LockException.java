package com.xwq.distributedlockzookeeper.zookeeper;

/**
 * 自定义锁异常
 * @author by Joney on 2019/1/24 16:22
 */
public class LockException extends RuntimeException {

    public LockException(String msg, Exception e) {
        super(msg, e);
    }

    public LockException(String msg) {
        super(msg);
    }
}
