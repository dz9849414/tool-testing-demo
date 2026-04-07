package com.example.tooltestingdemo.controller.template;

import com.example.tooltestingdemo.common.Result;
import com.example.tooltestingdemo.entity.template.TemplateHistory;
import com.example.tooltestingdemo.service.template.TemplateHistoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 模板历史版本 Controller
 * 
 * 文件位置：src/main/java/com/example/tooltestingdemo/controller/template/TemplateHistoryController.java
 */
@Slf4j
@RestController
@RequestMapping("/api/template/history")
@RequiredArgsConstructor
public class TemplateHistoryController {

    private final TemplateHistoryService historyService;

    /**
     * 获取模板的历史版本列表
     * 
     * 接口地址：GET /api/template/history/list/{templateId}
     */
    @GetMapping("/list/{templateId}")
    public Result<List<TemplateHistory>> getHistories(@PathVariable Long templateId) {
        List<TemplateHistory> histories = historyService.getHistoriesByTemplateId(templateId);
        return Result.success(histories);
    }

    /**
     * 获取历史版本详情
     * 
     * 接口地址：GET /api/template/history/{id}
     */
    @GetMapping("/{id}")
    public Result<TemplateHistory> getHistoryDetail(@PathVariable Long id) {
        TemplateHistory history = historyService.getHistoryDetail(id);
        if (history != null) {
            return Result.success(history);
        }
        return Result.error("历史版本不存在");
    }

    /**
     * 回滚到指定版本
     * 
     * 接口地址：POST /api/template/history/{id}/rollback
     */
    @PostMapping("/{id}/rollback")
    public Result<Void> rollbackToVersion(@PathVariable Long id) {
        boolean success = historyService.rollbackToVersion(id);
        if (success) {
            return Result.success("回滚成功");
        }
        return Result.error("回滚失败");
    }

    /**
     * 清理历史版本
     * 
     * 接口地址：DELETE /api/template/history/clean/{templateId}
     */
    @DeleteMapping("/clean/{templateId}")
    public Result<Integer> cleanOldHistories(@PathVariable Long templateId, 
                                              @RequestParam(defaultValue = "10") int keepCount) {
        int count = historyService.cleanOldHistories(templateId, keepCount);
        return Result.success("清理完成，删除 " + count + " 条记录", count);
    }
}
