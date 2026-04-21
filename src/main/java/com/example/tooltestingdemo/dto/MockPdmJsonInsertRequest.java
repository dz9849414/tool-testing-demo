package com.example.tooltestingdemo.dto;

import lombok.Data;

/**
 * PDM 模拟 JSON 数据插入请求。
 */
@Data
public class MockPdmJsonInsertRequest {

    /**
     * 自定义 JSON 数据；为空时自动生成样例数据。
     */
    private Object dataJson;
}
