package com.example.tooltestingdemo.service.protocol.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.tooltestingdemo.entity.protocol.TestDataSample;
import com.example.tooltestingdemo.mapper.protocol.TestDataSampleMapper;
import com.example.tooltestingdemo.service.protocol.ITestDataSampleService;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 测试数据样本库 服务实现类
 * </p>
 *
 * @author wanggang
 * @since 2026-04-13
 */
@Service
public class TestDataSampleServiceImpl extends ServiceImpl<TestDataSampleMapper, TestDataSample> implements ITestDataSampleService {

}
