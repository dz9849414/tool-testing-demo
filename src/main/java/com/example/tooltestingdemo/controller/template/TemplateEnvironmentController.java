package com.example.tooltestingdemo.controller.template;

import com.example.tooltestingdemo.common.Result;
import com.example.tooltestingdemo.entity.template.TemplateEnvironment;
import com.example.tooltestingdemo.service.template.TemplateEnvironmentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

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
     */
    @GetMapping("/list/{templateId}")
    public Result<List<TemplateEnvironment>> getEnvironments(@PathVariable Long templateId) {
        List<TemplateEnvironment> environments = environmentService.getEnvironmentsByTemplateId(templateId);
        return Result.success(environments);
    }

    /**
     * 获取默认环境
     * 
     * 接口地址：GET /api/template/environment/default/{templateId}
     */
    @GetMapping("/default/{templateId}")
    public Result<TemplateEnvironment> getDefaultEnvironment(@PathVariable Long templateId) {
        TemplateEnvironment environment = environmentService.getDefaultEnvironment(templateId);
        if (environment != null) {
            return Result.success(environment);
        }
        return Result.error("未设置默认环境");
    }

    /**
     * 创建环境配置
     * 
     * 接口地址：POST /api/template/environment
     */
    @PostMapping
    public Result<TemplateEnvironment> createEnvironment(@RequestBody TemplateEnvironment environment) {
        TemplateEnvironment created = environmentService.createEnvironment(environment);
        return Result.success("创建成功", created);
    }

    /**
     * 更新环境配置
     * 
     * 接口地址：PUT /api/template/environment/{id}
     */
    @PutMapping("/{id}")
    public Result<Void> updateEnvironment(@PathVariable Long id, @RequestBody TemplateEnvironment environment) {
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
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteEnvironment(@PathVariable Long id) {
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
     */
    @PutMapping("/{id}/default")
    public Result<Void> setDefaultEnvironment(@PathVariable Long id, @RequestParam Long templateId) {
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
     */
    @PostMapping("/{id}/clone")
    public Result<TemplateEnvironment> cloneEnvironment(@PathVariable Long id, @RequestParam String newName) {
        TemplateEnvironment cloned = environmentService.cloneEnvironment(id, newName);
        return Result.success("克隆成功", cloned);
    }
}
