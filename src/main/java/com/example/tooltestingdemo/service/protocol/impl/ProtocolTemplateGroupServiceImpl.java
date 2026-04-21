package com.example.tooltestingdemo.service.protocol.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.tooltestingdemo.entity.protocol.ProtocolTemplateGroup;
import com.example.tooltestingdemo.mapper.protocol.ProtocolTemplateGroupMapper;
import com.example.tooltestingdemo.service.protocol.IProtocolTemplateGroupService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 协议参数模板分组表 服务实现类
 * </p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProtocolTemplateGroupServiceImpl
        extends ServiceImpl<ProtocolTemplateGroupMapper, ProtocolTemplateGroup>
        implements IProtocolTemplateGroupService {

    private final ProtocolTemplateGroupMapper protocolTemplateGroupMapper;
}

