package com.example.tooltestingdemo.constants;

/**
 * 模板管理模块常量定义
 * 
 * 文件位置：src/main/java/com/example/tooltestingdemo/constants/TemplateConstants.java
 */
public final class TemplateConstants {

    private TemplateConstants() {}

    // ==================== 协议类型 ====================
    public static final String PROTOCOL_HTTP = "HTTP";
    public static final String PROTOCOL_HTTPS = "HTTPS";
    public static final String PROTOCOL_WEBSOCKET = "WEBSOCKET";
    public static final String PROTOCOL_SOAP = "SOAP";
    public static final String PROTOCOL_REST = "REST";
    public static final String PROTOCOL_MQTT = "MQTT";
    public static final String PROTOCOL_TCP = "TCP";
    public static final String PROTOCOL_UDP = "UDP";
    
    // ==================== 数据库类型 ====================
    public static final String PROTOCOL_SQL = "SQL";
    public static final String PROTOCOL_MYSQL = "MYSQL";
    public static final String PROTOCOL_POSTGRESQL = "POSTGRESQL";
    public static final String PROTOCOL_ORACLE = "ORACLE";
    public static final String PROTOCOL_SQLSERVER = "SQLSERVER";
    
    // ==================== 脚本类型 ====================
    public static final String PROTOCOL_SCRIPT = "SCRIPT";
    public static final String PROTOCOL_JS = "JS";
    public static final String PROTOCOL_GROOVY = "GROOVY";

    // ==================== HTTP 方法 ====================
    public static final String METHOD_GET = "GET";
    public static final String METHOD_POST = "POST";
    public static final String METHOD_PUT = "PUT";
    public static final String METHOD_DELETE = "DELETE";
    public static final String METHOD_PATCH = "PATCH";
    public static final String METHOD_HEAD = "HEAD";
    public static final String METHOD_OPTIONS = "OPTIONS";

    // ==================== 认证类型 ====================
    public static final String AUTH_NONE = "NONE";
    public static final String AUTH_BASIC = "BASIC";
    public static final String AUTH_DIGEST = "DIGEST";
    public static final String AUTH_OAUTH1 = "OAUTH1";
    public static final String AUTH_OAUTH2 = "OAUTH2";
    public static final String AUTH_BEARER = "BEARER";
    public static final String AUTH_APIKEY = "APIKEY";
    public static final String AUTH_JWT = "JWT";

    // ==================== 请求体类型 ====================
    public static final String BODY_TYPE_NONE = "NONE";
    public static final String BODY_TYPE_FORM_DATA = "FORM_DATA";
    public static final String BODY_TYPE_URLENCODED = "X_WWW_FORM_URLENCODED";
    public static final String BODY_TYPE_RAW = "RAW";
    public static final String BODY_TYPE_BINARY = "BINARY";
    public static final String BODY_TYPE_GRAPHQL = "GRAPHQL";

    // ==================== RAW 类型 ====================
    public static final String RAW_TYPE_JSON = "JSON";
    public static final String RAW_TYPE_XML = "XML";
    public static final String RAW_TYPE_HTML = "HTML";
    public static final String RAW_TYPE_TEXT = "TEXT";
    public static final String RAW_TYPE_JAVASCRIPT = "JavaScript";

    // ==================== 参数类型 ====================
    public static final String PARAM_TYPE_QUERY = "QUERY";
    public static final String PARAM_TYPE_PATH = "PATH";
    public static final String PARAM_TYPE_BODY = "BODY";

    // ==================== 数据类型 ====================
    public static final String DATA_TYPE_STRING = "STRING";
    public static final String DATA_TYPE_INTEGER = "INTEGER";
    public static final String DATA_TYPE_LONG = "LONG";
    public static final String DATA_TYPE_FLOAT = "FLOAT";
    public static final String DATA_TYPE_DOUBLE = "DOUBLE";
    public static final String DATA_TYPE_BOOLEAN = "BOOLEAN";
    public static final String DATA_TYPE_DATE = "DATE";
    public static final String DATA_TYPE_DATETIME = "DATETIME";
    public static final String DATA_TYPE_ARRAY = "ARRAY";
    public static final String DATA_TYPE_OBJECT = "OBJECT";
    public static final String DATA_TYPE_FILE = "FILE";

