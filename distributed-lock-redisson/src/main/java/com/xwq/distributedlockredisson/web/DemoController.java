package com.xwq.distributedlockredisson.web;

import com.xwq.distributedlockredisson.service.DistributedLockRedissonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DemoController {

    @Autowired
    private DistributedLockRedissonService distributedLockRedissonService;

    @GetMapping(value="/test")
    public String test(String clientId) {
        distributedLockRedissonService.test(clientId);
        return "{\"code\":\"200\", \"message\":\"成功\"}";
    }
}
