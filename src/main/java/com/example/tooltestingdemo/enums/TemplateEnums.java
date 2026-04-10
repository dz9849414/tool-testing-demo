package com.example.tooltestingdemo.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 模板模块枚举常量
 * 
 * <p>集中管理模板相关的所有类型枚举</p>
 */
public class TemplateEnums {

    /**
     * 操作类型
     */
    @Getter
    @AllArgsConstructor
    public enum OperationType {
        IMPORT("IMPORT", "导入"),
        EXPORT("EXPORT", "导出"),
        CREATE("CREATE", "创建"),
        UPDATE("UPDATE", "更新"),
        DELETE("DELETE", "删除"),
        PUBLISH("PUBLISH", "发布"),
        ARCHIVE("ARCHIVE", "归档"),
        COPY("COPY", "复制"),
        ROLLBACK("ROLLBACK", "回滚");

        private final String code;
        private final String desc;

        public static OperationType getByCode(String code) {
            for (OperationType type : values()) {
                if (type.code.equalsIgnoreCase(code)) {
                    return type;
                }
            }
            return null;
        }
    }

    /**
     * 协议类型
     */
    @Getter
    @AllArgsConstructor
    public enum ProtocolType {
        HTTP("HTTP", "HTTP协议"),
        HTTPS("HTTPS", "HTTPS安全协议"),
        WEBSOCKET("WEBSOCKET", "WebSocket协议"),
        SOAP("SOAP", "SOAP协议"),
        REST("REST", "RESTful API"),
        MQTT("MQTT", "MQTT物联网协议"),
        TCP("TCP", "TCP协议"),
        UDP("UDP", "UDP协议"),
        GRPC("GRPC", "gRPC协议"),
        DUBBO("DUBBO", "Dubbo协议");

        private final String code;
        private final String desc;

        public static ProtocolType getByCode(String code) {
            for (ProtocolType type : values()) {
                if (type.code.equalsIgnoreCase(code)) {
                    return type;
                }
            }
            return null;
        }
    }

    /**
     * HTTP请求方法
     */
    @Getter
    @AllArgsConstructor
    public enum HttpMethod {
        GET("GET", "GET请求"),
        POST("POST", "POST请求"),
        PUT("PUT", "PUT请求"),
        DELETE("DELETE", "DELETE请求"),
        PATCH("PATCH", "PATCH请求"),
        HEAD("HEAD", "HEAD请求"),
        OPTIONS("OPTIONS", "OPTIONS请求"),
        TRACE("TRACE", "TRACE请求"),
        CONNECT("CONNECT", "CONNECT请求");

        private final String code;
        private final String desc;

        public static HttpMethod getByCode(String code) {
            for (HttpMethod method : values()) {
                if (method.code.equalsIgnoreCase(code)) {
                    return method;
                }
            }
            return null;
        }
    }

    /**
     * 认证类型
     */
    @Getter
    @AllArgsConstructor
    public enum AuthType {
        NONE("NONE", "无认证"),
        BASIC("BASIC", "Basic认证"),
        DIGEST("DIGEST", "Digest认证"),
        BEARER("BEARER", "Bearer Token"),
        JWT("JWT", "JWT令牌"),
        OAUTH1("OAUTH1", "OAuth 1.0"),
        OAUTH2("OAUTH2", "OAuth 2.0"),
        APIKEY("APIKEY", "API Key"),
        HMAC("HMAC", "HMAC签名"),
        AK_SK("AK_SK", "AccessKey/SecretKey");

        private final String code;
        private final String desc;

        public static AuthType getByCode(String code) {
            for (AuthType type : values()) {
                if (type.code.equalsIgnoreCase(code)) {
                    return type;
                }
            }
            return null;
        }
    }

    /**
     * 请求体类型
     */
    @Getter
    @AllArgsConstructor
    public enum BodyType {
        NONE("NONE", "无请求体"),
        FORM_DATA("FORM_DATA", "multipart/form-data"),
        X_WWW_FORM_URLENCODED("X_WWW_FORM_URLENCODED", "application/x-www-form-urlencoded"),
        RAW("RAW", "Raw文本"),
        BINARY("BINARY", "二进制文件"),
        GRAPHQL("GRAPHQL", "GraphQL"),
        JSON("JSON", "application/json"),
        XML("XML", "application/xml"),
        HTML("HTML", "text/html"),
        TEXT("TEXT", "text/plain"),
        JAVASCRIPT("JAVASCRIPT", "application/javascript");

        private final String code;
        private final String desc;

        public static BodyType getByCode(String code) {
            for (BodyType type : values()) {
                if (type.code.equalsIgnoreCase(code)) {
                    return type;
                }
            }
            return null;
        }
    }

