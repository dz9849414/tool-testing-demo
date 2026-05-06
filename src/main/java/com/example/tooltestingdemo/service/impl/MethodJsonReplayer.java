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
import java.util.List;

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

        try {
            JSONArray methodArray = JSON.parseArray(methodJson);

            for (int i = 0; i < methodArray.size(); i++) {
                JSONObject methodInfo = methodArray.getJSONObject(i);
                String className = methodInfo.getString("className");
                String methodName = methodInfo.getString("methodName");

                if (className == null || methodName == null) {
                    log.warn("method_json格式错误，跳过: className={}, methodName={}", className, methodName);
                    continue;
                }

                executeMethod(className, methodName, logDTO);
            }

            log.info("基于method_json还原成功: 执行了{}个方法调用", methodArray.size());
        } catch (Exception e) {
            log.error("基于method_json还原失败: {}", e.getMessage(), e);
            throw new RuntimeException("方法调用链还原失败: " + e.getMessage(), e);
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
            return new Object[0];
        }

        Class<?>[] paramTypes = method.getParameterTypes();
        if (paramTypes.length == 0) {
            return new Object[0];
        }

        log.info("开始解析请求参数: {}, 目标参数类型: {}",
                requestParams.length() > 100 ? requestParams.substring(0, 100) + "..." : requestParams,
                paramTypes[0].getSimpleName());

        if (requestParams.startsWith("[") && requestParams.endsWith("]")) {
            String content = requestParams.substring(1, requestParams.length() - 1).trim();

            if (!content.startsWith("{")) {
                log.info("检测到Java对象toString格式");
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
                for (int i = 0; i < array.size(); i++) {
                    Object item = array.get(i);
                    if (item instanceof JSONObject) {
                        try {
                            Object dto = JSON.parseObject(item.toString(), paramTypes[0]);
                            return new Object[]{dto};
                        } catch (Exception e) {
                            log.debug("尝试解析为DTO失败，继续尝试: {}", e.getMessage());
                        }
                    }
                }
            } catch (Exception e) {
                log.debug("解析JSON数组失败: {}", e.getMessage());
            }
        }

        try {
            Object dto = JSON.parseObject(requestParams, paramTypes[0]);
            return new Object[]{dto};
        } catch (Exception e) {
            log.debug("解析为DTO失败: {}", e.getMessage());
        }

        try {
            if (requestParams.contains("(") && requestParams.contains(")")) {
                Object dto = parseSingleJavaObject(requestParams, paramTypes[0]);
                if (dto != null) {
                    return new Object[]{dto};
                }
            }
        } catch (Exception e) {
            log.debug("解析单个Java对象失败: {}", e.getMessage());
        }

        log.warn("无法解析请求参数格式: {}", requestParams.length() > 100 ? requestParams.substring(0, 100) + "..." : requestParams);
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