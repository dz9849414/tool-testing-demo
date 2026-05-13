package com.example.tooltestingdemo;

import org.mybatis.spring.annotation.MapperScan;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan({"com.example.tooltestingdemo.mapper", "com.example.tooltestingdemo.mapper.system"})
public class ToolTestingDemoApplication {
    private static final Logger LOGGER = LoggerFactory.getLogger(ToolTestingDemoApplication.class);
    public static void main(String[] args) {
        SpringApplication.run(ToolTestingDemoApplication.class, args);
        LOGGER.info("Application started");
    }
}