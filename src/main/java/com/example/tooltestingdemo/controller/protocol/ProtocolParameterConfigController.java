package com.example.tooltestingdemo.controller.protocol;

import com.example.tooltestingdemo.common.Result;
import com.example.tooltestingdemo.dto.ProtocolParameterConfigCreateDTO;
import com.example.tooltestingdemo.entity.protocol.ProtocolConfig;
import com.example.tooltestingdemo.service.protocol.IProtocolParameterConfigService;
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
@RequestMapping("api/protocol/protocolConfig")
@RequiredArgsConstructor
public class ProtocolParameterConfigController {

    private final IProtocolParameterConfigService protocolParameterConfigService;

    /**
     * 新增协议参数配置
     */
    @PostMapping
    public Result<ProtocolConfig> createProtocolParameterConfig(@RequestBody @Valid ProtocolParameterConfigCreateDTO dto) {
        ProtocolConfig result = protocolParameterConfigService.createProtocolParameterConfig(dto);
        return Result.success("创建成功", result);
    }

}