    // ==================== 断言类型 ====================
    public static final String ASSERT_STATUS_CODE = "STATUS_CODE";
    public static final String ASSERT_STATUS_MESSAGE = "STATUS_MESSAGE";
    public static final String ASSERT_RESPONSE_HEADER = "RESPONSE_HEADER";
    public static final String ASSERT_RESPONSE_BODY = "RESPONSE_BODY";
    public static final String ASSERT_RESPONSE_TIME = "RESPONSE_TIME";
    public static final String ASSERT_RESPONSE_SIZE = "RESPONSE_SIZE";
    public static final String ASSERT_JSON_PATH = "JSON_PATH";
    public static final String ASSERT_XML_PATH = "XML_PATH";
    public static final String ASSERT_REGEX = "REGEX";
    public static final String ASSERT_CONTAINS = "CONTAINS";
    public static final String ASSERT_EQUALS = "EQUALS";
    public static final String ASSERT_NOT_EQUALS = "NOT_EQUALS";

    // ==================== 处理器类型 ====================
    public static final String PRE_PROCESSOR_SET_VAR = "SET_VARIABLE";
    public static final String PRE_PROCESSOR_TIMESTAMP = "TIMESTAMP";
    public static final String PRE_PROCESSOR_RANDOM_STR = "RANDOM_STRING";
    public static final String PRE_PROCESSOR_RANDOM_NUM = "RANDOM_NUMBER";
    public static final String PRE_PROCESSOR_UUID = "RANDOM_UUID";
    public static final String PRE_PROCESSOR_BASE64_ENC = "BASE64_ENCODE";
    public static final String PRE_PROCESSOR_BASE64_DEC = "BASE64_DECODE";
    public static final String PRE_PROCESSOR_URL_ENC = "URL_ENCODE";
    public static final String PRE_PROCESSOR_URL_DEC = "URL_DECODE";
    public static final String PRE_PROCESSOR_MD5 = "MD5";
    public static final String PRE_PROCESSOR_SHA1 = "SHA1";
    public static final String PRE_PROCESSOR_SHA256 = "SHA256";
    public static final String PRE_PROCESSOR_JS_SCRIPT = "JS_SCRIPT";

    public static final String POST_PROCESSOR_JSON_EXTRACT = "JSON_EXTRACT";
    public static final String POST_PROCESSOR_XML_EXTRACT = "XML_EXTRACT";
    public static final String POST_PROCESSOR_REGEX_EXTRACT = "REGEX_EXTRACT";
    public static final String POST_PROCESSOR_HEADER_EXTRACT = "HEADER_EXTRACT";
    public static final String POST_PROCESSOR_COOKIE_EXTRACT = "COOKIE_EXTRACT";

    // ==================== PDM 系统类型 ====================
    public static final String PDM_CAD = "CAD";
    public static final String PDM_ERP = "ERP";
    public static final String PDM_PLM = "PLM";
    public static final String PDM_CAM = "CAM";
    public static final String PDM_CAE = "CAE";

    // ==================== PDM 模块 ====================
    public static final String PDM_MODULE_MATERIAL = "物料管理";
    public static final String PDM_MODULE_BOM = "BOM管理";
    public static final String PDM_MODULE_CHANGE = "变更管理";
    public static final String PDM_MODULE_DRAWING = "图纸管理";

    // ==================== 可见性 ====================
    public static final int VISIBILITY_PRIVATE = 1;
    public static final int VISIBILITY_TEAM = 2;
    public static final int VISIBILITY_PUBLIC = 3;

    // ==================== 模板状态 ====================
    public static final int STATUS_DRAFT = 0;
    public static final int STATUS_PUBLISHED = 1;
    public static final int STATUS_ARCHIVED = 2;
    public static final int STATUS_DISABLED = 3;

