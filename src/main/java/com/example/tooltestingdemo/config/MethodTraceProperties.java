package com.example.tooltestingdemo.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

@Data
@ConfigurationProperties(prefix = "app.method-trace")
public class MethodTraceProperties {

    /**
     * 是否启用方法追踪
     */
    private boolean enabled = true;

    /**
     * 需要追踪的方法所在包前缀
     */
    private List<String> includePackages = new ArrayList<>(List.of(
            "com.example.tooltestingdemo.service.impl.template"
    ));

    /**
     * 是否打印入参摘要
     */
    private boolean logArgs = true;

    /**
     * 是否打印返回值摘要
     */
    private boolean logResult = false;

    /**
     * 单个参数/返回值摘要的最大长度
     */
    private int maxLength = 300;
}
