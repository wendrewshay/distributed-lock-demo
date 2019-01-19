package com.xwq.distributedlockredis;

import com.xwq.distributedlockredis.redis.RedisTool;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.util.UUID;

@RunWith(SpringRunner.class)
@SpringBootTest
public class DistributedLockRedisTests2 {

    @Autowired
    private JedisPool jedisPool;

    @Test
    public void contextLoads() {
        String uid = UUID.randomUUID().toString();
        for (int i = 0; i < 55; i++) {
            Jedis jedis = jedisPool.getResource();
            RedisTool.tryGetDistributedLock(jedis, "order", uid, 5);
            int aaa = Integer.parseInt(jedis.get("aaa"));
            if (aaa > 0) {
                jedis.set("aaa", (aaa - 1) + "");
                System.out.println("test2 - lockKey:aaa - value:" + (aaa-1));
            }
            jedis.close();
            RedisTool.releaseDistributedLock(jedis, "order", uid);
        }

    }

}

