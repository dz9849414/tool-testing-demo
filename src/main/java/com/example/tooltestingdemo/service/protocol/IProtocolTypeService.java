package com.example.tooltestingdemo.service.protocol;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.tooltestingdemo.entity.protocol.ProtocolType;
import com.example.tooltestingdemo.vo.ProtocolTypeDeleteResultVO;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

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

    IPage<ProtocolType> getProtocolTypeList(ProtocolType protocolType);

    ProtocolType modifyProtocolType(ProtocolType protocolType);

    void exportProtocolTypes(ProtocolType protocolType, HttpServletResponse response) throws IOException;

    void deleteProtocolType(Long id);

    ProtocolTypeDeleteResultVO batchDeleteProtocolTypes(Long[] ids);
}
