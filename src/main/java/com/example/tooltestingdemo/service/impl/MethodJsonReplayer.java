package com.example.tooltestingdemo.service.impl;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.example.tooltestingdemo.dto.OperationLogImportDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.Comparator;
import java.util.zip.GZIPInputStream;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

@Component
@Slf4j
@RequiredArgsConstructor
public class MethodJsonReplayer {

    private final ApplicationContext applicationContext;

    public void replayByMethodJson(OperationLogImportDTO logDTO) {
        String methodJson = logDTO.getMethodJson();
        if (methodJson == null || methodJson.isEmpty()) {
            log.info("method_json为空，跳过还原");
            return;
        }

        // 检测并处理可能的压缩数据
        methodJson = decompressIfNeeded(methodJson);

        JSONArray methodArray;
        try {
            methodArray = JSON.parseArray(methodJson);
        } catch (Exception e) {
            log.error("解析method_json失败: {}", e.getMessage(), e);
            throw new RuntimeException("方法调用链还原失败: 解析JSON失败 - " + e.getMessage(), e);
        }

        int successCount = 0;
        int failureCount = 0;
        StringBuilder failureDetails = new StringBuilder();

        for (int i = 0; i < methodArray.size(); i++) {
            try {
                JSONObject methodInfo = methodArray.getJSONObject(i);
                String className = methodInfo.getString("className");
                String methodName = methodInfo.getString("methodName");

                if (className == null || methodName == null) {
                    log.warn("method_json格式错误，跳过第{}个方法调用: className={}, methodName={}", i + 1, className, methodName);
                    failureCount++;
                    if (failureDetails.length() > 0) {
                        failureDetails.append("; ");
                    }
                    failureDetails.append(String.format("方法%d: 格式错误(className=%s,methodName=%s)", i + 1, className, methodName));
                    continue;
                }

                executeMethod(className, methodName, logDTO);
                successCount++;
                log.info("成功执行第{}个方法调用: {}.{}", i + 1, className, methodName);
            } catch (Exception e) {
                failureCount++;
                String detail = String.format("方法%d: %s", i + 1, e.getMessage());
                log.error("执行第{}个方法调用失败: {}", i + 1, e.getMessage(), e);
                
                if (failureDetails.length() > 0) {
                    failureDetails.append("; ");
                }
                failureDetails.append(detail);
            }
        }

        log.info("基于method_json还原完成: 成功={}, 失败={}, 总计={}", successCount, failureCount, methodArray.size());
        
        // 如果全部失败，抛出异常，包含详细失败原因
        if (successCount == 0 && failureCount > 0) {
            String errorMsg = "方法调用链还原失败: 所有方法调用均失败";
            if (failureDetails.length() > 0) {
                errorMsg += ", 失败详情: " + failureDetails.toString();
            } else {
                errorMsg += ", 失败数=" + failureCount;
            }
            throw new RuntimeException(errorMsg);
        }
        
        // 如果部分失败，记录警告日志
        if (failureCount > 0) {
            log.warn("方法调用链还原部分失败: 成功={}, 失败={}, 失败详情: {}", successCount, failureCount, failureDetails.toString());
        }
    }

    /**
     * 检测并解压可能压缩的 method_json 数据
     * 处理场景：数据库存储的是 Gzip 压缩后的字节数组，读取时可能被转换成字符串表示
     */
    private String decompressIfNeeded(String methodJson) {
        // 如果是字节数组的字符串表示（如 [B@3398b007），这是数据损坏或转换问题
        if (methodJson.startsWith("[B@")) {
            log.error("检测到method_json是字节数组格式，无法解析: {}", methodJson);
            throw new RuntimeException("method_json格式错误: 检测到字节数组表示，无法解析");
        }
        
        // 尝试检测是否为 Base64 编码的压缩数据
        try {
            byte[] decodedBytes = Base64.getDecoder().decode(methodJson);
            // 检测是否为 Gzip 格式（Gzip 魔数：0x1F8B）
            if (decodedBytes.length >= 2 && decodedBytes[0] == (byte) 0x1F && decodedBytes[1] == (byte) 0x8B) {
                log.info("检测到Gzip压缩数据，尝试解压");
                try (GZIPInputStream gzis = new GZIPInputStream(new ByteArrayInputStream(decodedBytes))) {
                    String decompressed = new String(gzis.readAllBytes(), StandardCharsets.UTF_8);
                    log.debug("解压成功: 原始长度={}, 解压后长度={}", methodJson.length(), decompressed.length());
                    return decompressed;
                }
            }
        } catch (Exception e) {
            // 不是有效的 Base64 或 Gzip，返回原始字符串
            log.debug("method_json不是Base64编码的压缩数据，保持原样");
        }
        
        return methodJson;
    }

