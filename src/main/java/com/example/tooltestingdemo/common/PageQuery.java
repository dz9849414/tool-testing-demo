package com.example.tooltestingdemo.common;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 通用分页查询基类
 */
@Data
public class PageQuery implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private static final long DEFAULT_CURRENT = 1L;
    private static final long DEFAULT_SIZE = 10L;

    /**
     * 当前页
     */
    @TableField(exist = false)
    private Long current = DEFAULT_CURRENT;

    /**
     * 每页条数
     */
    @TableField(exist = false)
    private Long size = DEFAULT_SIZE;

    public long getSafeCurrent() {
        return current == null || current <= 0 ? DEFAULT_CURRENT : current;
    }

    public long getSafeSize() {
        return size == null || size <= 0 ? DEFAULT_SIZE : size;
    }

    public <T> Page<T> toPage() {
        return new Page<>(getSafeCurrent(), getSafeSize());
    }
}