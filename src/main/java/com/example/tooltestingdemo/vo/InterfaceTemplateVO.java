package com.example.tooltestingdemo.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 接口模板 VO（返回给前端）
 * 
 * 文件位置：src/main/java/com/example/tooltestingdemo/vo/InterfaceTemplateVO.java
 */
@Data
public class InterfaceTemplateVO {

    /**
     * 模板ID
     */
    private Long id;

    /**
     * 所属分类ID
     */
    private Long folderId;

    /**
     * 所属分类名称
     */
    private String folderName;

    /**
     * 模板名称
     */
    private String name;

    /**
     * 模板描述/备注
     */
    private String description;

    /**
     * 协议类型
     */
    private String protocolType;

    /**
     * 请求方法
     */
    private String method;

    /**
     * 基础URL
     */
    private String baseUrl;

    /**
     * 请求路径
     */
    private String path;

    /**
     * 完整URL
     */
    private String fullUrl;

    /**
     * 认证类型
     */
    private String authType;

    /**
     * 认证配置
     */
    private String authConfig;

    /**
     * Content-Type
     */
    private String contentType;

    /**
     * 字符编码
     */
    private String charset;

    /**
     * 请求体类型
     */
    private String bodyType;

    /**
     * 请求体内容
     */
    private String bodyContent;

    /**
     * RAW类型
     */
    private String bodyRawType;

    /**
     * 连接超时时间
     */
    private Integer connectTimeout;

    /**
     * 读取超时时间
     */
    private Integer readTimeout;

    /**
     * 重试次数
     */
    private Integer retryCount;

    /**
     * 重试间隔
     */
    private Integer retryInterval;

    /**
     * 版本号
     */
    private String version;

    /**
     * 版本说明
     */
    private String versionRemark;

    /**
     * 是否为最新版本
     */
    private Integer isLatest;

    /**
     * 创建人ID
     */
    private Long ownerId;

    /**
     * 创建人姓名
     */
    private String ownerName;

    /**
     * 所属团队ID
     */
    private Long teamId;

    /**
     * 可见性
     */
    private Integer visibility;

    /**
     * 标签
     */
    private String tags;

    /**
     * PDM系统类型
     */
    private String pdmSystemType;

    /**
     * PDM模块
     */
    private String pdmModule;

    /**
     * 业务场景描述
     */
    private String businessScene;

    /**
     * 状态
     */
    private Integer status;

    /**
     * 使用次数
     */
    private Integer useCount;

    /**
     * 最后使用时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastUseTime;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

    // ==================== 关联数据 ====================

    /**
     * 请求头列表
     */
    private List<TemplateHeaderVO> headers;

    /**
     * 请求参数列表
     */
    private List<TemplateParameterVO> parameters;

    /**
     * FormData参数列表
     */
    private List<TemplateFormDataVO> formDataList;

    /**
     * 断言规则列表
     */
    private List<TemplateAssertionVO> assertions;

    /**
     * 前置处理器列表
     */
    private List<TemplatePreProcessorVO> preProcessors;

    /**
     * 后置处理器列表
     */
    private List<TemplatePostProcessorVO> postProcessors;

    /**
     * 变量定义列表
     */
    private List<TemplateVariableVO> variables;

    /**
     * 环境配置列表
     */
    private List<TemplateEnvironmentVO> environments;

    /**
     * 历史版本列表
     */
    private List<TemplateHistoryVO> histories;
}
