package com.example.tooltestingdemo.controller.protocol;

import com.example.tooltestingdemo.common.Result;
import com.example.tooltestingdemo.dto.ProtocolConfigCreateDTO;
import com.example.tooltestingdemo.entity.protocol.ProtocolConfig;
import com.example.tooltestingdemo.service.protocol.IProtocolConfigService;
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
public class ProtocolConfigController {
    private final IProtocolConfigService protocolConfigService;

    /**
     * 新增协议配置。
     */
    @PostMapping
    public Result<ProtocolConfig> createProtocolConfig(@Valid @RequestBody ProtocolConfigCreateDTO dto) {
        ProtocolConfig created = protocolConfigService.createProtocolConfig(dto);
        return Result.success("创建成功", created);
    }
}
