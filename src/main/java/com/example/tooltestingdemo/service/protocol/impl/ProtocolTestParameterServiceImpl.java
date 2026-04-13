package com.example.tooltestingdemo.service.protocol.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.tooltestingdemo.entity.protocol.ProtocolTestParameter;
import com.example.tooltestingdemo.mapper.protocol.ProtocolTestParameterMapper;
import com.example.tooltestingdemo.service.protocol.IProtocolTestParameterService;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 协议测试参数表 服务实现类
 * </p>
 *
 * @author wanggang
 * @since 2026-04-13
 */
@Service
public class ProtocolTestParameterServiceImpl extends ServiceImpl<ProtocolTestParameterMapper, ProtocolTestParameter> implements IProtocolTestParameterService {

}
