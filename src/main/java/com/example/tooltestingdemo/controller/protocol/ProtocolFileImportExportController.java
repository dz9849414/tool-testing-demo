package com.example.tooltestingdemo.controller.protocol;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.tooltestingdemo.common.Result;
import com.example.tooltestingdemo.dto.ProtocolFileImportExportQueryDTO;
import com.example.tooltestingdemo.service.protocol.IProtocolFileImportExportService;
import com.example.tooltestingdemo.vo.ProtocolFileImportExportVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 协议文件导入导出记录表 前端控制器
 */
@RestController
@RequestMapping("/api/protocol/protocolFileImportExport")
@RequiredArgsConstructor
@Tag(name = "协议文件导入导出记录")
@Validated
public class ProtocolFileImportExportController {

    private final IProtocolFileImportExportService protocolFileImportExportService;

    /**
     * 协议文件导入导出记录分页列表
     */
    @GetMapping("/list")
    @PreAuthorize("hasRole('ADMIN') or @securityService.hasPermission('protocol:param:url')")
    @Operation(summary = "协议文件导入导出记录分页列表")
    public Result<IPage<ProtocolFileImportExportVO>> list(
            @Valid @ModelAttribute ProtocolFileImportExportQueryDTO dto) {
        IPage<ProtocolFileImportExportVO> page = protocolFileImportExportService.getProtocolFileImportExportList(dto);
        return Result.success(page);
    }
}

