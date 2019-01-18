package com.xwq.distributedlockredis.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import redis.clients.jedis.JedisPool;

@Service
public class DemoService {

    @Autowired
    private JedisPool jedisPool;
}
