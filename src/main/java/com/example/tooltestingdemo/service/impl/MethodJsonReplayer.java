package com.example.tooltestingdemo.service.impl;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.example.tooltestingdemo.dto.OperationLogImportDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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
        try {
            // 获取参数对象的ID字段
            Field idField = param.getClass().getDeclaredField("id");
            idField.setAccessible(true);
            Object idValue = idField.get(param);

            if (idValue == null) {
                return false;
            }

            // 查找getById方法
            Method getByIdMethod = null;
            for (Method method : bean.getClass().getMethods()) {
                if ("getById".equals(method.getName()) && method.getParameterTypes().length == 1) {
                    getByIdMethod = method;
                    break;
                }
            }

            if (getByIdMethod == null) {
                return false;
            }

            // 执行查询
            Object existingRecord = null;
            try {
                existingRecord = getByIdMethod.invoke(bean, idValue);
            } catch (Exception e) {
                // 尝试类型转换后调用
                Class<?> paramType = getByIdMethod.getParameterTypes()[0];
                Object convertedId = convertIdType(idValue, paramType);
                if (convertedId != null) {
                    existingRecord = getByIdMethod.invoke(bean, convertedId);
                }
            }

            if (existingRecord == null) {
                log.info("记录不存在，将执行原方法: id={}", idValue);
                return false;
            }

            // 记录存在，执行更新操作
            log.info("记录已存在，将执行更新操作: id={}", idValue);

            // 查找updateById方法
            Method updateByIdMethod = null;
            for (Method method : bean.getClass().getMethods()) {
                if ("updateById".equals(method.getName()) && method.getParameterTypes().length == 1) {
                    updateByIdMethod = method;
                    break;
                }
            }

            if (updateByIdMethod != null) {
                // 将param的字段值复制到existingRecord
                copyFields(param, existingRecord);
                updateByIdMethod.invoke(bean, existingRecord);
                log.info("更新记录成功: id={}", idValue);
            }

            return true;
        } catch (Exception e) {
            log.debug("检查并更新记录时发生异常: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 将源对象的字段值复制到目标对象
     */
    private void copyFields(Object source, Object target) throws Exception {
        Field[] sourceFields = source.getClass().getDeclaredFields();
        for (Field sourceField : sourceFields) {
            sourceField.setAccessible(true);
            Object value = sourceField.get(source);

            try {
                Field targetField = target.getClass().getDeclaredField(sourceField.getName());
                targetField.setAccessible(true);
                targetField.set(target, value);
            } catch (NoSuchFieldException e) {
                // 目标对象没有该字段，跳过
            }
        }
    }

    private Object getBeanFromApplicationContext(Class<?> clazz) {
        try {
            return applicationContext.getBean(clazz);
        } catch (Exception e) {
            log.warn("从ApplicationContext获取Bean失败，尝试创建实例: {}", clazz.getName());
            try {
                return clazz.getDeclaredConstructor().newInstance();
            } catch (Exception ex) {
                throw new RuntimeException("无法获取或创建Bean: " + clazz.getName(), ex);
            }
        }
    }

    private Object[] parseRequestParams(String requestParams, Method method) {
        if (requestParams == null || requestParams.isEmpty()) {
            log.info("请求参数为空，返回空参数数组");
            return new Object[0];
        }

        Class<?>[] paramTypes = method.getParameterTypes();
        if (paramTypes.length == 0) {
            log.info("方法不需要参数，返回空参数数组");
            return new Object[0];
        }

        log.info("开始解析请求参数: \n参数内容: {}\n目标参数类型: {}\n方法签名: {}",
                requestParams.length() > 200 ? requestParams.substring(0, 200) + "..." : requestParams,
                paramTypes[0].getSimpleName(),
                method.toString());

        // 如果方法需要多个参数，记录警告
        if (paramTypes.length > 1) {
            log.warn("方法需要{}个参数，但当前实现只支持解析1个参数", paramTypes.length);
        }

        if (requestParams.startsWith("[") && requestParams.endsWith("]")) {
            String content = requestParams.substring(1, requestParams.length() - 1).trim();

            if (!content.startsWith("{")) {
                log.info("检测到Java对象toString格式 (数组包裹)");
                try {
                    Object[] result = parseJavaObjectFormat(content, paramTypes[0]);
                    if (result.length > 0) {
                        log.info("成功解析Java对象格式，得到{}个对象", result.length);
                        return result;
                    } else {
                        log.warn("解析Java对象格式返回空结果");
                    }
                } catch (Exception e) {
                    log.error("解析Java对象格式失败: {}", e.getMessage(), e);
                }
            }

            try {
                JSONArray array = JSON.parseArray(requestParams);
                log.info("解析JSON数组成功，数组大小: {}", array.size());
                for (int i = 0; i < array.size(); i++) {
                    Object item = array.get(i);
                    if (item instanceof JSONObject) {
                        try {
                            Object dto = JSON.parseObject(item.toString(), paramTypes[0]);
                            log.info("成功解析第{}个JSON对象为DTO: {}", i + 1, paramTypes[0].getSimpleName());
                            return new Object[]{dto};
                        } catch (Exception e) {
                            log.debug("尝试解析第{}个对象为DTO失败: {}", i + 1, e.getMessage());
                        }
                    }
                }
            } catch (Exception e) {
                log.debug("解析JSON数组失败: {}", e.getMessage());
            }
        }

        // 尝试直接解析为DTO（去掉外层数组）
        String cleanParams = requestParams.trim();
        if (cleanParams.startsWith("[") && cleanParams.endsWith("]")) {
            cleanParams = cleanParams.substring(1, cleanParams.length() - 1).trim();
        }

        try {
            Object dto = JSON.parseObject(cleanParams, paramTypes[0]);
            log.info("成功直接解析为DTO: {}", paramTypes[0].getSimpleName());
            return new Object[]{dto};
        } catch (Exception e) {
            log.debug("直接解析为DTO失败: {}", e.getMessage());
        }

        try {
            if (cleanParams.contains("(") && cleanParams.contains(")")) {
                Object dto = parseSingleJavaObject(cleanParams, paramTypes[0]);
                if (dto != null) {
                    log.info("成功解析Java对象格式为DTO: {}", paramTypes[0].getSimpleName());
                    return new Object[]{dto};
                }
            }
        } catch (Exception e) {
            log.debug("解析单个Java对象失败: {}", e.getMessage());
        }

        log.warn("无法解析请求参数格式，返回空数组: {}", requestParams.length() > 100 ? requestParams.substring(0, 100) + "..." : requestParams);
        return new Object[0];
    }

    private Method findMethod(Object bean, String methodName, String requestParams) {
        Method[] methods = bean.getClass().getMethods();
        List<Method> candidates = new ArrayList<>();

        for (Method method : methods) {
            if (method.getName().equals(methodName)) {
                candidates.add(method);
            }
        }

        if (candidates.isEmpty()) {
            log.warn("找不到方法: {}", methodName);
            return null;
        }

        if (candidates.size() == 1) {
            log.info("找到唯一方法: {}", candidates.get(0));
            return candidates.get(0);
        }

        int paramCount = requestParams != null && !requestParams.isEmpty() ? 1 : 0;
        for (Method method : candidates) {
            if (method.getParameterTypes().length == paramCount) {
                log.info("根据参数数量选择方法: {}", method);
                return method;
            }
        }

        log.warn("找到多个同名方法，返回第一个: {}", candidates.get(0));
        return candidates.get(0);
    }

    private Object[] parseJavaObjectFormat(String content, Class<?> targetClass) throws Exception {
        List<Object> result = new ArrayList<>();

        if (content.contains("(") && content.contains(")")) {
            int firstParen = content.indexOf('(');
            int lastParen = findMatchingParen(content, firstParen);

            if (firstParen > 0 && lastParen > firstParen) {
                String objectStr = content.substring(0, lastParen + 1);
                Object obj = parseSingleJavaObject(objectStr, targetClass);
                if (obj != null) {
                    result.add(obj);
                }

                String remaining = content.substring(lastParen + 1).trim();
                if (remaining.startsWith(",") || remaining.contains("(")) {
                    remaining = remaining.replaceFirst("^[,\\s]+", "");
                    if (!remaining.isEmpty()) {
                        Object[] more = parseJavaObjectFormat(remaining, targetClass);
                        for (Object o : more) {
                            result.add(o);
                        }
                    }
                }
            }
        }

        return result.toArray();
    }

    private int findMatchingParen(String str, int start) {
        int depth = 1;
        for (int i = start + 1; i < str.length(); i++) {
            if (str.charAt(i) == '(') {
                depth++;
            } else if (str.charAt(i) == ')') {
                depth--;
                if (depth == 0) {
                    return i;
                }
            }
        }
        return str.length() - 1;
    }

    private Object parseSingleJavaObject(String objectStr, Class<?> targetClass) throws Exception {
        log.info("解析单个Java对象: {}", objectStr.length() > 100 ? objectStr.substring(0, 100) + "..." : objectStr);

        int firstParen = objectStr.indexOf('(');
        int lastParen = objectStr.lastIndexOf(')');

        if (firstParen == -1 || lastParen == -1 || firstParen >= lastParen) {
            log.warn("无法找到括号，返回null");
            return null;
        }

        String className = objectStr.substring(0, firstParen).trim();
        String fieldsStr = objectStr.substring(firstParen + 1, lastParen);

        log.info("类名: {}, 目标类型: {}", className, targetClass.getSimpleName());

        Object instance = targetClass.getDeclaredConstructor().newInstance();

        List<String> fieldPairs = parseFieldPairs(fieldsStr);
        log.info("解析到{}个字段", fieldPairs.size());

        for (String pair : fieldPairs) {
            int eqIndex = pair.indexOf('=');
            if (eqIndex == -1) {
                continue;
            }

            String fieldName = pair.substring(0, eqIndex).trim();
            String fieldValue = pair.substring(eqIndex + 1).trim();

            log.debug("设置字段: {} = {}", fieldName, fieldValue.length() > 50 ? fieldValue.substring(0, 50) + "..." : fieldValue);
            setFieldValue(instance, fieldName, fieldValue);
        }

        return instance;
    }

    private List<String> parseFieldPairs(String fieldsStr) {
        List<String> result = new ArrayList<>();
        int depth = 0;
        StringBuilder current = new StringBuilder();

        for (char c : fieldsStr.toCharArray()) {
            if (c == '(') {
                depth++;
                current.append(c);
            } else if (c == ')') {
                depth--;
                current.append(c);
            } else if (c == ',' && depth == 0) {
                result.add(current.toString().trim());
                current = new StringBuilder();
            } else {
                current.append(c);
            }
        }

        if (current.length() > 0) {
            result.add(current.toString().trim());
        }

        return result;
    }

    private void setFieldValue(Object instance, String fieldName, String fieldValue) {
        try {
            Field field = instance.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);

            Class<?> fieldType = field.getType();
            Object value = convertValue(fieldType, fieldValue);

            field.set(instance, value);
            log.debug("成功设置字段: {} = {}", fieldName, value);
        } catch (NoSuchFieldException e) {
            log.debug("字段不存在: {}", fieldName);
        } catch (Exception e) {
            log.debug("设置字段值失败: {}={}, error={}", fieldName, fieldValue, e.getMessage());
        }
    }

    private Object convertValue(Class<?> targetType, String value) {
        if (value == null || value.equals("null")) {
            return null;
        }

        value = value.trim();

        if (targetType == String.class) {
            return value;
        }

        if (targetType == Integer.class || targetType == int.class) {
            return Integer.parseInt(value);
        }

        if (targetType == Long.class || targetType == long.class) {
            return Long.parseLong(value);
        }

        if (targetType == Boolean.class || targetType == boolean.class) {
            return Boolean.parseBoolean(value);
        }

        if (targetType == Double.class || targetType == double.class) {
            return Double.parseDouble(value);
        }

        if (targetType == Float.class || targetType == float.class) {
            return Float.parseFloat(value);
        }

        if (targetType == LocalDateTime.class) {
            return parseLocalDateTime(value);
        }

        if (targetType == LocalDate.class) {
            return LocalDate.parse(value);
        }

        if (targetType == LocalTime.class) {
            return LocalTime.parse(value);
        }

        if (targetType.isEnum()) {
            try {
                return java.lang.Enum.valueOf((Class<? extends Enum>) targetType, value);
            } catch (Exception e) {
                log.debug("枚举转换失败: {}", e.getMessage());
                return value;
            }
        }

        return value;
    }

    private LocalDateTime parseLocalDateTime(String value) {
        DateTimeFormatter[] formatters = {
                DateTimeFormatter.ISO_LOCAL_DATE_TIME,
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"),
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS"),
                DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSSSSS"),
                DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")
        };

        for (DateTimeFormatter formatter : formatters) {
            try {
                return LocalDateTime.parse(value, formatter);
            } catch (Exception e) {
                // continue
            }
        }

        return null;
    }

    /**
     * 转换ID类型，用于匹配MyBatis-Plus的removeById方法参数类型
     */
    private Object convertIdType(Object idValue, Class<?> targetType) {
        try {
            if (targetType == Long.class || targetType == long.class) {
                if (idValue instanceof String) {
                    // 移除可能的下划线或其他字符，只保留数字
                    String numStr = ((String) idValue).replaceAll("[^0-9]", "");
                    if (!numStr.isEmpty()) {
                        return Long.parseLong(numStr);
                    }
                    return Long.parseLong((String) idValue);
                } else if (idValue instanceof Number) {
                    return ((Number) idValue).longValue();
                }
            } else if (targetType == String.class) {
                return String.valueOf(idValue);
            } else if (targetType == Integer.class || targetType == int.class) {
                if (idValue instanceof String) {
                    return Integer.parseInt((String) idValue);
                } else if (idValue instanceof Number) {
                    return ((Number) idValue).intValue();
                }
            }
        } catch (Exception e) {
            log.debug("ID类型转换失败: {} -> {}, error={}", idValue, targetType, e.getMessage());
        }
        return null;
    }
}