package com.example.tooltestingdemo.service.template.parser;

import com.example.tooltestingdemo.dto.InterfaceTemplateDTO;

import java.util.List;

/**
 * 模板解析器接口
 */
public interface TemplateParser {

    /**
     * 获取支持的格式
     *
     * @return 格式名称
     */
    String getFormat();

    /**
     * 解析模板内容
     *
     * @param content 文件内容
     * @return 模板DTO列表
     */
    List<InterfaceTemplateDTO> parse(String content);

    /**
     * 验证格式是否正确
     *
     * @param content 文件内容
     * @return 是否有效
     */
    boolean validate(String content);

    /**
     * 获取格式描述
     *
     * @return 描述信息
     */
    String getDescription();
}
