package com.example.tooltestingdemo.controller.protocol;

import com.example.tooltestingdemo.common.Result;
import com.example.tooltestingdemo.dto.ProtocolConfigCreateDTO;
import com.example.tooltestingdemo.entity.protocol.ProtocolConfig;
import com.example.tooltestingdemo.service.protocol.IProtocolConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * <p>
 * 协议参数配置表 前端控制器
 * </p>
 *
 * @author wanggang
 * @since 2026-04-13
 */
@RestController
@RequestMapping("/api/protocol/protocolConfig")
@RequiredArgsConstructor
@Tag(name = "协议配置管理")
public class ProtocolConfigController {
    private final IProtocolConfigService protocolConfigService;

    /**
     * 新增协议配置
     */
    @PostMapping
    @Operation(summary = "新增协议配置", description = "创建协议配置并写入URL配置/认证配置（JSON），可同时创建多个参数模板及模板分组参数")
    public Result<ProtocolConfig> create(@RequestBody @Valid ProtocolConfigCreateDTO dto) {
        ProtocolConfig saved = protocolConfigService.createProtocolConfig(dto);
        return Result.success("创建成功", saved);
    }
}
