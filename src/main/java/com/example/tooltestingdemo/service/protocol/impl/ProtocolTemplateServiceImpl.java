package com.example.tooltestingdemo.service.protocol.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.tooltestingdemo.entity.protocol.ProtocolTemplate;
import com.example.tooltestingdemo.mapper.protocol.ProtocolTemplateMapper;
import com.example.tooltestingdemo.service.protocol.IProtocolTemplateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 协议参数模板表 服务实现类
 * </p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProtocolTemplateServiceImpl extends ServiceImpl<ProtocolTemplateMapper, ProtocolTemplate>
        implements IProtocolTemplateService {

    private final ProtocolTemplateMapper protocolTemplateMapper;
}

