package com.example.tooltestingdemo.service.protocol;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.tooltestingdemo.dto.ProtocolConfigCreateDTO;
import com.example.tooltestingdemo.entity.protocol.ProtocolConfig;

/**
 * <p>
 * 协议参数配置表 服务类
 * </p>
 *
 * @author wanggang
 * @since 2026-04-13
 */
public interface IProtocolConfigService extends IService<ProtocolConfig> {

    /**
     * 新增协议配置（含URL/认证/参数模板）
     */
    ProtocolConfig createProtocolConfig(ProtocolConfigCreateDTO dto);
}
