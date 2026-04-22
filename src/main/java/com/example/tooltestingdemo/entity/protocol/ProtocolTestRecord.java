package com.example.tooltestingdemo.entity.protocol;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.example.tooltestingdemo.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * 协议测试记录表实体类
 * 表名：protocol_test_record
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("pdm_tool_protocol_test_record")
public class ProtocolTestRecord extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 协议类型ID
     */
    private Long protocolId;

    /**
     * 协议配置ID（可为空，表示使用默认配置）
     */
    private Long configId;

    /**
     * 测试类型：CONNECT-连接测试, TRANSFER-数据传输, COMPREHENSIVE-综合测试
     */
    private String testType;

    /**
     * 测试场景：NETWORK-网络连通, AUTH-认证, PROTOCOL-协议
     */
    private String testScenario;

    /**
     * 测试数据（JSON格式）
     */
    private String testData;

    /**
     * 结果状态：SUCCESS-成功, FAILED-失败
     */
    private String resultStatus;

    /**
     * 响应码（如200, 401, 500等）
     */
    private String responseCode;

    /**
     * 响应时间（毫秒）
     */
    private Integer responseTime;

    /**
     * 错误信息
     */
    private String errorMessage;

    /**
     * 比对结果（JSON格式存储校验和、数据差异等）
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private String comparisonResult;

    /**
     * 测试专用参数配置
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private String testParams;

    /**
     * 是否手动测试：0-自动，1-手动
     */
    private Integer isManual;

    // 枚举类定义
    public enum TestType {
        CONNECT("连接测试"),
        TRANSFER("数据传输"),
        COMPREHENSIVE("综合测试");

        private final String description;

        TestType(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    public enum TestScenario {
        NETWORK("网络连通"),
        AUTH("认证"),
        PROTOCOL("协议");

        private final String description;

        TestScenario(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    public enum ResultStatus {
        SUCCESS("成功"),
        FAILED("失败");

        private final String description;

        ResultStatus(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }
}