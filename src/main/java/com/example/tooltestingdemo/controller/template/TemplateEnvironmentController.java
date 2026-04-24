package com.example.tooltestingdemo.controller.template;

import com.example.tooltestingdemo.common.Result;
import com.example.tooltestingdemo.entity.template.TemplateEnvironment;
import com.example.tooltestingdemo.service.template.TemplateEnvironmentService;
import com.example.tooltestingdemo.vo.TemplateEnvironmentVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;

/**
 * 模板环境配置 Controller
 * 
 * 文件位置：src/main/java/com/example/tooltestingdemo/controller/template/TemplateEnvironmentController.java
 */
@Slf4j
@RestController
@RequestMapping("/api/template/environment")
@RequiredArgsConstructor
public class TemplateEnvironmentController {

    private final TemplateEnvironmentService environmentService;

    /**
     * 获取模板的所有环境配置
     * 
     * 接口地址：GET /api/template/environment/list/{templateId}
     * 
     * @param templateId 模板ID
     * @return 环境配置VO列表
     */
    @GetMapping("/list/{templateId}")
    @PreAuthorize("hasRole('ADMIN') or @securityService.hasPermission('test:template:param')")
    public Result<List<TemplateEnvironmentVO>> getEnvironments(@PathVariable Long templateId) {
        List<TemplateEnvironmentVO> environments = environmentService.getEnvironmentsByTemplateId(templateId);
        return Result.success(environments);
    }

    /**
     * 获取默认环境
     * 
     * 接口地址：GET /api/template/environment/default/{templateId}
     * 
     * @param templateId 模板ID
     * @return 默认环境配置VO
     */
    @GetMapping("/default/{templateId}")
    public Result<TemplateEnvironmentVO> getDefaultEnvironment(@PathVariable Long templateId) {
        TemplateEnvironmentVO vo = environmentService.getDefaultEnvironment(templateId);
        if (vo != null) {
            return Result.success(vo);
        }
        return Result.error("未设置默认环境");
    }

    /**
     * 创建环境配置
     * 
     * 接口地址：POST /api/template/environment
     * 
     * @param environment 环境配置
     * @return 创建后的环境配置VO
     */
    @PostMapping
    public Result<TemplateEnvironmentVO> createEnvironment(@RequestBody TemplateEnvironment environment) {
        TemplateEnvironmentVO vo = environmentService.createEnvironment(environment);
        return Result.success("创建成功", vo);
    }

    /**
     * 更新环境配置
     * 
     * 接口地址：PUT /api/template/environment/{id}
     * 
     * @param id 环境配置ID
     * @param environment 环境配置
     * @return 是否成功
     */
    @PutMapping("/{id}")
    public Result<String> updateEnvironment(@PathVariable Long id, @RequestBody TemplateEnvironment environment) {
        environment.setId(id);
        boolean success = environmentService.updateEnvironment(environment);
        if (success) {
            return Result.success("更新成功");
        }
        return Result.error("更新失败");
    }

    /**
     * 删除环境配置
     * 
     * 接口地址：DELETE /api/template/environment/{id}
     * 
     * @param id 环境配置ID
     * @return 是否成功
     */
    @DeleteMapping("/{id}")
    public Result<String> deleteEnvironment(@PathVariable Long id) {
        boolean success = environmentService.deleteEnvironment(id);
        if (success) {
            return Result.success("删除成功");
        }
        return Result.error("删除失败");
    }

    /**
     * 设置默认环境
     * 
     * 接口地址：PUT /api/template/environment/{id}/default
     * 
     * @param id 环境配置ID
     * @param templateId 模板ID
     * @return 是否成功
     */
    @PutMapping("/{id}/default")
    public Result<String> setDefaultEnvironment(@PathVariable Long id, @RequestParam Long templateId) {
        boolean success = environmentService.setDefaultEnvironment(templateId, id);
        if (success) {
            return Result.success("设置成功");
        }
        return Result.error("设置失败");
    }

    /**
     * 克隆环境配置
     * 
     * 接口地址：POST /api/template/environment/{id}/clone
     * 
     * @param id 环境配置ID
     * @param newName 新环境名称
     * @return 新环境配置VO
     */
    @PostMapping("/{id}/clone")
    public Result<TemplateEnvironmentVO> cloneEnvironment(@PathVariable Long id, @RequestParam String newName) {
        TemplateEnvironmentVO vo = environmentService.cloneEnvironment(id, newName);
        return Result.success("克隆成功", vo);
    }
}