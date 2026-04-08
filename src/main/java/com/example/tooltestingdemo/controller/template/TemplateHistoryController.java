package com.example.tooltestingdemo.controller.template;

import com.example.tooltestingdemo.common.Result;
import com.example.tooltestingdemo.service.template.TemplateHistoryService;
import com.example.tooltestingdemo.vo.TemplateHistoryVO;
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
     * 
     * @param templateId 模板ID
     * @return 历史版本VO列表
     */
    @GetMapping("/list/{templateId}")
    public Result<List<TemplateHistoryVO>> getHistories(@PathVariable Long templateId) {
        List<TemplateHistoryVO> histories = historyService.getHistoriesByTemplateId(templateId);
        return Result.success(histories);
    }

    /**
     * 获取历史版本详情
     * 
     * 接口地址：GET /api/template/history/{id}
     * 
     * @param id 历史版本ID
     * @return 历史版本详情VO
     */
    @GetMapping("/{id}")
    public Result<TemplateHistoryVO> getHistoryDetail(@PathVariable Long id) {
        TemplateHistoryVO vo = historyService.getHistoryDetail(id);
        if (vo != null) {
            return Result.success(vo);
        }
        return Result.error("历史版本不存在");
    }

    /**
     * 回滚到指定版本
     * 
     * 接口地址：POST /api/template/history/{id}/rollback
     * 
     * @param id 历史版本ID
     * @return 是否成功
     */
    @PostMapping("/{id}/rollback")
    public Result<String> rollbackToVersion(@PathVariable Long id) {
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
     * 
     * @param templateId 模板ID
     * @param keepCount 保留数量
     * @return 清理数量
     */
    @DeleteMapping("/clean/{templateId}")
    public Result<Integer> cleanOldHistories(@PathVariable Long templateId, 
                                              @RequestParam(defaultValue = "10") int keepCount) {
        int count = historyService.cleanOldHistories(templateId, keepCount);
        return Result.success("清理完成，删除 " + count + " 条记录", count);
    }
}
