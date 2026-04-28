package com.example.tooltestingdemo.controller.protocol;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.tooltestingdemo.common.Result;
import com.example.tooltestingdemo.dto.ProtocolTestConnectDTO;
import com.example.tooltestingdemo.dto.ProtocolTestRecordQueryDTO;
import com.example.tooltestingdemo.dto.ProtocolTestTransferDTO;
import com.example.tooltestingdemo.entity.protocol.ProtocolTestRecord;
import com.example.tooltestingdemo.service.protocol.IProtocolTestRecordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

/**
 * <p>
 * 协议测试记录表 前端控制器
 * </p>
 *
 * @author wanggang
 * @since 2026-04-13
 */
@RestController
@RequestMapping("/api/protocol/protocolTestRecord")
@RequiredArgsConstructor
@Tag(name = "协议测试记录")
public class ProtocolTestRecordController {

    private final IProtocolTestRecordService protocolTestRecordService;

    /**
     * 协议连接测试并保存记录
     */
    @PostMapping("/testConnect")
    @PreAuthorize("hasRole('ADMIN') or @securityService.hasPermission('protocol:param:url')")
    @Operation(summary = "协议连接测试", description = "根据协议配置ID获取 primary URL 发起 HTTP GET 测试，并保存结果到测试记录表")
    public Result<ProtocolTestRecord> testConnect(@RequestBody @Valid ProtocolTestConnectDTO dto) {
        ProtocolTestRecord record = protocolTestRecordService.testConnect(dto.getConfigId());
        String msg = ProtocolTestRecord.ResultStatus.SUCCESS.name().equals(record.getResultStatus()) ? "连接测试成功" : "连接测试失败";
        return Result.success(msg, record);
    }

    /**
     * 协议数据传输测试并保存记录
     */
    @PostMapping("/testTransfer")
    @PreAuthorize("hasRole('ADMIN') or @securityService.hasPermission('protocol:param:url')")
    @Operation(summary = "协议数据传输测试", description = "根据协议配置ID获取 primary URL，按入参发起数据传输请求，并保存结果到测试记录表")
    public Result<ProtocolTestRecord> testTransfer(@RequestBody @Valid ProtocolTestTransferDTO dto) {
        ProtocolTestRecord record = protocolTestRecordService.testTransfer(dto);
        String msg = ProtocolTestRecord.ResultStatus.SUCCESS.name().equals(record.getResultStatus()) ? "数据传输测试成功" : "数据传输测试失败";
        return Result.success(msg, record);
    }

    /**
     * 协议测试记录分页列表
     */
    @GetMapping("/list")
    @PreAuthorize("hasRole('ADMIN') or @securityService.hasPermission('protocol:param:url')")
    @Operation(summary = "协议测试记录分页列表", description = "支持按协议ID、配置ID、测试类型、测试场景、结果状态、创建时间范围筛选")
    public Result<IPage<ProtocolTestRecord>> list(@ModelAttribute ProtocolTestRecordQueryDTO dto) {
        IPage<ProtocolTestRecord> page = protocolTestRecordService.getProtocolTestRecordList(dto);
        return Result.success(page);
    }

    /**
     * 导出协议测试记录
     */
    @GetMapping("/export")
    @PreAuthorize("hasRole('ADMIN') or @securityService.hasPermission('protocol:param:url')")
    @Operation(summary = "导出协议测试记录")
    public void export(@ModelAttribute ProtocolTestRecordQueryDTO dto, HttpServletResponse response) throws IOException {
        protocolTestRecordService.exportProtocolTestRecords(dto, response);
    }
}
