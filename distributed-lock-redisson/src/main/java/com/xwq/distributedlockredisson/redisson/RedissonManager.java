package com.xwq.distributedlockredisson.redisson;

import org.redisson.Redisson;
import org.redisson.config.Config;

/**
 * Redisson管理器
 * @author by Joney on 2019/1/19 18:31
 */
public class RedissonManager {

    private static Config config = new Config();
    /**
     * 声明Redisson对象
     */
    private static Redisson redisson = null;

    static {
        config.useSingleServer().setAddress("192.168.22.180:6379");
        redisson = (Redisson)Redisson.create(config);
    }

    /**
     * 获取Redisson对象
     * @return Redisson
     * @author by Joney on 2019/1/19 18:36
     */
    public static Redisson getRedisson() {
        return redisson;
    }
}