    /**
     * 处理器类型 - 前置处理器
     */
    @Getter
    @AllArgsConstructor
    public enum PreProcessorType {
        SET_VARIABLE("SET_VARIABLE", "设置变量"),
        REMOVE_VARIABLE("REMOVE_VARIABLE", "删除变量"),
        TIMESTAMP("TIMESTAMP", "时间戳"),
        RANDOM_STRING("RANDOM_STRING", "随机字符串"),
        RANDOM_NUMBER("RANDOM_NUMBER", "随机数字"),
        RANDOM_UUID("RANDOM_UUID", "UUID"),
        BASE64_ENCODE("BASE64_ENCODE", "Base64编码"),
        BASE64_DECODE("BASE64_DECODE", "Base64解码"),
        URL_ENCODE("URL_ENCODE", "URL编码"),
        URL_DECODE("URL_DECODE", "URL解码"),
        MD5("MD5", "MD5加密"),
        SHA1("SHA1", "SHA1加密"),
        SHA256("SHA256", "SHA256加密"),
        HMAC("HMAC", "HMAC签名"),
        JS_SCRIPT("JS_SCRIPT", "JavaScript脚本"),
        GROOVY_SCRIPT("GROOVY_SCRIPT", "Groovy脚本"),
        DATABASE_QUERY("DATABASE_QUERY", "数据库查询"),
        CACHE_GET("CACHE_GET", "缓存获取");

        private final String code;
        private final String desc;

        public static PreProcessorType getByCode(String code) {
            for (PreProcessorType type : values()) {
                if (type.code.equalsIgnoreCase(code)) {
                    return type;
                }
            }
            return null;
        }
    }

    /**
     * 处理器类型 - 后置处理器
     */
    @Getter
    @AllArgsConstructor
    public enum PostProcessorType {
        JSON_EXTRACT("JSON_EXTRACT", "JSON提取"),
        XML_EXTRACT("XML_EXTRACT", "XML提取"),
        REGEX_EXTRACT("REGEX_EXTRACT", "正则提取"),
        HEADER_EXTRACT("HEADER_EXTRACT", "Header提取"),
        COOKIE_EXTRACT("COOKIE_EXTRACT", "Cookie提取"),
        JS_SCRIPT("JS_SCRIPT", "JavaScript脚本"),
        GROOVY_SCRIPT("GROOVY_SCRIPT", "Groovy脚本"),
        DATABASE_INSERT("DATABASE_INSERT", "数据库插入"),
        CACHE_SET("CACHE_SET", "缓存设置"),
        RESPONSE_HANDLE("RESPONSE_HANDLE", "响应处理");

        private final String code;
        private final String desc;

        public static PostProcessorType getByCode(String code) {
            for (PostProcessorType type : values()) {
                if (type.code.equalsIgnoreCase(code)) {
                    return type;
                }
            }
            return null;
        }
    }

    /**
     * 断言类型
     */
    @Getter
    @AllArgsConstructor
    public enum AssertionType {
        STATUS_CODE("STATUS_CODE", "状态码"),
        STATUS_MESSAGE("STATUS_MESSAGE", "状态消息"),
        RESPONSE_HEADER("RESPONSE_HEADER", "响应头"),
        RESPONSE_BODY("RESPONSE_BODY", "响应体"),
        RESPONSE_TIME("RESPONSE_TIME", "响应时间"),
        RESPONSE_SIZE("RESPONSE_SIZE", "响应大小"),
        JSON_PATH("JSON_PATH", "JSON路径"),
        XML_PATH("XML_PATH", "XML路径"),
        REGEX("REGEX", "正则匹配"),
        CONTAINS("CONTAINS", "包含"),
        EQUALS("EQUALS", "等于"),
        NOT_EQUALS("NOT_EQUALS", "不等于"),
        GREATER_THAN("GREATER_THAN", "大于"),
        LESS_THAN("LESS_THAN", "小于"),
        IS_EMPTY("IS_EMPTY", "为空"),
        IS_NOT_EMPTY("IS_NOT_EMPTY", "不为空"),
        IS_NULL("IS_NULL", "为null"),
        IS_NOT_NULL("IS_NOT_NULL", "不为null");

        private final String code;
        private final String desc;

        public static AssertionType getByCode(String code) {
            for (AssertionType type : values()) {
                if (type.code.equalsIgnoreCase(code)) {
                    return type;
                }
            }
            return null;
        }
    }

    /**
     * 参数类型
     */
    @Getter
    @AllArgsConstructor
    public enum ParamType {
        QUERY("QUERY", "Query参数"),
        PATH("PATH", "Path参数"),
        HEADER("HEADER", "Header参数"),
        BODY("BODY", "Body参数"),
        COOKIE("COOKIE", "Cookie参数"),
        FORM("FORM", "表单参数"),
        FILE("FILE", "文件参数");

        private final String code;
        private final String desc;

        public static ParamType getByCode(String code) {
            for (ParamType type : values()) {
                if (type.code.equalsIgnoreCase(code)) {
                    return type;
                }
            }
            return null;
        }
    }

