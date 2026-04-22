package com.example.tooltestingdemo.entity.mock;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * PDM 模拟 JSON 数据。
 */
@Data
@TableName("pdm_tool_mock_pdm_json_data")
public class MockPdmJsonData {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private String dataJson;
}