package com.example.tooltestingdemo.service.protocol.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.tooltestingdemo.entity.protocol.ProtocolParameterConfig;
import com.example.tooltestingdemo.mapper.protocol.ProtocolParameterConfigMapper;
import com.example.tooltestingdemo.service.protocol.IProtocolParameterConfigService;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 协议参数配置表 服务实现类
 * </p>
 *
 * @author wanggang
 * @since 2026-04-13
 */
@Service
public class ProtocolParameterConfigServiceImpl extends ServiceImpl<ProtocolParameterConfigMapper, ProtocolParameterConfig> implements IProtocolParameterConfigService {

}
