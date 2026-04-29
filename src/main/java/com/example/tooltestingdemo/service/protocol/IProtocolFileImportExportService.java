package com.example.tooltestingdemo.service.protocol;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.tooltestingdemo.dto.ProtocolFileImportExportQueryDTO;
import com.example.tooltestingdemo.entity.protocol.ProtocolFileImportExport;
import com.example.tooltestingdemo.vo.ProtocolFileImportExportVO;

/**
 * 协议文件导入导出记录表 服务类
 */
public interface IProtocolFileImportExportService extends IService<ProtocolFileImportExport> {

    /**
     * 协议文件导入导出记录分页查询
     */
    IPage<ProtocolFileImportExportVO> getProtocolFileImportExportList(ProtocolFileImportExportQueryDTO dto);
}