    /**
     * 变量类型
     */
    @Getter
    @AllArgsConstructor
    public enum VariableType {
        STRING("STRING", "字符串"),
        INTEGER("INTEGER", "整数"),
        LONG("LONG", "长整数"),
        FLOAT("FLOAT", "浮点数"),
        DOUBLE("DOUBLE", "双精度浮点"),
        BOOLEAN("BOOLEAN", "布尔值"),
        DATE("DATE", "日期"),
        DATETIME("DATETIME", "日期时间"),
        JSON("JSON", "JSON对象"),
        ARRAY("ARRAY", "数组"),
        FILE("FILE", "文件");

        private final String code;
        private final String desc;

        public static VariableType getByCode(String code) {
            for (VariableType type : values()) {
                if (type.code.equalsIgnoreCase(code)) {
                    return type;
                }
            }
            return null;
        }
    }

    /**
     * 模板状态
     */
    @Getter
    @AllArgsConstructor
    public enum TemplateStatus {
        DRAFT(0, "草稿"),
        PENDING_REVIEW(1, "待审核"),
        PUBLISHED(2, "已发布"),
        ARCHIVED(3, "已归档"),
        DISABLED(4, "已禁用"),
        REJECTED(5, "已驳回");

        private final int code;
        private final String desc;

        public static TemplateStatus getByCode(int code) {
            for (TemplateStatus status : values()) {
                if (status.code == code) {
                    return status;
                }
            }
            return null;
        }
        
        /**
         * 是否可编辑的状态
         */
        public boolean isEditable() {
            return this == DRAFT || this == REJECTED;
        }
        
        /**
         * 是否可提交审核的状态
         */
        public boolean isSubmittable() {
            return this == DRAFT || this == REJECTED;
        }
    }

    /**
     * 可见性
     */
    @Getter
    @AllArgsConstructor
    public enum Visibility {
        PRIVATE(1, "私有"),
        TEAM(2, "团队可见"),
        PUBLIC(3, "公开");

        private final int code;
        private final String desc;

        public static Visibility getByCode(int code) {
            for (Visibility visibility : values()) {
                if (visibility.code == code) {
                    return visibility;
                }
            }
            return null;
        }
    }

    /**
     * 导入导出状态
     */
    @Getter
    @AllArgsConstructor
    public enum ImportExportStatus {
        PROCESSING(0, "处理中"),
        SUCCESS(1, "成功"),
        PARTIAL_SUCCESS(2, "部分成功"),
        FAILED(3, "失败");

        private final int code;
        private final String desc;

        public static ImportExportStatus getByCode(int code) {
            for (ImportExportStatus status : values()) {
                if (status.code == code) {
                    return status;
                }
            }
            return null;
        }
    }

    /**
     * 导入策略
     */
    @Getter
    @AllArgsConstructor
    public enum ImportStrategy {
        SKIP("SKIP", "跳过重复"),
        OVERWRITE("OVERWRITE", "覆盖更新"),
        RENAME("RENAME", "重命名导入");

        private final String code;
        private final String desc;

        /** 用于注解默认值的常量 */
        public static final String SKIP_CODE = "SKIP";

        public static ImportStrategy getByCode(String code) {
            for (ImportStrategy strategy : values()) {
                if (strategy.code.equalsIgnoreCase(code)) {
                    return strategy;
                }
            }
            return null;
        }
    }

    /**
     * PDM系统类型
     */
    @Getter
    @AllArgsConstructor
    public enum PdmSystemType {
        CAD("CAD", "计算机辅助设计"),
        ERP("ERP", "企业资源计划"),
        PLM("PLM", "产品生命周期管理"),
        CAM("CAM", "计算机辅助制造"),
        CAE("CAE", "计算机辅助工程"),
        MES("MES", "制造执行系统"),
        SCM("SCM", "供应链管理"),
        CRM("CRM", "客户关系管理");

        private final String code;
        private final String desc;

        public static PdmSystemType getByCode(String code) {
            for (PdmSystemType type : values()) {
                if (type.code.equalsIgnoreCase(code)) {
                    return type;
                }
            }
            return null;
        }
    }

    /**
     * 比较运算符
     */
    @Getter
    @AllArgsConstructor
    public enum Operator {
        EQUALS("EQUALS", "等于", "=="),
        NOT_EQUALS("NOT_EQUALS", "不等于", "!="),
        CONTAINS("CONTAINS", "包含", "contains"),
        STARTS_WITH("STARTS_WITH", "开头是", "startsWith"),
        ENDS_WITH("ENDS_WITH", "结尾是", "endsWith"),
        MATCHES("MATCHES", "匹配正则", "matches"),
        GREATER_THAN("GREATER_THAN", "大于", ">"),
        LESS_THAN("LESS_THAN", "小于", "<"),
        GREATER_EQUALS("GREATER_EQUALS", "大于等于", ">="),
        LESS_EQUALS("LESS_EQUALS", "小于等于", "<=");

        private final String code;
        private final String desc;
        private final String symbol;

        public static Operator getByCode(String code) {
            for (Operator op : values()) {
                if (op.code.equalsIgnoreCase(code)) {
                    return op;
                }
            }
            return null;
        }
    }
}
