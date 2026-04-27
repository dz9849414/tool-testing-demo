package com.example.tooltestingdemo.mapper.system;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.tooltestingdemo.entity.system.SysUserPermission;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户权限直接分配Mapper
 */
@Mapper
public interface SysUserPermissionMapper extends BaseMapper<SysUserPermission> {
}