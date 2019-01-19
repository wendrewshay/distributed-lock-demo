package com.xwq.distributedlockredisson.service;

import com.xwq.distributedlockredisson.redisson.RedissonManager;
import com.xwq.distributedlockredisson.redisson.RedissonTool;
import org.redisson.Redisson;
import org.springframework.stereotype.Service;

@Service
public class DistributedLockRedissonService {

    public void test(String clientId) {
        Redisson redisson = RedissonManager.getRedisson();
        RedissonTool.acquire(clientId);
        // 业务逻辑
        int aaa = Integer.parseInt((String)redisson.getBucket("aaa").get());
        if (aaa > 0) {
            redisson.getBucket("aaa").set(aaa - 1);
            System.out.println("clientId:" + clientId + " - value:" + (aaa-1));
        }
        RedissonTool.release(clientId);
    }

}

