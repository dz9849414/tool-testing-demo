package com.example.tooltestingdemo.config;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.jedis.JedisClientConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;

import java.time.Duration;

@Configuration
public class RedisConfig {

    @Value("${spring.redis.host:redis}")
    private String host;

    @Value("${spring.redis.port:6379}")
    private int port;

    @Value("${spring.redis.database:0}")
    private int database;

    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        RedisStandaloneConfiguration config = new RedisStandaloneConfiguration();
        config.setHostName(host);
        config.setPort(port);
        config.setDatabase(database);

        JedisClientConfiguration clientConfig = JedisClientConfiguration.builder()
                .connectTimeout(Duration.ofSeconds(5))
                .build();

        return new JedisConnectionFactory(config, clientConfig);
    }
}
