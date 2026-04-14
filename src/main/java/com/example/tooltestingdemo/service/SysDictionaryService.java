package com.example.tooltestingdemo.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.tooltestingdemo.entity.SysDictionary;

import java.util.List;

/**
 * 数据字典服务接口
 */
public interface SysDictionaryService extends IService<SysDictionary> {

    /**
     * 新增数据字典
     */
    boolean saveDictionary(SysDictionary dictionary);

    /**
     * 更新数据字典
     */
    boolean updateDictionary(SysDictionary dictionary);

    /**
     * 删除数据字典
     */
    boolean deleteDictionary(String id);

    /**
     * 批量删除数据字典
     */
    boolean deleteDictionaries(List<String> ids);

    /**
     * 启用/禁用数据字典
     */
    boolean toggleStatus(String id);

    /**
     * 检查字典键唯一性
     */
    boolean checkCodeUnique(String code, String type, String id);

    /**
     * 分页查询数据字典
     */
    Page<SysDictionary> getDictionariesByPage(Page<SysDictionary> page, String type, String keyword, Integer status);

    /**
     * 根据类型查询数据字典
     */
    List<SysDictionary> getDictionariesByType(String type);

    /**
     * 根据类型和状态查询数据字典
     */
    List<SysDictionary> getDictionariesByTypeAndStatus(String type, Integer status);
}