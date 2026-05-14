package com.example.tooltestingdemo.config;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.jedis.JedisClientConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;

import java.time.Duration;

@Configuration
public class RedisConfig {

    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        RedisStandaloneConfiguration config = new RedisStandaloneConfiguration();
        config.setHostName(System.getenv("SPRING_REDIS_HOST"));
        config.setPort(Integer.parseInt(System.getenv("SPRING_REDIS_PORT")));
        config.setDatabase(Integer.parseInt(System.getenv("SPRING_REDIS_DATABASE")));

        JedisClientConfiguration clientConfig = JedisClientConfiguration.builder()
                .connectTimeout(Duration.ofSeconds(5))
                .build();

        return new JedisConnectionFactory(config, clientConfig);
    }
}