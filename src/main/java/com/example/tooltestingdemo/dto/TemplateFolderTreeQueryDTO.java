package com.example.tooltestingdemo.dto;

import com.example.tooltestingdemo.common.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 模板文件夹树分页查询参数。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class TemplateFolderTreeQueryDTO extends PageQuery {

    /**
     * 父文件夹ID；不传默认查询根目录。
     */
    private Long parentId;
}
