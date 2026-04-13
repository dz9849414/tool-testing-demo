package com.example.tooltestingdemo;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.example.tooltestingdemo.mapper")
public class ToolTestingDemoApplication {
    public static void main(String[] args) {
        SpringApplication.run(ToolTestingDemoApplication.class, args);
    }
}