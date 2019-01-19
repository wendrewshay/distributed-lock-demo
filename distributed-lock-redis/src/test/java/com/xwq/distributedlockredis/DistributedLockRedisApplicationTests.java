package com.xwq.distributedlockredis;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;


@RunWith(SpringRunner.class)
@SpringBootTest
public class DistributedLockRedisApplicationTests {

    @Autowired
    private JedisPool jedisPool;

    @Test
    public void init() {
        Jedis jedis = jedisPool.getResource();
        jedis.set("aaa", "100");
        System.out.println("aaa = " + jedis.get("aaa"));
        jedis.close();
    }

}

