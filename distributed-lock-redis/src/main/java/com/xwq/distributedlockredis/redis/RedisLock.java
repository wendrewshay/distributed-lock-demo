package com.xwq.distributedlockredis.redis;

import com.github.rholder.retry.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.NamedThreadLocal;
import org.springframework.stereotype.Component;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.util.Collections;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * A distributed lock implemented by jedis client api.
 * retry policy supported.
 * Created by wendrewshay on 2019/7/24 16:00
 */
@Component
public class RedisLock {

    private static Logger logger = LoggerFactory.getLogger(RedisLock.class);

    private static final String SET_IF_NOT_EXIST = "NX";
    private static final String SET_WITH_EXPIRE_TIME = "PX";
    /**
     * 默认redis锁键
     */
    private static final String DEFAULT_LOCK_KEY = "default_redis_lock_key";
    /**
     * 默认有效期
     */
    private static final int DEFAULT_EXPIRE_TIME = 5;
    /**
     * 获取锁成功标识
     */
    private static final String LOCK_SUCCESS = "OK";
    /**
     * 释放锁成功标识
     */
    private static final Long UNLOCK_SUCCESS = 1L;
    /**
     * 当前线程中的redis连接
     */
    private static ThreadLocal<Jedis> currJedis = new NamedThreadLocal<Jedis>("RedisLock"){
        @Override
        protected Jedis initialValue() {
            return null;
        }
    };

    /**
     * 定义重试策略
     */
    private static Retryer<Boolean> retryer = RetryerBuilder.<Boolean>newBuilder()
            .retryIfException()
            .retryIfResult(aBoolean -> Objects.equals(aBoolean, false))
            .withStopStrategy(StopStrategies.stopAfterAttempt(3))
            .withWaitStrategy(WaitStrategies.fixedWait(50, TimeUnit.MILLISECONDS))
            .withRetryListener(new RetryListener() {
                @Override
                public <V> void onRetry(Attempt<V> attempt) {
                    logger.info(">>> 线程-{}尝试第{}次获取锁，结果：{}", Thread.currentThread().getId(), attempt.getAttemptNumber(), attempt.getResult());
//                    if (attempt.hasException()) {
//                        logger.error(">>> retry attempt exception: {}", attempt.getExceptionCause().getMessage(), attempt.getExceptionCause());
//                    }
                }
            }).build();

    private final JedisPool jedisPool;
    public RedisLock(JedisPool jedisPool) {
        this.jedisPool = jedisPool;
    }

    /**
     * 获取锁
     * @author wendrewshay
     * @date 2019/7/24 16:21
     */
    public boolean lock() {
        try {
            return retryer.call(doLock());
        } catch (ExecutionException | RetryException e) {
//            logger.error(">>> 获取锁异常，原因：{}", e.getMessage(), e);
        }
        logger.info("### requestId={}, get lock failed.", Thread.currentThread().getId());
        return false;
    }

    private Callable<Boolean> doLock() {
        return new Callable<Boolean>() {
            /**
             * Computes a result, or throws an exception if unable to do so.
             *
             * @return computed result
             * @throws Exception if unable to compute a result
             */
            @Override
            public Boolean call() throws Exception {
                Jedis jedis = currJedis.get();
                if (jedis == null) {
                    jedis = jedisPool.getResource();
                    currJedis.set(jedis);
                }
                String flag = jedis.set(DEFAULT_LOCK_KEY, String.valueOf(Thread.currentThread().getId()), SET_IF_NOT_EXIST, SET_WITH_EXPIRE_TIME, DEFAULT_EXPIRE_TIME);
                if (LOCK_SUCCESS.equalsIgnoreCase(flag)) {
                    logger.info("### requestId={}, get lock succeed.", Thread.currentThread().getId());
                    return true;
                }
                return false;
            }
        };
    }

    /**
     * 释放锁
     * @author wendrewshay
     * @date 2019/7/24 16:21
     */
    public boolean unlock() {
        Jedis jedis = currJedis.get();
        if (jedis == null) {
            jedis = this.jedisPool.getResource();
        }
        try{
            String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
            Object flag = jedis.eval(script, Collections.singletonList(DEFAULT_LOCK_KEY), Collections.singletonList(String.valueOf(Thread.currentThread().getId())));
            if (UNLOCK_SUCCESS.equals(flag)) {
                logger.info("### requestId={}, release lock succeed.", Thread.currentThread().getId());
                return true;
            }
            logger.info("### requestId={}, release lock failed.", Thread.currentThread().getId());
            return false;
        } finally {
            jedis.close();
            currJedis.remove();
        }
    }
}