    private void executeMethod(String className, String methodName, OperationLogImportDTO logDTO) throws Exception {
        Class<?> targetClass = Class.forName(className);

        Object bean = getBeanFromApplicationContext(targetClass);
        if (bean == null) {
            throw new RuntimeException("找不到Bean: " + className);
        }

        // 先查找方法，获取参数类型
        Method targetMethod = findMethod(bean, methodName, logDTO.getRequestParams());
        if (targetMethod == null) {
            throw new RuntimeException("找不到方法: " + className + "." + methodName);
        }

        // 根据方法参数类型解析参数
        Object[] args = parseRequestParams(logDTO.getRequestParams(), targetMethod);

        // 检查参数数量是否匹配
        int expectedParamCount = targetMethod.getParameterTypes().length;
        if (args.length != expectedParamCount) {
            String paramTypeNames = Arrays.stream(targetMethod.getParameterTypes())
                    .map(Class::getSimpleName)
                    .collect(Collectors.joining(", "));
            throw new RuntimeException(String.format(
                    "参数数量不匹配: 方法[%s]期望%d个参数(%s)，但从requestParams[%s]中实际解析出%d个参数", 
                    methodName, expectedParamCount, paramTypeNames, 
                    logDTO.getRequestParams() != null && logDTO.getRequestParams().length() > 100 
                            ? logDTO.getRequestParams().substring(0, 100) + "..." 
                            : logDTO.getRequestParams(),
                    args.length));
        }

        // 根据ID查询，如果存在则更新，不存在则执行原方法
        boolean shouldInvoke = true;
        if (args.length > 0) {
            shouldInvoke = !checkAndUpdateIfExists(args[0], bean);
        }

        if (shouldInvoke) {
            try {
                targetMethod.invoke(bean, args);
            } catch (Exception e) {
                // 检查是否是主键冲突异常
                String errorMsg = e.getCause() != null ? e.getCause().getMessage() : e.getMessage();
                if (errorMsg != null && errorMsg.contains("Duplicate entry") && errorMsg.contains("PRIMARY")) {
                    throw new RuntimeException("记录已存在", e);
                }
                throw e;
            }
        }

        log.info("执行方法成功: {}.{}", className, methodName);
    }

    /**
     * 根据ID查询记录，如果存在则更新，返回true；不存在则返回false
     */
    private boolean checkAndUpdateIfExists(Object param, Object bean) {
        return false;
    }

    /**
     * 从ApplicationContext获取Bean
     */
    private Object getBeanFromApplicationContext(Class<?> targetClass) {
        try {
            return applicationContext.getBean(targetClass);
        } catch (Exception e) {
            log.warn("从ApplicationContext获取Bean失败: {}", targetClass.getName());
            return null;
        }
    }

    /**
     * 查找方法（支持重载方法的动态匹配）
     */
    private Method findMethod(Object bean, String methodName, String requestParams) {
        // 获取所有公共方法
        Method[] methods = bean.getClass().getMethods();
        
        // 按方法名过滤
        List<Method> matchedMethods = Arrays.stream(methods)
                .filter(m -> m.getName().equals(methodName))
                .collect(Collectors.toList());
        
        if (matchedMethods.isEmpty()) {
            log.debug("未找到方法: {}.{}", bean.getClass().getName(), methodName);
            return null;
        }
        
        // 如果只有一个匹配的方法，直接返回
        if (matchedMethods.size() == 1) {
            return matchedMethods.get(0);
        }
        
        // 有多个重载方法，根据参数数量和requestParams来选择
        return selectBestMethod(matchedMethods, requestParams);
    }

    /**
     * 从多个重载方法中选择最合适的
     */
    private Method selectBestMethod(List<Method> methods, String requestParams) {
        int paramCount = countParams(requestParams);
        
        // 优先选择参数数量匹配的方法
        List<Method> candidates = methods.stream()
                .filter(m -> m.getParameterCount() == paramCount)
                .collect(Collectors.toList());
        
        if (!candidates.isEmpty()) {
            // 如果有多个参数数量匹配的，优先选择第一个
            return candidates.get(0);
        }
        
        // 如果没有完全匹配的，选择参数数量最接近的
        return methods.stream()
                .min(Comparator.comparingInt(m -> Math.abs(m.getParameterCount() - paramCount)))
                .orElse(methods.get(0));
    }

