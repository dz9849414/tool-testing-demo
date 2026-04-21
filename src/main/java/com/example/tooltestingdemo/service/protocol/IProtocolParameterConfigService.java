package com.example.tooltestingdemo.service.protocol;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.tooltestingdemo.dto.ProtocolParameterConfigCreateDTO;
import com.example.tooltestingdemo.entity.protocol.ProtocolConfig;

/**
 * <p>
 * 协议参数配置表 服务类
 * </p>
 *
 * @author wanggang
 * @since 2026-04-13
 */
public interface IProtocolParameterConfigService extends IService<ProtocolConfig> {

    ProtocolConfig createProtocolParameterConfig(ProtocolParameterConfigCreateDTO dto);

}
