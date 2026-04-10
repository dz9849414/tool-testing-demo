package com.example.tooltestingdemo.service.template.engine.executor;

import com.example.tooltestingdemo.service.template.engine.core.ExecutionResult;
import com.example.tooltestingdemo.service.template.engine.core.TemplateContext;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;

/**
 * SQL 执行器
 * 
 * 执行 SQL 语句的模板执行器，支持多种数据库类型
 * 
 * <p>支持的协议类型：</p>
 * <ul>
 *   <li>SQL - 通用 SQL</li>
 *   <li>MYSQL - MySQL 数据库</li>
 *   <li>POSTGRESQL - PostgreSQL 数据库</li>
 *   <li>ORACLE - Oracle 数据库</li>
 *   <li>SQLSERVER - SQL Server 数据库</li>
 * </ul>
 * 
 * @author PDM接口测试工具
 * @since 1.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SqlExecutor implements TemplateExecutor {

    private final ObjectMapper objectMapper;

    // 数据库驱动映射
    private static final Map<String, String> DRIVER_MAP = new HashMap<>();
    
    static {
        DRIVER_MAP.put("MYSQL", "com.mysql.cj.jdbc.Driver");
        DRIVER_MAP.put("POSTGRESQL", "org.postgresql.Driver");
        DRIVER_MAP.put("ORACLE", "oracle.jdbc.driver.OracleDriver");
        DRIVER_MAP.put("SQLSERVER", "com.microsoft.sqlserver.jdbc.SQLServerDriver");
        DRIVER_MAP.put("SQLITE", "org.sqlite.JDBC");
        DRIVER_MAP.put("H2", "org.h2.Driver");
    }

    @Override
    public String getType() {
        return "SQL";
    }
    
    /**
     * 支持多种数据库类型
     */
    @Override
    public boolean supports(String protocolType) {
        if (protocolType == null) {
            return false;
        }
        String upper = protocolType.toUpperCase();
        return upper.equals("SQL") || 
               upper.equals("MYSQL") || 
               upper.equals("POSTGRESQL") ||
               upper.equals("ORACLE") ||
               upper.equals("SQLSERVER");
    }

    @Override
    public ExecutionResult execute(TemplateContext context) {
        log.debug("执行 SQL: templateId={}", 
                context.getTemplate() != null ? context.getTemplate().getId() : null);
        
        LocalDateTime startTime = LocalDateTime.now();
        long startMs = System.currentTimeMillis();
        
        SqlConfig config = parseConfig(context);
        if (config == null) {
            return buildErrorResult(context, "SQL 配置解析失败", startMs);
        }
        
        Connection connection = null;
        Statement statement = null;
        ResultSet resultSet = null;
        
        try {
            // 1. 建立连接
            connection = getConnection(config);
            
            // 2. 替换变量
            String sql = replaceVariables(config.getSql(), context.getAllVariables());
            log.debug("执行 SQL: {}", sql);
            
            // 3. 创建语句
            statement = connection.createStatement();
            statement.setQueryTimeout(config.getTimeout() != null ? config.getTimeout() : 30);
            
            // 4. 执行 SQL
            boolean hasResultSet = statement.execute(sql);
            
            // 5. 处理结果
            Object resultData;
            int affectedRows = 0;
            
            if (hasResultSet) {
                // 查询操作，返回结果集
                resultSet = statement.getResultSet();
                resultData = convertResultSetToList(resultSet);
            } else {
                // 更新操作，返回影响行数
                affectedRows = statement.getUpdateCount();
                Map<String, Object> updateResult = new HashMap<>();
                updateResult.put("affectedRows", affectedRows);
                resultData = updateResult;
            }
            
            // 6. 构建结果
            long durationMs = System.currentTimeMillis() - startMs;
            
            ExecutionResult.ResponseInfo responseInfo = ExecutionResult.ResponseInfo.builder()
                    .statusCode(200)
                    .statusText("OK")
                    .body(resultData)
                    .responseTime(durationMs)
                    .build();
            
            ExecutionResult.RequestInfo requestInfo = ExecutionResult.RequestInfo.builder()
                    .url(config.getJdbcUrl())
                    .method("SQL")
                    .body(sql)
                    .build();
            
            return ExecutionResult.builder()
                    .success(true)
                    .statusCode("200")
                    .message("执行成功")
                    .templateId(context.getTemplate() != null ? context.getTemplate().getId() : null)
                    .templateName(context.getTemplate() != null ? context.getTemplate().getName() : null)
                    .startTime(startTime)
                    .request(requestInfo)
                    .response(responseInfo)
                    .variables(context.getAllVariables())
                    .build();
            
        } catch (Exception e) {
            log.error("SQL 执行失败", e);
            return buildErrorResult(context, e.getMessage(), startMs);
        } finally {
            // 关闭资源
            closeQuietly(resultSet);
            closeQuietly(statement);
            closeQuietly(connection);
        }
    }

    @Override
    public ValidationResult validate(TemplateContext context) {
        if (context.getTemplate() == null) {
            return ValidationResult.failure("模板信息为空");
        }
        
        SqlConfig config = parseConfig(context);
        if (config == null) {
            return ValidationResult.failure("SQL 配置解析失败");
        }
        
        if (!StringUtils.hasText(config.getJdbcUrl())) {
            return ValidationResult.failure("JDBC URL 不能为空");
        }
        
        if (!StringUtils.hasText(config.getSql())) {
            return ValidationResult.failure("SQL 语句不能为空");
        }
        
        // 验证数据库驱动
        String dbType = detectDbType(config.getJdbcUrl());
        String driverClass = DRIVER_MAP.get(dbType.toUpperCase());
        if (driverClass != null) {
            try {
                Class.forName(driverClass);
            } catch (ClassNotFoundException e) {
                return ValidationResult.failure("数据库驱动未找到: " + driverClass + 
                        "，请确保依赖已添加");
            }
        }
        
        return ValidationResult.success();
    }

    @Override
    public PreviewResult preview(TemplateContext context) {
        SqlConfig config = parseConfig(context);
        if (config == null) {
            return new PreviewResult(null, "SQL", null, "配置解析失败", null);
        }
        
        String sql = replaceVariables(config.getSql(), context.getAllVariables());
        
        Map<String, String> headers = new HashMap<>();
        headers.put("JDBC-URL", config.getJdbcUrl());
        headers.put("Database-Type", detectDbType(config.getJdbcUrl()));
        
        return new PreviewResult(
                config.getJdbcUrl(),
                "SQL",
                headers,
                sql,
                null
        );
    }

    // ==================== 私有方法 ====================

    /**
     * 解析 SQL 配置
     */
    private SqlConfig parseConfig(TemplateContext context) {
        try {
            String bodyContent = context.getTemplate().getBodyContent();
            if (!StringUtils.hasText(bodyContent)) {
                return null;
            }
            
            return objectMapper.readValue(bodyContent, SqlConfig.class);
        } catch (Exception e) {
            log.error("解析 SQL 配置失败", e);
            return null;
        }
    }

    /**
     * 获取数据库连接
     */
    private Connection getConnection(SqlConfig config) throws SQLException {
        Properties props = new Properties();
        if (StringUtils.hasText(config.getUsername())) {
            props.setProperty("user", config.getUsername());
        }
        if (StringUtils.hasText(config.getPassword())) {
            props.setProperty("password", config.getPassword());
        }
        
        // 添加额外的连接属性
        if (config.getProperties() != null) {
            config.getProperties().forEach(props::setProperty);
        }
        
        return DriverManager.getConnection(config.getJdbcUrl(), props);
    }

    /**
     * 检测数据库类型
     */
    private String detectDbType(String jdbcUrl) {
        if (!StringUtils.hasText(jdbcUrl)) {
            return "UNKNOWN";
        }
        
        String url = jdbcUrl.toLowerCase();
        if (url.contains("mysql")) {
            return "MYSQL";
        } else if (url.contains("postgresql") || url.contains("postgres")) {
            return "POSTGRESQL";
        } else if (url.contains("oracle")) {
            return "ORACLE";
        } else if (url.contains("sqlserver") || url.contains("microsoft")) {
            return "SQLSERVER";
        } else if (url.contains("sqlite")) {
            return "SQLITE";
        } else if (url.contains("h2")) {
            return "H2";
        }
        
        return "SQL";
    }

    /**
     * 将 ResultSet 转换为 List<Map>
     */
    private List<Map<String, Object>> convertResultSetToList(ResultSet rs) throws SQLException {
        List<Map<String, Object>> rows = new ArrayList<>();
        ResultSetMetaData metaData = rs.getMetaData();
        int columnCount = metaData.getColumnCount();
        
        while (rs.next()) {
            Map<String, Object> row = new LinkedHashMap<>();
            for (int i = 1; i <= columnCount; i++) {
                String columnName = metaData.getColumnLabel(i);
                Object value = rs.getObject(i);
                row.put(columnName, value);
            }
            rows.add(row);
        }
        
        return rows;
    }

    /**
     * 替换 SQL 中的变量
     */
    private String replaceVariables(String sql, Map<String, Object> variables) {
        if (!StringUtils.hasText(sql) || variables == null) {
            return sql;
        }
        
        String result = sql;
        for (Map.Entry<String, Object> entry : variables.entrySet()) {
            String placeholder = "${" + entry.getKey() + "}";
            String value = entry.getValue() != null ? entry.getValue().toString() : "";
            result = result.replace(placeholder, value);
        }
        
        return result;
    }

    /**
     * 构建错误结果
     */
    private ExecutionResult buildErrorResult(TemplateContext context, String message, long startMs) {
        return ExecutionResult.builder()
                .success(false)
                .statusCode("ERROR")
                .message(message)
                .templateId(context.getTemplate() != null ? context.getTemplate().getId() : null)
                .templateName(context.getTemplate() != null ? context.getTemplate().getName() : null)
                .startTime(LocalDateTime.now())
                .endTime(LocalDateTime.now())
                .durationMs(System.currentTimeMillis() - startMs)
                .build();
    }

    /**
     * 安静关闭资源
     */
    private void closeQuietly(AutoCloseable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (Exception e) {
                log.debug("关闭资源失败", e);
            }
        }
    }

    // ==================== 配置内部类 ====================

    /**
     * SQL 配置
     */
    private static class SqlConfig {
        private String jdbcUrl;
        private String username;
        private String password;
        private String sql;
        private Integer timeout;
        private Map<String, String> properties;

        public String getJdbcUrl() { return jdbcUrl; }
        public void setJdbcUrl(String jdbcUrl) { this.jdbcUrl = jdbcUrl; }
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
        public String getSql() { return sql; }
        public void setSql(String sql) { this.sql = sql; }
        public Integer getTimeout() { return timeout; }
        public void setTimeout(Integer timeout) { this.timeout = timeout; }
        public Map<String, String> getProperties() { return properties; }
        public void setProperties(Map<String, String> properties) { this.properties = properties; }
    }
}
