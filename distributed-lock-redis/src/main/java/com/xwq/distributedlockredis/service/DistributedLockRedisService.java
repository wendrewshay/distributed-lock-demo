package com.xwq.distributedlockredis.service;

import com.xwq.distributedlockredis.redis.RedisTool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

@Service
public class DistributedLockRedisService {

    @Autowired
    private JedisPool jedisPool;

    public void test(String clientId) {
        try(Jedis jedis = jedisPool.getResource()) {
            // 获得锁
            if (RedisTool.tryGetDistributedLock(jedis, "order", clientId, 5)) {
                // 业务逻辑
                int aaa = Integer.parseInt(jedis.get("aaa"));
                if (aaa > 0) {
                    jedis.set("aaa", (aaa - 1) + "");
                    System.out.println("clientId:" + clientId + " - value:" + (aaa-1));
                }
                // 释放锁
                RedisTool.releaseDistributedLock(jedis, "order", clientId);
            }
        }
    }

}

