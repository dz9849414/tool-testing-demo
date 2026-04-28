package com.example.tooltestingdemo.service.protocol;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.tooltestingdemo.dto.ProtocolTestTransferDTO;
import com.example.tooltestingdemo.entity.protocol.ProtocolTestRecord;

/**
 * <p>
 * 协议测试记录表 服务类
 * </p>
 *
 * @author wanggang
 * @since 2026-04-13
 */
public interface IProtocolTestRecordService extends IService<ProtocolTestRecord> {

    /**
     * 连接测试：从协议配置中选取 primary URL 发起 HTTP GET 测试并落库。
     *
     * @param configId 协议配置ID
     * @return 保存后的测试记录
     */
    ProtocolTestRecord testConnect(Long configId);

    /**
     * 数据传输测试：从协议配置中选取 primary URL 进行请求并落库。
     *
     * @param dto 数据传输测试请求
     * @return 保存后的测试记录
     */
    ProtocolTestRecord testTransfer(ProtocolTestTransferDTO dto);
}
