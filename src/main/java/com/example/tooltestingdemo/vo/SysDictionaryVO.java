package com.example.tooltestingdemo.vo;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 数据字典VO
 */
@Data
public class SysDictionaryVO {
    private String id;
    private String code;
    private String name;
    private String value;
    private String type;
    private String description;
    private Integer sort;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}