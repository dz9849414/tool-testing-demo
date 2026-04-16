package com.example.tooltestingdemo.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.tooltestingdemo.common.Result;
import com.example.tooltestingdemo.entity.SysDictionary;
import com.example.tooltestingdemo.service.SysDictionaryService;
import com.example.tooltestingdemo.dto.SysDictionaryDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.apache.commons.beanutils.BeanUtils;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 数据字典控制器
 */
@RestController
@RequestMapping("/api/dictionaries")
@RequiredArgsConstructor
@Tag(name = "数据字典管理", description = "数据字典管理接口")
public class SysDictionaryController {

    private final SysDictionaryService dictionaryService;

    /**
     * 新增数据字典
     */
    @PostMapping
    @Operation(summary = "新增数据字典", description = "支持字典类型、字典键、字典值、排序号、状态的新增")
    @PreAuthorize("hasRole('ADMIN') or @securityService.hasPermission('system:dictionary:api')")
    public Result<?> createDictionary(@RequestBody SysDictionaryDTO dictionaryDTO) {
        // 检查字典是否已存在
        boolean exists = !dictionaryService.checkCodeUnique(dictionaryDTO.getCode(), dictionaryDTO.getType(), null);
        if (exists) {
            return Result.error("字典已存在，请勿重复添加");
        }
        
        SysDictionary dictionary = new SysDictionary();
        try {
            BeanUtils.copyProperties(dictionary, dictionaryDTO);
        } catch (Exception e) {
            return Result.error("参数转换失败");
        }
        
        boolean saved = dictionaryService.saveDictionary(dictionary);
        if (saved) {
            return Result.success("新增数据字典成功");
        } else {
            return Result.error("新增数据字典失败");
        }
    }

    /**
     * 编辑数据字典
     */
    @PutMapping("/{id}")
    @Operation(summary = "编辑数据字典", description = "支持字典类型、字典键、字典值、排序号、状态的编辑")
    @PreAuthorize("hasRole('ADMIN') or @securityService.hasPermission('system:dictionary:api')")
    public Result<?> updateDictionary(
            @Parameter(description = "字典ID") @PathVariable String id,
            @RequestBody SysDictionaryDTO dictionaryDTO) {
        SysDictionary dictionary = new SysDictionary();
        dictionary.setId(id);
        
        try {
            BeanUtils.copyProperties(dictionary, dictionaryDTO);
        } catch (Exception e) {
            return Result.error("参数转换失败");
        }
        
        boolean updated = dictionaryService.updateDictionary(dictionary);
        if (updated) {
            return Result.success("编辑数据字典成功");
        } else {
            return Result.error("编辑数据字典失败");
        }
    }

    /**
     * 删除数据字典
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除数据字典", description = "根据ID删除数据字典")
    @PreAuthorize("hasRole('ADMIN') or @securityService.hasPermission('system:dictionary:api')")
    public Result<?> deleteDictionary(@Parameter(description = "字典ID") @PathVariable String id) {
        boolean deleted = dictionaryService.deleteDictionary(id);
        if (deleted) {
            return Result.success("删除数据字典成功");
        } else {
            return Result.error("删除数据字典失败");
        }
    }

    /**
     * 批量删除数据字典
     */
    @DeleteMapping
    @Operation(summary = "批量删除数据字典", description = "根据ID列表批量删除数据字典")
    @PreAuthorize("@securityService.hasPermission('system:dictionary:api')")
    public Result<?> deleteDictionaries(@RequestBody List<String> ids) {
        boolean deleted = dictionaryService.deleteDictionaries(ids);
        if (deleted) {
            return Result.success("批量删除数据字典成功");
        } else {
            return Result.error("批量删除数据字典失败");
        }
    }

    /**
     * 启用/禁用数据字典
     */
    @PutMapping("/{id}/status")
    @Operation(summary = "启用/禁用数据字典", description = "切换数据字典的启用/禁用状态")
    @PreAuthorize("@securityService.hasPermission('system:dictionary:api')")
    public Result<?> toggleStatus(@Parameter(description = "字典ID") @PathVariable String id) {
        boolean toggled = dictionaryService.toggleStatus(id);
        if (toggled) {
            return Result.success("切换数据字典状态成功");
        } else {
            return Result.error("切换数据字典状态失败");
        }
    }

    /**
     * 检查字典键唯一性
     */
    @GetMapping("/check-code")
    @Operation(summary = "检查字典键唯一性", description = "实时校验字典键唯一性，重复则给出提示")
    @PreAuthorize("@securityService.hasPermission('system:dictionary:api')")
    public Result<?> checkCodeUnique(
            @Parameter(description = "字典键") @RequestParam String code,
            @Parameter(description = "字典类型") @RequestParam String type,
            @Parameter(description = "字典ID，编辑时传入") @RequestParam(required = false) String id) {
        boolean unique = dictionaryService.checkCodeUnique(code, type, id);
        return Result.success("检查字典键唯一性成功", unique);
    }

    /**
     * 分页查询数据字典
     */
    @GetMapping
    @Operation(summary = "分页查询数据字典", description = "支持按类型筛选、分页、搜索")
    @PreAuthorize("@securityService.hasPermission('system:dictionary:api')")
    public Result<?> getDictionaries(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") Integer size,
            @Parameter(description = "字典类型") @RequestParam(required = false) String type,
            @Parameter(description = "关键词") @RequestParam(required = false) String keyword,
            @Parameter(description = "状态：0-禁用，1-启用") @RequestParam(required = false) Integer status) {
        Page<SysDictionary> pageParam = new Page<>(page, size);
        Page<SysDictionary> result = dictionaryService.getDictionariesByPage(pageParam, type, keyword, status);
        return Result.success("查询数据字典成功", result);
    }

    /**
     * 根据类型查询数据字典
     */
    @GetMapping("/type/{type}")
    @Operation(summary = "根据类型查询数据字典", description = "根据类型查询启用状态的数据字典")
    @PreAuthorize("hasRole('ADMIN') or @securityService.hasPermission('system:dictionary:api')")
    public Result<?> getDictionariesByType(@Parameter(description = "字典类型") @PathVariable String type) {
        List<SysDictionary> dictionaries = dictionaryService.getDictionariesByType(type);
        return Result.success("根据类型查询数据字典成功", dictionaries);
    }

    /**
     * 根据ID查询数据字典详情
     */
    @GetMapping("/{id}")
    @Operation(summary = "根据ID查询数据字典详情", description = "根据字典ID查询数据字典的详细信息")
    @PreAuthorize("hasRole('ADMIN') or @securityService.hasPermission('system:dictionary:api')")
    public Result<?> getDictionaryById(@Parameter(description = "字典ID") @PathVariable String id) {
        SysDictionary dictionary = dictionaryService.getById(id);
        if (dictionary == null) {
            return Result.error("数据字典不存在");
        }
        return Result.success("获取数据字典详情成功", dictionary);
    }
}