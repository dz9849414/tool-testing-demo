package com.example.tooltestingdemo.service.template;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.tooltestingdemo.dto.template.TemplateJobGenerateRequest;
import com.example.tooltestingdemo.entity.template.TemplateJobGenerationLog;
import com.example.tooltestingdemo.vo.TemplateJobGenerationLogVO;

import java.util.List;

/**
 * 模板任务批量生成 Service
 */
public interface TemplateJobGenerationService extends IService<TemplateJobGenerationLog> {

    TemplateJobGenerationLogVO generate(TemplateJobGenerateRequest request);

    IPage<TemplateJobGenerationLogVO> pageLogs(Page<TemplateJobGenerationLog> page, String keyword);


    int batchDeleteLogsAndJobs(List<Long> ids);
}
