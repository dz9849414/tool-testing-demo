package com.example.tooltestingdemo.service.protocol;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.tooltestingdemo.dto.ProtocolConfigCreateDTO;
import com.example.tooltestingdemo.dto.ProtocolConfigModifyDTO;
import com.example.tooltestingdemo.dto.ProtocolConfigQueryDTO;
import com.example.tooltestingdemo.entity.protocol.ProtocolConfig;
import com.example.tooltestingdemo.vo.ProtocolConfigVO;

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
     * 新增协议配置：校验 URL/认证结构，序列化写入 JSON 字段并保存主表（不含参数模板）。
     */
    ProtocolConfig createProtocolConfig(ProtocolConfigCreateDTO dto);

    /**
     * 分页查询协议配置列表。
     */
    IPage<ProtocolConfigVO> getProtocolConfigList(ProtocolConfigQueryDTO dto);

    /**
     * 根据 ID 查询协议配置详情。
     */
    ProtocolConfigVO getProtocolConfigDetail(Long id);

    /**
     * 编辑协议配置。
     */
    ProtocolConfigVO modifyProtocolConfig(ProtocolConfigModifyDTO dto);

    /**
     * 逻辑删除协议配置。
     */
    void deleteProtocolConfig(Long id);
}
