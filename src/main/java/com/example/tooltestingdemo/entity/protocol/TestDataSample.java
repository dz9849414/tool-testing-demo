package com.example.tooltestingdemo.entity.protocol;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.example.tooltestingdemo.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * 测试数据样本库实体类
 * 表名：test_data_sample
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("test_data_sample")
public class TestDataSample extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 样本名称
     */
    private String sampleName;

    /**
     * 样本类型：BOM-物料清单, DRAWING-图纸, DOCUMENT-文档, CUSTOM-自定义
     */
    private String sampleType;

    /**
     * 适用的协议分类
     */
    private String protocolCategory;

    /**
     * 数据格式
     */
    private String dataFormat;

    /**
     * 样本数据（支持大文本存储）
     */
    private String sampleData;

    /**
     * 数据大小（字节）
     */
    private Integer dataSize;

    /**
     * 校验和
     */
    private String checksum;

    /**
     * 样本描述
     */
    private String description;

    /**
     * 是否标准样本：0-用户自定义，1-系统标准
     */
    private Integer isStandard;

    // 枚举类定义
    public enum SampleType {
        BOM("物料清单"),
        DRAWING("图纸"),
        DOCUMENT("文档"),
        CUSTOM("自定义");

        private final String description;

        SampleType(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }
}