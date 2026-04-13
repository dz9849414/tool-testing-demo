package com.example.tooltestingdemo.service.protocol.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.tooltestingdemo.entity.protocol.ProtocolTestRecord;
import com.example.tooltestingdemo.mapper.protocol.ProtocolTestRecordMapper;
import com.example.tooltestingdemo.service.protocol.IProtocolTestRecordService;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 协议测试记录表 服务实现类
 * </p>
 *
 * @author wanggang
 * @since 2026-04-13
 */
@Service
public class ProtocolTestRecordServiceImpl extends ServiceImpl<ProtocolTestRecordMapper, ProtocolTestRecord> implements IProtocolTestRecordService {

}
