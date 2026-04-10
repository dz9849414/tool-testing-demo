package com.example.tooltestingdemo.service.template.engine.interceptor;

import com.example.tooltestingdemo.service.template.engine.core.ExecutionResult;
import com.example.tooltestingdemo.service.template.engine.core.TemplateContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 变量解析拦截器
 * 
 * 在执行前解析模板中的内置变量，如 {{$timestamp}}、{{$uuid}} 等
 * 
 * <p>支持的变量：</p>
 * <ul>
 *   <li>{{$timestamp}} - 当前时间戳（毫秒）</li>
 *   <li>{{$timestampSec}} - 当前时间戳（秒）</li>
 *   <li>{{$uuid}} - UUID</li>
 *   <li>{{$randomInt}} - 随机整数</li>
 *   <li>{{$randomInt:min:max}} - 指定范围随机整数</li>
 *   <li>{{$randomString:length}} - 随机字符串</li>
 *   <li>{{$datetime}} - 当前日期时间</li>
 *   <li>{{$datetime:format}} - 指定格式日期时间</li>
 *   <li>{{$env:KEY}} - 环境变量</li>
 * </ul>
 * 
 * @author PDM接口测试工具
 * @since 1.0
 */
@Slf4j
@Component
@Order(100)
public class VariableResolverInterceptor implements ExecutionInterceptor {

    /**
     * 变量匹配正则：{{$varName}} 或 {{$varName:param1:param2}}
     */
    private static final Pattern VARIABLE_PATTERN = Pattern.compile("\\{\\{\\$([^}]+)\\}\\}");

    private static final DateTimeFormatter DEFAULT_DATETIME_FORMAT = 
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public void beforeExecute(TemplateContext context) {
        log.debug("执行变量解析拦截器");
        
        Map<String, String> resolvedVariables = new HashMap<>();
        
        // 解析内置变量并存储到上下文
        resolvedVariables.put("timestamp", String.valueOf(System.currentTimeMillis()));
        resolvedVariables.put("timestampSec", String.valueOf(System.currentTimeMillis() / 1000));
        resolvedVariables.put("uuid", UUID.randomUUID().toString().replace("-", ""));
        resolvedVariables.put("randomInt", String.valueOf(new Random().nextInt(100000)));
        resolvedVariables.put("datetime", LocalDateTime.now().format(DEFAULT_DATETIME_FORMAT));
        
        // 将内置变量存入上下文
        resolvedVariables.forEach(context::setTemplateVariable);
        
        // 设置到属性中，供其他拦截器使用
        context.setAttribute("_resolvedVariables", resolvedVariables);
        
        log.debug("变量解析完成: {}", resolvedVariables.keySet());
    }

    @Override
    public void afterExecute(TemplateContext context, ExecutionResult result) {
        // 变量解析拦截器在执行后不需要操作
    }

    @Override
    public int getOrder() {
        return 100;
    }

    /**
     * 解析字符串中的变量占位符
     * 
     * @param content 原始内容
     * @param context 执行上下文
     * @return 替换后的内容
     */
    public static String resolveVariables(String content, TemplateContext context) {
        if (!StringUtils.hasText(content)) {
            return content;
        }

        StringBuffer result = new StringBuffer();
        Matcher matcher = VARIABLE_PATTERN.matcher(content);

        while (matcher.find()) {
            String varExpression = matcher.group(1);
            String replacement = resolveVariable(varExpression, context);
            if (replacement != null) {
                matcher.appendReplacement(result, Matcher.quoteReplacement(replacement));
            }
        }
        matcher.appendTail(result);

        return result.toString();
    }

    /**
     * 解析单个变量表达式
     */
    private static String resolveVariable(String expression, TemplateContext context) {
        if (!StringUtils.hasText(expression)) {
            return null;
        }

        String[] parts = expression.split(":");
        String varName = parts[0].trim();

        switch (varName.toLowerCase()) {
            case "timestamp":
                return String.valueOf(System.currentTimeMillis());

            case "timestampsec":
                return String.valueOf(System.currentTimeMillis() / 1000);

            case "uuid":
                return UUID.randomUUID().toString().replace("-", "");

            case "randomint":
                return resolveRandomInt(parts);

            case "randomstring":
                return resolveRandomString(parts);

            case "datetime":
                return resolveDateTime(parts);

            case "env":
                return resolveEnvVariable(parts, context);

            default:
                // 尝试从上下文变量中获取
                Object value = context.getVariable(varName);
                return value != null ? value.toString() : null;
        }
    }

    private static String resolveRandomInt(String[] parts) {
        int min = 0;
        int max = 100000;
        
        if (parts.length >= 2) {
            try {
                min = Integer.parseInt(parts[1].trim());
            } catch (NumberFormatException ignored) {
            }
        }
        if (parts.length >= 3) {
            try {
                max = Integer.parseInt(parts[2].trim());
            } catch (NumberFormatException ignored) {
            }
        }
        
        return String.valueOf(min + new Random().nextInt(max - min + 1));
    }

    private static String resolveRandomString(String[] parts) {
        int length = 10;
        if (parts.length >= 2) {
            try {
                length = Integer.parseInt(parts[1].trim());
            } catch (NumberFormatException ignored) {
            }
        }
        
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder sb = new StringBuilder();
        Random random = new Random();
        
        for (int i = 0; i < length; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        
        return sb.toString();
    }

    private static String resolveDateTime(String[] parts) {
        LocalDateTime now = LocalDateTime.now();
        
        if (parts.length >= 2) {
            String format = parts[1].trim();
            try {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format);
                return now.format(formatter);
            } catch (Exception e) {
                log.warn("日期格式解析失败: {}", format);
            }
        }
        
        return now.format(DEFAULT_DATETIME_FORMAT);
    }

    private static String resolveEnvVariable(String[] parts, TemplateContext context) {
        if (parts.length < 2) {
            return null;
        }
        
        String key = parts[1].trim();
        
        // 首先尝试从环境变量获取
        String value = System.getenv(key);
        if (value != null) {
            return value;
        }
        
        // 尝试从系统属性获取
        value = System.getProperty(key);
        if (value != null) {
            return value;
        }
        
        // 尝试从上下文变量获取
        Object ctxValue = context.getVariable(key);
        if (ctxValue != null) {
            return ctxValue.toString();
        }
        
        return null;
    }
}
