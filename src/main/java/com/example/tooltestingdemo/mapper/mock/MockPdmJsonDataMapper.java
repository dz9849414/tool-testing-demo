package com.example.tooltestingdemo.mapper.mock;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.tooltestingdemo.entity.mock.MockPdmJsonData;
import org.apache.ibatis.annotations.Mapper;

/**
 * PDM 模拟 JSON 数据 Mapper。
 */
@Mapper
public interface MockPdmJsonDataMapper extends BaseMapper<MockPdmJsonData> {
}
