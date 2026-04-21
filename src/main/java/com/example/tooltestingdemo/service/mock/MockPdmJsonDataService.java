package com.example.tooltestingdemo.service.mock;

import com.example.tooltestingdemo.vo.MockPdmJsonDataVO;

import java.util.List;

/**
 * PDM 模拟 JSON 数据服务。
 */
public interface MockPdmJsonDataService {

    /**
     * 插入一条 PDM 模拟数据。
     *
     * @param dataJson 自定义 JSON 数据；为空时自动生成样例数据
     * @return 插入后的数据
     */
    MockPdmJsonDataVO insert(Object dataJson);

    /**
     * 根据 ID 查询数据。
     */
    MockPdmJsonDataVO getById(Long id);

    /**
     * 查询最新数据列表。
     */
    List<MockPdmJsonDataVO> listLatest(int limit);
}
