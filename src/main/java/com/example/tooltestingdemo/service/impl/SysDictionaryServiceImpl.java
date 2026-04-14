package com.example.tooltestingdemo.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.tooltestingdemo.entity.SysDictionary;
import com.example.tooltestingdemo.mapper.SysDictionaryMapper;
import com.example.tooltestingdemo.service.SysDictionaryService;
import com.example.tooltestingdemo.service.SecurityService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 数据字典服务实现类
 */
@Service
@RequiredArgsConstructor
public class SysDictionaryServiceImpl extends ServiceImpl<SysDictionaryMapper, SysDictionary> implements SysDictionaryService {

    private final SysDictionaryMapper dictionaryMapper;
    private final SecurityService securityService;

    @Override
    public boolean saveDictionary(SysDictionary dictionary) {
        // 检查字典是否已存在
        if (!checkCodeUnique(dictionary.getCode(), dictionary.getType(), null)) {
            return false;
        }
        // 设置创建时间和更新时间
        dictionary.setCreateTime(LocalDateTime.now());
        dictionary.setUpdateTime(LocalDateTime.now());
        return save(dictionary);
    }

    @Override
    public boolean updateDictionary(SysDictionary dictionary) {
        // 设置更新时间
        dictionary.setUpdateTime(LocalDateTime.now());
        return updateById(dictionary);
    }

    @Override
    public boolean deleteDictionary(String id) {
        return removeById(id);
    }

    @Override
    public boolean deleteDictionaries(List<String> ids) {
        return removeByIds(ids);
    }

    @Override
    public boolean toggleStatus(String id) {
        SysDictionary dictionary = getById(id);
        if (dictionary == null) {
            return false;
        }
        // 切换状态
        dictionary.setStatus(dictionary.getStatus() == 1 ? 0 : 1);
        dictionary.setUpdateTime(LocalDateTime.now());
        return updateById(dictionary);
    }

    @Override
    public boolean checkCodeUnique(String code, String type, String id) {
        LambdaQueryWrapper<SysDictionary> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SysDictionary::getCode, code)
                .eq(SysDictionary::getType, type);
        if (id != null) {
            queryWrapper.ne(SysDictionary::getId, id);
        }
        return count(queryWrapper) == 0;
    }

    @Override
    public Page<SysDictionary> getDictionariesByPage(Page<SysDictionary> page, String type, String keyword, Integer status) {
        LambdaQueryWrapper<SysDictionary> queryWrapper = new LambdaQueryWrapper<>();

        if (type != null) {
            queryWrapper.eq(SysDictionary::getType, type);
        }

        if (keyword != null) {
            queryWrapper.and(wrapper -> wrapper
                    .like(SysDictionary::getCode, keyword)
                    .or().like(SysDictionary::getValue, keyword)
            );
        }

        if (status != null) {
            queryWrapper.eq(SysDictionary::getStatus, status);
        }

        queryWrapper.orderByAsc(SysDictionary::getType)
                .orderByAsc(SysDictionary::getSort);

        return page(page, queryWrapper);
    }

    @Override
    public List<SysDictionary> getDictionariesByType(String type) {
        LambdaQueryWrapper<SysDictionary> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SysDictionary::getType, type)
                .eq(SysDictionary::getStatus, 1)
                .orderByAsc(SysDictionary::getSort);
        return list(queryWrapper);
    }

    @Override
    public List<SysDictionary> getDictionariesByTypeAndStatus(String type, Integer status) {
        LambdaQueryWrapper<SysDictionary> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SysDictionary::getType, type)
                .eq(SysDictionary::getStatus, status)
                .orderByAsc(SysDictionary::getSort);
        return list(queryWrapper);
    }
}