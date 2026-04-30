package com.example.tooltestingdemo.controller.protocol;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.tooltestingdemo.common.Result;
import com.example.tooltestingdemo.dto.ProtocolConfigCreateDTO;
import com.example.tooltestingdemo.dto.ProtocolConfigModifyDTO;
import com.example.tooltestingdemo.dto.ProtocolConfigQueryDTO;
import com.example.tooltestingdemo.dto.ProtocolConfigStatusUpdateDTO;
import com.example.tooltestingdemo.entity.protocol.ProtocolConfig;
import com.example.tooltestingdemo.service.protocol.IProtocolConfigService;
import com.example.tooltestingdemo.vo.ProtocolConfigImportResultVO;
import com.example.tooltestingdemo.vo.ProtocolConfigVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.security.access.prepost.PreAuthorize;

import java.io.IOException;

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
@Validated
public class ProtocolConfigController {
    private final IProtocolConfigService protocolConfigService;

    /**
     * 新增协议配置
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or @securityService.hasPermission('protocol:param:url')")
    @Operation(summary = "新增协议配置", description = "创建协议配置：将 URL 列表、认证列表校验后序列化为 JSON 写入库表；不包含参数模板。")
    public Result<ProtocolConfig> create(@RequestBody @Valid ProtocolConfigCreateDTO dto) {
        ProtocolConfig saved = protocolConfigService.createProtocolConfig(dto);
        return Result.success("创建成功", saved);
    }

    /**
     * 协议配置分页列表
     */
    @GetMapping("/list")
    @PreAuthorize("hasRole('ADMIN') or @securityService.hasPermission('protocol:param:url')")
//    @ProtocolPermissionFilter(protocolIdField = "id", enabled = true)
    @Operation(summary = "协议配置分页列表", description = "支持按协议ID、配置名称、状态以及创建/修改时间范围筛选")
    public Result<IPage<ProtocolConfigVO>> list(@ModelAttribute ProtocolConfigQueryDTO dto) {
        IPage<ProtocolConfigVO> page = protocolConfigService.getProtocolConfigList(dto);
        return Result.success(page);
    }

    /**
     * 下载协议配置导入模板
     */
    @GetMapping("/import/template")
    @PreAuthorize("hasRole('ADMIN') or @securityService.hasPermission('protocol:param:importExport')")
    @Operation(summary = "下载协议配置导入模板")
    public void downloadImportTemplate(HttpServletResponse response) throws IOException {
        protocolConfigService.downloadImportTemplate(response);
    }

    /**
     * 导入协议配置
     */
    @PostMapping("/import")
    @PreAuthorize("hasRole('ADMIN') or @securityService.hasPermission('protocol:param:importExport')")
    @Operation(summary = "导入协议配置", description = "按导入模板批量导入，失败时可下载失败原因文件")
    public Result<ProtocolConfigImportResultVO> importProtocolConfigs(@RequestParam("file") MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            return Result.error("导入文件不能为空");
        }
        ProtocolConfigImportResultVO result = protocolConfigService.importProtocolConfigs(file);
        return Result.success(result.getMessage(), result);
    }

    /**
     * 下载导入失败原因文件
     */
    @GetMapping("/import/failures/{reportId}")
    @PreAuthorize("hasRole('ADMIN') or @securityService.hasPermission('protocol:param:importExport')")
    @Operation(summary = "下载导入失败原因文件")
    public void downloadImportFailureReport(@PathVariable @NotBlank(message = "报告ID不能为空") String reportId, HttpServletResponse response) throws IOException {
        protocolConfigService.downloadImportFailureReport(reportId, response);
    }

    /**
     * 导出协议配置
     */
    @GetMapping("/export")
    @PreAuthorize("hasRole('ADMIN') or @securityService.hasPermission('protocol:param:importExport')")
    @Operation(summary = "导出协议配置", description = "按查询条件导出全部协议配置，导出字段与导入字段一致")
    public void exportProtocolConfigs(@ModelAttribute ProtocolConfigQueryDTO dto, HttpServletResponse response) throws IOException {
        protocolConfigService.exportProtocolConfigs(dto, response);
    }

    /**
     * 查询协议配置详情
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @securityService.hasPermission('protocol:param:url')")
    @Operation(summary = "协议配置详情", description = "根据协议配置ID获取详情")
    public Result<ProtocolConfigVO> detail(@PathVariable Long id) {
        ProtocolConfigVO detail = protocolConfigService.getProtocolConfigDetail(id);
        return Result.success(detail);
    }

    /**
     * 编辑协议配置
     */
    @PostMapping("/modify")
    @PreAuthorize("hasRole('ADMIN') or @securityService.hasPermission('protocol:param:url')")
    @Operation(summary = "编辑协议配置", description = "按ID更新协议配置字段，支持更新URL配置与认证配置")
    public Result<ProtocolConfigVO> modify(@RequestBody @Valid ProtocolConfigModifyDTO dto) {
        ProtocolConfigVO updated = protocolConfigService.modifyProtocolConfig(dto);
        return Result.success("编辑成功", updated);
    }

    /**
     * 编辑协议配置状态
     */
    @PostMapping("/status")
    @PreAuthorize("hasRole('ADMIN') or @securityService.hasPermission('protocol:param:url')")
    @Operation(summary = "编辑协议配置状态", description = "按ID更新协议配置状态，0-禁用，1-启用")
    public Result<ProtocolConfigVO> updateProtocolConfigStatus(@RequestBody @Valid ProtocolConfigStatusUpdateDTO dto) {
        ProtocolConfigVO updated = protocolConfigService.updateProtocolConfigStatus(dto);
        return Result.success("状态更新成功", updated);
    }

    /**
     * 删除协议配置（逻辑删除）
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @securityService.hasPermission('protocol:param:url')")
    @Operation(summary = "删除协议配置", description = "逻辑删除协议配置，并记录删除人和删除时间")
    public Result<Void> delete(@PathVariable Long id) {
        protocolConfigService.deleteProtocolConfig(id);
        return Result.success("删除成功");
    }
}