    /**
     * 计算requestParams中的参数数量
     */
    private int countParams(String requestParams) {
        if (requestParams == null || requestParams.isEmpty()) {
            return 0;
        }
        
        try {
            JSONObject json = JSON.parseObject(requestParams);
            return json.size();
        } catch (Exception e) {
            // 如果不是JSON格式，假设只有1个参数
            return 1;
        }
    }

    /**
     * 解析请求参数（动态从requestParams中提取，支持多种格式）
     */
    private Object[] parseRequestParams(String requestParams, Method method) {
        Class<?>[] paramTypes = method.getParameterTypes();
        
        // 无参数方法
        if (paramTypes.length == 0) {
            return new Object[0];
        }
        
        // requestParams为空
        if (requestParams == null || requestParams.isEmpty()) {
            log.warn("方法需要{}个参数，但requestParams为空", paramTypes.length);
            return new Object[0];
        }
        
        try {
            // 尝试解析为JSON格式
            if (requestParams.trim().startsWith("{")) {
                return parseJsonParams(requestParams, paramTypes, method);
            }
            // 尝试解析为Java对象数组格式 [Obj1, Obj2, ...]
            else if (requestParams.trim().startsWith("[")) {
                return parseJavaArrayParams(requestParams, paramTypes, method);
            }
            // 尝试解析为Java对象格式 ClassName(field=value, ...)
            else {
                return parseJavaObjectParams(requestParams, paramTypes, method);
            }
        } catch (Exception e) {
            log.error("解析请求参数失败: {}", e.getMessage(), e);
            return new Object[0];
        }
    }

    /**
     * 解析JSON格式参数
     */
    private Object[] parseJsonParams(String requestParams, Class<?>[] paramTypes, Method method) {
        if (paramTypes.length == 1) {
            Object param = JSON.parseObject(requestParams, paramTypes[0]);
            log.debug("解析单参数成功: type={}", paramTypes[0].getSimpleName());
            return new Object[]{param};
        }
        
        JSONObject json = JSON.parseObject(requestParams);
        Object[] args = new Object[paramTypes.length];
        
        for (int i = 0; i < paramTypes.length; i++) {
            Class<?> paramType = paramTypes[i];
            String paramName = getParamName(method, i);
            
            if (paramName != null && json.containsKey(paramName)) {
                args[i] = parseParamValue(json.get(paramName), paramType);
            } else {
                args[i] = parseParamValue(json.values().toArray()[i], paramType);
            }
        }
        
        log.debug("解析{}个参数成功", paramTypes.length);
        return args;
    }

    /**
     * 解析Java对象数组格式参数，如: [AuthLoginDTO(username=admin, password=admin123), RequestWrapper@xxx]
     */
    private Object[] parseJavaArrayParams(String requestParams, Class<?>[] paramTypes, Method method) {
        // 移除方括号
        String content = requestParams.trim().substring(1, requestParams.length() - 1);
        
        // 按 "), " 分割对象（处理嵌套情况）
        List<String> objectStrings = splitByTopLevelDelimiter(content, "), ");
        
        Object[] args = new Object[Math.min(objectStrings.size(), paramTypes.length)];
        
        for (int i = 0; i < args.length; i++) {
            String objStr = objectStrings.get(i);
            if (!objStr.endsWith(")")) {
                objStr = objStr + ")";
            }
            args[i] = parseJavaObjectString(objStr, paramTypes[i]);
        }
        
        log.debug("解析Java数组格式参数成功，数量={}", args.length);
        return args;
    }

    /**
     * 解析Java对象格式参数，如: AuthLoginDTO(username=admin, password=admin123)
     */
    private Object[] parseJavaObjectParams(String requestParams, Class<?>[] paramTypes, Method method) {
        if (paramTypes.length == 1) {
            Object param = parseJavaObjectString(requestParams, paramTypes[0]);
            return new Object[]{param};
        }
        
        // 如果有多参数但只有一个对象字符串，尝试从中提取字段作为多个参数
        return extractMultipleParamsFromObject(requestParams, paramTypes, method);
    }

    /**
     * 解析单个Java对象字符串，如: AuthLoginDTO(username=admin, password=admin123)
     */
    private Object parseJavaObjectString(String objStr, Class<?> targetType) {
        try {
            // 提取类名和字段部分
            int firstParen = objStr.indexOf('(');
            int lastParen = objStr.lastIndexOf(')');
            
            if (firstParen > 0 && lastParen > firstParen) {
                // String className = objStr.substring(0, firstParen).trim();
                String fieldsStr = objStr.substring(firstParen + 1, lastParen);
                
                // 解析字段键值对
                List<String> fieldPairs = splitByTopLevelDelimiter(fieldsStr, ", ");
                JSONObject json = new JSONObject();
                
                for (String pair : fieldPairs) {
                    int eqIndex = pair.indexOf('=');
                    if (eqIndex > 0) {
                        String key = pair.substring(0, eqIndex).trim();
                        String value = pair.substring(eqIndex + 1).trim();
                        
                        // 尝试解析值类型
                        json.put(key, parseStringValue(value));
                    }
                }
                
                // 将JSON转换为目标类型
                return JSON.parseObject(json.toJSONString(), targetType);
            }
        } catch (Exception e) {
            log.debug("解析Java对象字符串失败: {}, error={}", objStr, e.getMessage());
        }
        
        return null;
    }

