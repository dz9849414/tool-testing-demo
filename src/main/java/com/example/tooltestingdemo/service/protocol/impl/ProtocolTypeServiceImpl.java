package com.example.tooltestingdemo.service.protocol.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.tooltestingdemo.entity.protocol.ProtocolType;
import com.example.tooltestingdemo.mapper.protocol.ProtocolTypeMapper;
import com.example.tooltestingdemo.service.protocol.IProtocolTypeService;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 协议类型主表 服务实现类
 * </p>
 *
 * @author wanggang
 * @since 2026-04-13
 */
@Service
public class ProtocolTypeServiceImpl extends ServiceImpl<ProtocolTypeMapper, ProtocolType> implements IProtocolTypeService {

}
