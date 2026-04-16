package com.example.tooltestingdemo.controller.protocol;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.tooltestingdemo.common.Result;
import com.example.tooltestingdemo.entity.protocol.ProtocolType;
import com.example.tooltestingdemo.service.protocol.IProtocolTypeService;
import com.example.tooltestingdemo.vo.ProtocolTypeDeleteResultVO;
import com.example.tooltestingdemo.vo.ProtocolTypeImportResultVO;
import com.example.tooltestingdemo.vo.ProtocolTypeStatusChangeVO;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
@RestController
@RequestMapping("api/protocol/protocolType")
@RequiredArgsConstructor
public class ProtocolTypeController {

    private final IProtocolTypeService protocolTypeService;


    /**
     * 新增协议类型
     * <p>
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
     * <p>
     * 接口地址：get /api/protocol/protocolType/list
     *
     */
    @GetMapping("/list")
    public Result<IPage<ProtocolType>> getProtocolTypeList(ProtocolType protocolType) {
        IPage<ProtocolType> protocolTypePage = protocolTypeService.getProtocolTypeList(protocolType);
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
    public void exportProtocolTypes(ProtocolType protocolType, HttpServletResponse response) throws IOException {
        protocolTypeService.exportProtocolTypes(protocolType, response);
    }

    /**
     * 启用/禁用协议类型
     */
    @PutMapping("/{id}/status")
    public Result<ProtocolTypeStatusChangeVO> updateProtocolTypeStatus(@PathVariable Long id,
                                                                       @RequestParam Integer status,
                                                                       @RequestParam(value = "confirm", required = false, defaultValue = "false") Boolean confirm) {
        ProtocolTypeStatusChangeVO result = protocolTypeService.updateProtocolTypeStatus(id, status, confirm);
        return Result.success(result.getMessage(), result);
    }

    /**
     * 编辑协议类型
     * <p>
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
    public Result<ProtocolTypeDeleteResultVO> batchDeleteProtocolTypes(@RequestBody Long[] ids) {
        if (ids == null || ids.length == 0) {
            return Result.error("协议类型ID列表不能为空");
        }
        ProtocolTypeDeleteResultVO result = protocolTypeService.batchDeleteProtocolTypes(ids);
        return Result.success(result.getSummaryMessage(), result);
    }

}