    /**
     * 从对象字符串中提取多个参数
     */
    private Object[] extractMultipleParamsFromObject(String objStr, Class<?>[] paramTypes, Method method) {
        Object[] args = new Object[paramTypes.length];
        
        try {
            int firstParen = objStr.indexOf('(');
            int lastParen = objStr.lastIndexOf(')');
            
            if (firstParen > 0 && lastParen > firstParen) {
                String fieldsStr = objStr.substring(firstParen + 1, lastParen);
                List<String> fieldPairs = splitByTopLevelDelimiter(fieldsStr, ", ");
                
                for (int i = 0; i < paramTypes.length; i++) {
                    if (i < fieldPairs.size()) {
                        int eqIndex = fieldPairs.get(i).indexOf('=');
                        if (eqIndex > 0) {
                            String value = fieldPairs.get(i).substring(eqIndex + 1).trim();
                            args[i] = parseParamValue(parseStringValue(value), paramTypes[i]);
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.debug("从对象字符串提取参数失败: {}", e.getMessage());
        }
        
        return args;
    }

    /**
     * 按顶层分隔符分割字符串（处理嵌套括号）
     */
    private List<String> splitByTopLevelDelimiter(String str, String delimiter) {
        List<String> result = new ArrayList<>();
        int depth = 0;
        int start = 0;
        
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            
            if (c == '(') {
                depth++;
            } else if (c == ')') {
                depth--;
            } else if (depth == 0 && str.startsWith(delimiter, i)) {
                result.add(str.substring(start, i).trim());
                start = i + delimiter.length();
                i += delimiter.length() - 1;
            }
        }
        
        if (start < str.length()) {
            result.add(str.substring(start).trim());
        }
        
        return result;
    }

    /**
     * 解析字符串值（处理数字、布尔值等）
     */
    private Object parseStringValue(String value) {
        if (value == null) {
            return null;
        }
        
        // 处理数字
        try {
            if (value.contains(".")) {
                return Double.parseDouble(value);
            }
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            // 不是数字，继续尝试
        }
        
        // 处理布尔值
        if ("true".equalsIgnoreCase(value)) {
            return true;
        }
        if ("false".equalsIgnoreCase(value)) {
            return false;
        }
        
        // 处理null
        if ("null".equalsIgnoreCase(value)) {
            return null;
        }
        
        // 处理字符串（移除引号）
        if ((value.startsWith("\"") && value.endsWith("\"")) || 
            (value.startsWith("'") && value.endsWith("'"))) {
            return value.substring(1, value.length() - 1);
        }
        
        return value;
    }

    /**
     * 获取方法参数名（通过反射获取）
     */
    private String getParamName(Method method, int index) {
        try {
            java.lang.reflect.Parameter[] params = method.getParameters();
            if (index < params.length) {
                return params[index].getName();
            }
        } catch (Exception e) {
            log.debug("获取参数名失败: {}", e.getMessage());
        }
        return null;
    }

    /**
     * 解析单个参数值
     */
    private Object parseParamValue(Object jsonValue, Class<?> targetType) {
        if (jsonValue == null) {
            return null;
        }
        
        try {
            // 基本类型转换
            if (targetType == String.class) {
                return jsonValue.toString();
            } else if (targetType == Integer.class || targetType == int.class) {
                return ((Number) jsonValue).intValue();
            } else if (targetType == Long.class || targetType == long.class) {
                return ((Number) jsonValue).longValue();
            } else if (targetType == Boolean.class || targetType == boolean.class) {
                return jsonValue instanceof Boolean ? jsonValue : Boolean.parseBoolean(jsonValue.toString());
            } else if (targetType == Double.class || targetType == double.class) {
                return ((Number) jsonValue).doubleValue();
            }
            
            // 复杂类型：尝试JSON转换
            return JSON.parseObject(JSON.toJSONString(jsonValue), targetType);
            
        } catch (Exception e) {
            log.debug("解析参数值失败: value={}, targetType={}, error={}", jsonValue, targetType.getSimpleName(), e.getMessage());
            return jsonValue;
        }
    }
}