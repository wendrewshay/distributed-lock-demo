package com.xwq.distributedlockredis.web;

import com.xwq.distributedlockredis.service.DistributedLockRedisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DemoController {

    @Autowired
    private DistributedLockRedisService distributedLockRedisService;

    @GetMapping(value="/test")
    public String test(String clientId) {
        distributedLockRedisService.test(clientId);
        return "{\"code\":\"200\", \"message\":\"成功\"}";
    }
}
