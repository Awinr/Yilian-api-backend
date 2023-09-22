package com.Reflux.ReApiGateway.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RedissonConfig {
    @Bean
    public RedissonClient redissonClient(){
        // 配置
        Config config = new Config();
        config.useSingleServer().setAddress("redis://122.51.215.230:6379").setPassword("123456").setDatabase(2);
        // 创建RedissonClient对象
        return Redisson.create(config);
    }
}
