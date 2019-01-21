package com.xwq.distributedlockredisson.service;

import com.xwq.distributedlockredisson.redisson.RedissonManager;
import com.xwq.distributedlockredisson.redisson.RedissonTool;
import org.redisson.Redisson;
import org.redisson.api.RBucket;
import org.springframework.stereotype.Service;

@Service
public class DistributedLockRedissonService {


    public void test(String clientId) {
        Redisson redisson = RedissonManager.getRedisson();
        // 获取锁
        RedissonTool.acquire(redisson, clientId);
        // 业务逻辑
        RBucket<String> bucket = redisson.getBucket("aaa");
        int aaa = Integer.parseInt(bucket.get());
        if (aaa > 0) {
            redisson.getBucket("aaa").set(aaa - 1);
            System.out.println("clientId:" + clientId + " - value:" + (aaa-1));
        }
        // 释放锁
        RedissonTool.release(redisson, clientId);
    }

}

