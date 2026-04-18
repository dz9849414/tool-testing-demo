package com.example.tooltestingdemo.dto.common;

import lombok.Data;
import java.util.ArrayList;
import java.util.List;

/**
 * 分页结果DTO
 */
@Data
public class PageResult<T> {
    
    /** 当前页码 */
    private Integer page;
    
    /** 每页大小 */
    private Integer size;
    
    /** 总记录数 */
    private Long total;
    
    /** 总页数 */
    private Integer totalPages;
    
    /** 数据列表 */
    private List<T> data;
    
    /** 是否有下一页 */
    private Boolean hasNext;
    
    /** 是否有上一页 */
    private Boolean hasPrev;
    
    /** 显示的页码列表 */
    private List<Integer> pageNumbers;
    
    public PageResult() {}
    
    public PageResult(Integer page, Integer size, Long total, List<T> data) {
        this.page = page;
        this.size = size;
        this.total = total;
        this.totalPages = total > 0 ? (int) Math.ceil((double) total / size) : 0;
        this.data = data;
        
        // 计算分页控件相关字段
        this.hasNext = page < this.totalPages;
        this.hasPrev = page > 1;
        this.pageNumbers = calculatePageNumbers(page, this.totalPages);
    }
    
    /**
     * 计算显示的页码列表
     */
    private List<Integer> calculatePageNumbers(int currentPage, int totalPages) {
        List<Integer> pages = new ArrayList<>();
        
        if (totalPages <= 0) {
            return pages;
        }
        
        // 显示当前页前后各2页，最多显示7个页码
        int start = Math.max(1, currentPage - 2);
        int end = Math.min(totalPages, currentPage + 2);
        
        // 如果总页数较少，调整显示范围
        if (end - start < 4) {
            if (start == 1) {
                end = Math.min(totalPages, start + 4);
            } else if (end == totalPages) {
                start = Math.max(1, end - 4);
            }
        }
        
        for (int i = start; i <= end; i++) {
            pages.add(i);
        }
        
        return pages;
    }
}