package com.example.tooltestingdemo.service.protocol;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.tooltestingdemo.entity.protocol.ProtocolType;
import com.example.tooltestingdemo.vo.ProtocolTypeDeleteResultVO;

import java.util.List;

/**
 * <p>
 * 协议类型主表 服务类
 * </p>
 *
 * @author wanggang
 * @since 2026-04-11
 */
public interface IProtocolTypeService extends IService<ProtocolType> {

    ProtocolType createProtocolType(ProtocolType protocolType);

    List<ProtocolType> getProtocolTypeList(ProtocolType protocolType);

    ProtocolType modifyProtocolType(ProtocolType protocolType);

    void deleteProtocolType(Long id);

    ProtocolTypeDeleteResultVO batchDeleteProtocolTypes(Long[] ids);
}
