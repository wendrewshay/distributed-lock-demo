package com.xwq.distributedlockredisson;

import com.xwq.distributedlockredisson.redisson.RedissonManager;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class DistributedLockRedissonApplication implements CommandLineRunner {

    public static void main(String[] args) {
        SpringApplication.run(DistributedLockRedissonApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        RedissonManager.init();
    }
}

