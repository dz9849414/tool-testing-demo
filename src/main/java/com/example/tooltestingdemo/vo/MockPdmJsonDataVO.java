package com.example.tooltestingdemo.vo;

import lombok.Data;

/**
 * PDM 模拟 JSON 数据返回对象。
 */
@Data
public class MockPdmJsonDataVO {

    private Long id;

    private Object dataJson;
    private String remark;
}
