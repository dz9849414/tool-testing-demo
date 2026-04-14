package com.example.tooltestingdemo.controller.protocol;

import com.example.tooltestingdemo.common.Result;
import com.example.tooltestingdemo.entity.protocol.ProtocolType;
import com.example.tooltestingdemo.service.protocol.IProtocolTypeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * <p>
 * 协议类型主表 前端控制器
 * </p>
 *
 * @author wanggang
 * @since 2026-04-11
 */
@Slf4j
@RestController
@RequestMapping("api/protocol/protocolType")
@RequiredArgsConstructor
public class ProtocolTypeController {

    private final IProtocolTypeService protocolTypeService;


    /**
     * 新增协议类型
     *
     * 接口地址：POST /api/protocol/protocolType
     *
     */
    @PostMapping
    public Result<ProtocolType> createProtocolType(@RequestBody @Valid ProtocolType protocolType) {
        ProtocolType vo = protocolTypeService.createProtocolType(protocolType);
        return Result.success("创建成功", vo);
    }

    /**
     * 获取协议类型列表
     *
     * 接口地址：get /api/protocol/protocolType/list
     *
     */
    @GetMapping("/list")
    public Result<List<ProtocolType>> getProtocolTypeList(ProtocolType protocolType) {
        List<ProtocolType> protocolTypeList = protocolTypeService.getProtocolTypeList(protocolType);
        return Result.success(protocolTypeList);
    }

    /**
     * 编辑协议类型
     *
     * 接口地址：POST /api/protocol/protocolType/modify
     *
     */
    @PostMapping("/modify")
    public Result<ProtocolType> modifyProtocolType(@RequestBody ProtocolType protocolType) {
        ProtocolType vo = protocolTypeService.modifyProtocolType(protocolType);
        long relatedProjectCount = vo.getRelatedProjectCount() == null ? 0L : vo.getRelatedProjectCount();
        long relatedTemplateCount = vo.getRelatedTemplateCount() == null ? 0L : vo.getRelatedTemplateCount();
        String message = relatedProjectCount > 0 || relatedTemplateCount > 0
                ? String.format("编辑成功，关联影响范围：%d 个项目、%d 个模板。", relatedProjectCount, relatedTemplateCount)
                : "编辑成功";
        return Result.success(message, vo);
    }

}
