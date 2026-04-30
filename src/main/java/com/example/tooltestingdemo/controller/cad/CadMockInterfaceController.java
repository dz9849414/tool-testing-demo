package com.example.tooltestingdemo.controller.cad;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.tooltestingdemo.common.Result;
import com.example.tooltestingdemo.dto.cad.*;
import com.example.tooltestingdemo.entity.cad.CadDataConvertMapping;
import com.example.tooltestingdemo.entity.cad.CadFileConvertConfig;
import com.example.tooltestingdemo.entity.cad.CadMockInterface;
import com.example.tooltestingdemo.service.cad.CadMockInterfaceService;
import com.example.tooltestingdemo.vo.cad.CadFileConvertTaskVO;
import com.example.tooltestingdemo.vo.cad.CadConnectivityTestVO;
import com.example.tooltestingdemo.vo.cad.CadMockTestVO;
import com.example.tooltestingdemo.vo.cad.CadUnifiedExecuteVO;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/pdm/cad/mock")
@RequiredArgsConstructor
@Validated
@Tag(name = "CAD模拟接口与文件转换")
public class CadMockInterfaceController {

    private final CadMockInterfaceService cadMockInterfaceService;

    @GetMapping("/page")
    @PreAuthorize("hasRole('ADMIN') or @securityService.hasPermission('cad:mock:query')")
    public Result<IPage<CadMockInterface>> page(@ModelAttribute CadMockInterfaceQueryDTO dto) {
        return Result.success(cadMockInterfaceService.page(dto));
    }

    @PostMapping("/create")
    @PreAuthorize("hasRole('ADMIN') or @securityService.hasPermission('cad:mock:create')")
    public Result<CadMockInterface> create(@RequestBody @Valid CadMockInterfaceCreateDTO dto) {
        return Result.success("保存成功", cadMockInterfaceService.saveMockInterface(dto));
    }

    @PostMapping("/auth/save")
    @PreAuthorize("hasRole('ADMIN') or @securityService.hasPermission('cad:mock:auth:edit')")
    public Result<CadMockInterface> saveAuth(@RequestBody @Valid CadAuthConfigSaveDTO dto) {
        return Result.success("保存成功", cadMockInterfaceService.saveAuthConfig(dto));
    }

    @PostMapping("/data-convert/save-batch")
    @PreAuthorize("hasRole('ADMIN') or @securityService.hasPermission('cad:mock:data:edit')")
    public Result<List<CadDataConvertMapping>> saveDataConvert(@RequestBody @Valid CadDataConvertSaveBatchDTO dto) {
        return Result.success("保存成功", cadMockInterfaceService.saveDataMappings(dto));
    }

    @GetMapping("/data-convert/list/{mockInterfaceId}")
    @PreAuthorize("hasRole('ADMIN') or @securityService.hasPermission('cad:mock:data:query')")
    public Result<List<CadDataConvertMapping>> listDataConvert(@PathVariable Long mockInterfaceId) {
        return Result.success(cadMockInterfaceService.listDataMappings(mockInterfaceId));
    }

    @PostMapping("/file-convert/save")
    @PreAuthorize("hasRole('ADMIN') or @securityService.hasPermission('cad:mock:file:edit')")
    public Result<CadFileConvertConfig> saveFileConvert(@RequestBody @Valid CadFileConvertConfigSaveDTO dto) {
        return Result.success("保存成功", cadMockInterfaceService.saveFileConvertConfig(dto));
    }

    @GetMapping("/file-convert/list")
    @PreAuthorize("hasRole('ADMIN') or @securityService.hasPermission('cad:mock:file:query')")
    public Result<List<CadFileConvertConfig>> listFileConvert(@ModelAttribute CadFileConvertConfigQueryDTO dto) {
        return Result.success(cadMockInterfaceService.listFileConvertConfigs(dto));
    }

    @PostMapping("/file-convert/execute")
    @PreAuthorize("hasRole('ADMIN') or @securityService.hasPermission('cad:mock:file:execute')")
    public Result<CadFileConvertTaskVO> executeFileConvert(@RequestBody @Valid CadFileConvertExecuteDTO dto) {
        return Result.success("转换任务已创建", cadMockInterfaceService.executeFileConvert(dto));
    }

    @GetMapping("/file-convert/task/{taskNo}")
    @PreAuthorize("hasRole('ADMIN') or @securityService.hasPermission('cad:mock:file:query')")
    public Result<CadFileConvertTaskVO> getTask(@PathVariable String taskNo) {
        return Result.success(cadMockInterfaceService.getTask(taskNo));
    }

    @PostMapping("/execute")
    @PreAuthorize("hasRole('ADMIN') or @securityService.hasPermission('cad:mock:execute')")
    public Result<CadUnifiedExecuteVO> execute(@RequestBody @Valid CadMockUnifiedExecuteDTO dto) {
        return Result.success(cadMockInterfaceService.executeUnified(dto));
    }

    @PostMapping("/test")
    @PreAuthorize("hasRole('ADMIN') or @securityService.hasPermission('cad:mock:test')")
    public Result<CadMockTestVO> test(@RequestBody CadMockTestDTO dto) {
        return Result.success(cadMockInterfaceService.test(dto));
    }

    @PostMapping("/connectivity-test")
    @PreAuthorize("hasRole('ADMIN') or @securityService.hasPermission('cad:mock:connectivity:test')")
    public Result<CadConnectivityTestVO> connectivityTest(@RequestBody CadConnectivityTestDTO dto) {
        return Result.success(cadMockInterfaceService.connectivityTest(dto));
    }
}