    // ==================== 文件夹状态 ====================
    public static final int FOLDER_STATUS_DISABLED = 0;
    public static final int FOLDER_STATUS_ENABLED = 1;

    // ==================== 是否启用 ====================
    public static final int ENABLED_NO = 0;
    public static final int ENABLED_YES = 1;

    // ==================== 是否必填 ====================
    public static final int REQUIRED_NO = 0;
    public static final int REQUIRED_YES = 1;

    // ==================== 是否为变量 ====================
    public static final int VARIABLE_NO = 0;
    public static final int VARIABLE_YES = 1;

    // ==================== 是否为最新版本 ====================
    public static final int LATEST_NO = 0;
    public static final int LATEST_YES = 1;

    // ==================== 收藏类型 ====================
    public static final int FAVORITE_TYPE_FAVORITE = 1;
    public static final int FAVORITE_TYPE_FOLLOW = 2;

    // ==================== 导入导出状态 ====================
    public static final int IMPORT_EXPORT_PROCESSING = 0;
    public static final int IMPORT_EXPORT_SUCCESS = 1;
    public static final int IMPORT_EXPORT_FAILED = 2;

    // ==================== 操作类型 ====================
    public static final String OPERATION_CREATE = "CREATE";
    public static final String OPERATION_UPDATE = "UPDATE";
    public static final String OPERATION_DELETE = "DELETE";
    public static final String OPERATION_PUBLISH = "PUBLISH";
    public static final String OPERATION_ARCHIVE = "ARCHIVE";
    public static final String OPERATION_COPY = "COPY";

    // ==================== 共享类型 ====================
    public static final String SHARE_TYPE_USER = "USER";
    public static final String SHARE_TYPE_TEAM = "TEAM";
    public static final String SHARE_TYPE_ROLE = "ROLE";
    public static final String SHARE_TYPE_DEPARTMENT = "DEPARTMENT";

    // ==================== 共享权限 ====================
    public static final String PERMISSION_VIEW = "VIEW";
    public static final String PERMISSION_EDIT = "EDIT";
    public static final String PERMISSION_EXECUTE = "EXECUTE";
    public static final String PERMISSION_ADMIN = "ADMIN";

    // ==================== 环境代码 ====================
    public static final String ENV_DEV = "DEV";
    public static final String ENV_TEST = "TEST";
    public static final String ENV_PROD = "PROD";

    // ==================== 变量作用域 ====================
    public static final String SCOPE_TEMPLATE = "TEMPLATE";
    public static final String SCOPE_STEP = "STEP";
    public static final String SCOPE_GLOBAL = "GLOBAL";

    // ==================== 变量来源 ====================
    public static final String SOURCE_MANUAL = "MANUAL";
    public static final String SOURCE_EXTRACT = "EXTRACT";
    public static final String SOURCE_SCRIPT = "SCRIPT";
    public static final String SOURCE_ENVIRONMENT = "ENVIRONMENT";
    public static final String SOURCE_DATABASE = "DATABASE";

    // ==================== 版本类型 ====================
    public static final String VERSION_TYPE_AUTO = "AUTO";
    public static final String VERSION_TYPE_MAJOR = "MAJOR";
    public static final String VERSION_TYPE_MINOR = "MINOR";
    public static final String VERSION_TYPE_PATCH = "PATCH";

    // ==================== 文件格式 ====================
    public static final String FORMAT_JSON = "JSON";
    public static final String FORMAT_YAML = "YAML";
    public static final String FORMAT_POSTMAN = "POSTMAN";
    public static final String FORMAT_HTTP = "HTTP";
    public static final String FORMAT_OPENAPI = "OPENAPI";

    // ==================== 默认超时时间（毫秒） ====================
    public static final int DEFAULT_CONNECT_TIMEOUT = 30000;
    public static final int DEFAULT_READ_TIMEOUT = 30000;
    public static final int DEFAULT_RETRY_INTERVAL = 1000;
}
