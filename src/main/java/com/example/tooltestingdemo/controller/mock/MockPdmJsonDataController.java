package com.example.tooltestingdemo.controller.mock;

import com.example.tooltestingdemo.common.Result;
import com.example.tooltestingdemo.dto.MockPdmJsonInsertRequest;
import com.example.tooltestingdemo.service.mock.MockPdmJsonDataService;
import com.example.tooltestingdemo.vo.MockPdmJsonDataVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * PDM 模拟 JSON 数据接口。
 */
@Slf4j
@RestController
@RequestMapping("/api/mock/pdm-json-data")
@RequiredArgsConstructor
public class MockPdmJsonDataController {

    private final MockPdmJsonDataService mockPdmJsonDataService;

    /**
     * 插入一条模拟数据。
     *
     * <p>如果不传 {@code dataJson}，则自动生成一条船舶 PDM 样例数据。</p>
     */
    @PostMapping
    public Result<MockPdmJsonDataVO> insert(@RequestBody(required = false) MockPdmJsonInsertRequest request) {
        Object dataJson = request == null ? null : request.getDataJson();
        return Result.success("插入成功", mockPdmJsonDataService.insert(dataJson));
    }

    /**
     * 根据 ID 查询一条模拟数据。
     */
    @GetMapping("/{id}")
    public Result<MockPdmJsonDataVO> getById(@PathVariable Long id) {
        MockPdmJsonDataVO data = mockPdmJsonDataService.getById(id);
        return data == null ? Result.error("数据不存在") : Result.success(data);
    }

    /**
     * 查询最新模拟数据列表。
     */
    @GetMapping("/latest")
    public Result<List<MockPdmJsonDataVO>> listLatest(@RequestParam(defaultValue = "10") Integer limit) {
        int queryLimit = limit == null ? 10 : Math.min(Math.max(limit, 1), 100);
        return Result.success(mockPdmJsonDataService.listLatest(queryLimit));
    }
}
