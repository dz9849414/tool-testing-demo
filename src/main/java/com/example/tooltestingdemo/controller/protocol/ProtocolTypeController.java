package com.example.tooltestingdemo.controller.protocol;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.tooltestingdemo.common.Result;
import com.example.tooltestingdemo.dto.*;
import com.example.tooltestingdemo.entity.protocol.ProtocolType;
import com.example.tooltestingdemo.service.protocol.IProtocolTypeService;
import com.example.tooltestingdemo.vo.ProtocolTypeBatchStatusChangeVO;
import com.example.tooltestingdemo.vo.ProtocolTypeDeleteResultVO;
import com.example.tooltestingdemo.vo.ProtocolTypeImportResultVO;
import com.example.tooltestingdemo.vo.ProtocolTypeStatusChangeVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * <p>
 * 协议类型主表 前端控制器
 * </p>
 *
 * @author wanggang
 * @since 2026-04-11
 */
@RestController
@RequestMapping("/api/protocol/protocolType")
@RequiredArgsConstructor
@Tag(name = "协议类型管理")
public class ProtocolTypeController {

    private final IProtocolTypeService protocolTypeService;

    /**
     * 新增协议类型
     */
    @PostMapping
    public Result<ProtocolType> createProtocolType(@RequestBody @Valid ProtocolTypeCreateDTO dto) {
        ProtocolType vo = protocolTypeService.createProtocolType(dto);
        return Result.success("创建成功", vo);
    }

    /**
     * 查询协议类型分页列表
     */
    @GetMapping("/list")
    @Operation(summary = "协议类型分页列表", description = "支持按协议编码、名称、分类、系统类型、状态以及创建/修改时间范围筛选")
    public Result<IPage<ProtocolType>> getProtocolTypeList(@ModelAttribute ProtocolTypeQueryDTO dto) {
        IPage<ProtocolType> protocolTypePage = protocolTypeService.getProtocolTypeList(dto);
        return Result.success(protocolTypePage);
    }

    /**
     * 下载协议类型导入模板
     */
    @GetMapping("/import/template")
    public void downloadImportTemplate(HttpServletResponse response) throws IOException {
        protocolTypeService.downloadImportTemplate(response);
    }

    /**
     * 导入协议类型
     */
    @PostMapping("/import")
    public Result<ProtocolTypeImportResultVO> importProtocolTypes(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "strategy", required = false, defaultValue = "INCREMENTAL") String strategy) throws IOException {
        if (file == null || file.isEmpty()) {
            return Result.error("导入文件不能为空");
        }
        ProtocolTypeImportResultVO result = protocolTypeService.importProtocolTypes(file, strategy);
        return Result.success(result.getMessage(), result);
    }

    /**
     * 下载导入失败原因文件
     */
    @GetMapping("/import/failures/{reportId}")
    public void downloadImportFailureReport(@PathVariable String reportId, HttpServletResponse response) throws IOException {
        protocolTypeService.downloadImportFailureReport(reportId, response);
    }

    /**
     * 导出协议类型
     * <p>
     * 接口地址：GET /api/protocol/protocolType/export
     *
     */
    @GetMapping("/export")
    public void exportProtocolTypes(@ModelAttribute ProtocolTypeQueryDTO dto,
                                    HttpServletResponse response) throws IOException {
        protocolTypeService.exportProtocolTypes(dto, response);
    }

    /**
     * 批量变更协议类型状态
     */
    @PostMapping("/batch/status")
    public Result<ProtocolTypeBatchStatusChangeVO> batchUpdateProtocolTypeStatus(
            @RequestBody @Valid ProtocolTypeBatchStatusUpdateDTO dto) {
        if (dto.getIds() == null || dto.getIds().length == 0) {
            return Result.error("协议类型ID列表不能为空");
        }
        ProtocolTypeBatchStatusChangeVO result = protocolTypeService.batchUpdateProtocolTypeStatus(dto);
        return Result.success(result.getMessage(), result);
    }

    /**
     * 变更协议类型状态
     */
    @PostMapping("/status")
    public Result<ProtocolTypeStatusChangeVO> updateProtocolTypeStatus(@RequestBody @Valid ProtocolTypeStatusUpdateDTO dto) {
        ProtocolTypeStatusChangeVO result = protocolTypeService.updateProtocolTypeStatus(dto);
        return Result.success(result.getMessage(), result);
    }

    /**
     * 编辑协议类型
     */
    @PostMapping("/modify")
    public Result<ProtocolType> modifyProtocolType(@RequestBody ProtocolTypeModifyDTO dto) {
        ProtocolType vo = protocolTypeService.modifyProtocolType(dto);
        return Result.success("编辑成功", vo);
    }

    /**
     * 删除协议类型
     * <p>
     * 接口地址：DELETE /api/protocol/protocolType/{id}
     *
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteProtocolType(@PathVariable Long id) {
        protocolTypeService.deleteProtocolType(id);
        return Result.success("删除成功");
    }

    /**
     * 批量删除协议类型
     * <p>
     * 接口地址：DELETE /api/protocol/protocolType/batch
     *
     */
    @DeleteMapping("/batch")
    public Result<ProtocolTypeDeleteResultVO> batchDeleteProtocolTypes(@RequestBody ProtocolTypeBatchDeleteDTO dto) {
        if (dto == null || dto.getIds() == null || dto.getIds().length == 0) {
            return Result.error("协议类型ID列表不能为空");
        }
        ProtocolTypeDeleteResultVO result = protocolTypeService.batchDeleteProtocolTypes(dto.getIds());
        return Result.success(result.getSummaryMessage(), result);
    }
}
