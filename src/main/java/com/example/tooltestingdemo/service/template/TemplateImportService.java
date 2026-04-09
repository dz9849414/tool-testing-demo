package com.example.tooltestingdemo.service.template;

import com.example.tooltestingdemo.dto.TemplateImportDTO;
import com.example.tooltestingdemo.vo.TemplateImportResultVO;

/**
 * 模板导入服务接口
 */
public interface TemplateImportService {

    /**
     * 导入模板
     *
     * @param dto 导入请求
     * @return 导入结果
     */
    TemplateImportResultVO importTemplates(TemplateImportDTO dto);

    /**
     * 验证导入文件
     *
     * @param dto 导入请求
     * @return 验证结果
     */
    TemplateImportResultVO validateImport(TemplateImportDTO dto);

    /**
     * 导出模板为JSON格式
     *
     * @param templateIds 模板ID列表
     * @return JSON字符串
     */
    String exportToJson(Long[] templateIds);

    /**
     * 导出为Postman Collection格式
     *
     * @param templateIds 模板ID列表
     * @return JSON字符串
     */
    String exportToPostman(Long[] templateIds);
}
