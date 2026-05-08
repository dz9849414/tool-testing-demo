package com.example.tooltestingdemo.enums;

/**
 * HTTP错误码枚举，用于高频错误码预警判断
 */
public enum HttpErrorCode {
    // 客户端错误
    BAD_REQUEST(400, "请求参数错误", "客户端发送的请求格式不正确或参数缺失", 
        "检查请求参数是否完整，验证参数格式是否正确，查看API文档确认参数要求"),
    UNAUTHORIZED(401, "未授权", "请求需要身份验证，用户未登录或token无效",
        "检查用户是否已登录，验证token是否有效且未过期，重新获取认证凭证"),
    FORBIDDEN(403, "禁止访问", "用户没有权限访问该资源",
        "检查用户角色和权限配置，确认是否有访问该资源的权限，联系管理员授权"),
    NOT_FOUND(404, "资源不存在", "请求的资源未找到",
        "检查请求URL是否正确，确认资源是否已被删除或尚未创建"),
    REQUEST_TIMEOUT(408, "请求超时", "服务器等待请求超时",
        "优化请求数据量，检查网络连接稳定性，考虑分批次处理"),
    CONFLICT(409, "资源冲突", "请求与当前资源状态冲突",
        "检查资源当前状态，避免重复提交，使用乐观锁或分布式锁"),
    PAYLOAD_TOO_LARGE(413, "请求体过大", "上传的数据超过服务器限制",
        "压缩上传数据，分块上传大文件，联系管理员调整服务器限制"),
    UNSUPPORTED_MEDIA_TYPE(415, "不支持的媒体类型", "请求的内容类型不被支持",
        "检查Content-Type是否正确，确认服务器支持的内容类型"),
    
    // 服务端错误
    INTERNAL_SERVER_ERROR(500, "服务器内部错误", "服务器在处理请求时发生意外",
        "查看服务器日志定位异常原因，检查数据库连接和第三方服务状态"),
    NOT_IMPLEMENTED(501, "未实现", "服务器不支持当前请求的功能",
        "确认API版本兼容性，检查是否使用了未实现的接口"),
    BAD_GATEWAY(502, "网关错误", "作为网关或代理的服务器收到无效响应",
        "检查后端服务状态，确认负载均衡配置，查看网络连通性"),
    SERVICE_UNAVAILABLE(503, "服务不可用", "服务器暂时无法处理请求",
        "等待服务恢复，检查服务健康状态，考虑服务熔断和降级策略"),
    GATEWAY_TIMEOUT(504, "网关超时", "网关或代理服务器超时",
        "优化后端服务响应时间，增加超时配置，检查网络延迟"),
    
    // 网络层错误
    NETWORK_ERROR(0, "网络错误", "网络连接失败或请求无法到达服务器",
        "检查网络连接状态，确认目标服务器是否可达，排查防火墙设置"),
    CONNECTION_REFUSED(1, "连接被拒绝", "目标服务器拒绝连接",
        "确认目标服务器是否启动，检查端口是否开放，验证安全组规则"),
    DNS_RESOLVE_FAILED(2, "DNS解析失败", "无法解析域名",
        "检查DNS配置，尝试使用IP地址直接访问，刷新DNS缓存");

    private final int code;
    private final String shortMessage;
    private final String detailedMessage;
    private final String suggestion;

    HttpErrorCode(int code, String shortMessage, String detailedMessage, String suggestion) {
        this.code = code;
        this.shortMessage = shortMessage;
        this.detailedMessage = detailedMessage;
        this.suggestion = suggestion;
    }

    public int getCode() {
        return code;
    }

    public String getShortMessage() {
        return shortMessage;
    }

    public String getDetailedMessage() {
        return detailedMessage;
    }

    public String getSuggestion() {
        return suggestion;
    }

    /**
     * 根据错误码获取枚举值
     */
    public static HttpErrorCode fromCode(int code) {
        for (HttpErrorCode errorCode : values()) {
            if (errorCode.code == code) {
                return errorCode;
            }
        }
        return null;
    }

    /**
     * 判断是否是客户端错误(4xx)
     */
    public boolean isClientError() {
        return code >= 400 && code < 500;
    }

    /**
     * 判断是否是服务端错误(5xx)
     */
    public boolean isServerError() {
        return code >= 500 && code < 600;
    }
}