package com.xwq.distributedlockredis.redis;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;

import java.util.Collections;

/**
 * 分布式锁获取、释放工具类
 * @author by Joney on 2019/1/19 9:01
 */
public class RedisTool {

    private static Logger logger = LoggerFactory.getLogger(RedisTool.class);

    /**
     * 锁获取成功标识
     */
    private static final String LOCK_SUCCESS = "OK";
    /**
     * 释放锁成功标识
     */
    private static final Long RELEASE_SUCCESS = 1L;
    /**
     * 当key不存在时，我们进行set操作；若key已经存在，则不做任何操作。
     */
    private static final String SET_IF_NOT_EXIST = "NX";
    /**
     * 给锁的这个key加一个过期的设置，具体时间由入参决定。
     */
    private static final String SET_WITH_EXPIRE_TIME = "PX";

    /**
     * 尝试获取分布式锁
     * @param jedis      redis客户端
     * @param lockKey    锁
     * @param requestId  请求标识
     * @param expireTime 过期时间
     * @return boolean
     * @author by Joney on 2019/1/19 8:59
     */
    public static boolean tryGetDistributedLock(Jedis jedis, String lockKey, String requestId, int expireTime) {
        String result = jedis.set(lockKey, requestId, SET_IF_NOT_EXIST, SET_WITH_EXPIRE_TIME, expireTime);
        if (LOCK_SUCCESS.equals(result)) {
            logger.info("### requestId={}, get lock succeed.", requestId);
            return true;
        }
        logger.info("### requestId={}, get lock failed.", requestId);
        return false;
    }

    /**
     * 释放分布式锁
     * @param jedis     redis客户端
     * @param lockKey   锁
     * @param requestId 请求标识
     * @return boolean
     * @author by Joney on 2019/1/19 9:15
     */
    public static boolean releaseDistributedLock(Jedis jedis, String lockKey, String requestId) {
        String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
        Object result = jedis.eval(script, Collections.singletonList(lockKey), Collections.singletonList(requestId));
        if (RELEASE_SUCCESS.equals(result)) {
            logger.info("### requestId={}, release lock succeed.", requestId);
            return true;
        }
        return false;
    }
}
