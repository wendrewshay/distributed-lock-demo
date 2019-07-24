package com.xwq.distributedlockredis;

import com.xwq.distributedlockredis.redis.RedisLock;
import com.xwq.distributedlockredis.redis.RedisTool;
import net.sourceforge.groboutils.junit.v1.MultiThreadedTestRunner;
import net.sourceforge.groboutils.junit.v1.TestRunnable;
import org.checkerframework.checker.units.qual.A;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.util.UUID;

@RunWith(SpringRunner.class)
@SpringBootTest
public class DistributedLockTest {

    @Autowired
    private JedisPool jedisPool;

    private static Logger logger = LoggerFactory.getLogger(DistributedLockTest.class);
    /**
     * 锁键
     */
    private static final String LOCK_KEY = "distributedLockTest_redis";
    /**
     * 模拟共享资源
     */
    private static Integer num = 0;

    @Autowired
    private RedisLock redisLock;

    @Test
    public void run1() throws Throwable {
        TestRunnable runner = new TestRunnable() {
            @Override
            public void runTest() throws Throwable {
                if (redisLock.lock()) {
                    num ++;
                    logger.info(">>> requestId={}, num={}", Thread.currentThread().getId(), num);
                    redisLock.unlock();
                }
            }
        };
        // 10个线程同时跑，有且只有一个线程能获取到锁
        int runnerCount = 10;
        TestRunnable[] trs = new TestRunnable[runnerCount];
        for (int i = 0; i < runnerCount; i ++) {
            trs[i] = runner;
        }
        MultiThreadedTestRunner mttr = new MultiThreadedTestRunner(trs);
        mttr.runTestRunnables();
    }

    @Test
    public void run2() throws Throwable {
        TestRunnable runner = new TestRunnable() {
            @Override
            public void runTest() throws Throwable {
                String requestId = UUID.randomUUID().toString();
                try(Jedis jedis = jedisPool.getResource()) {
                    if (RedisTool.tryGetDistributedLock(jedis, LOCK_KEY, requestId, 5)) {
                        num ++;
                        logger.info(">>> requestId={}, num={}", requestId, num);
                    }
                    RedisTool.releaseDistributedLock(jedis, LOCK_KEY, requestId);
                }
            }
        };
        // 10个线程同时跑，有且只有一个线程能获取到锁
        int runnerCount = 10;
        TestRunnable[] trs = new TestRunnable[runnerCount];
        for (int i = 0; i < runnerCount; i ++) {
            trs[i] = runner;
        }
        MultiThreadedTestRunner mttr = new MultiThreadedTestRunner(trs);
        mttr.runTestRunnables();
    }
}
