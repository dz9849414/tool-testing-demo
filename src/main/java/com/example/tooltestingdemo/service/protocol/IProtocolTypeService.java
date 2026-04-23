package com.example.tooltestingdemo.service.protocol;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.tooltestingdemo.dto.ProtocolTypeCreateDTO;
import com.example.tooltestingdemo.dto.ProtocolTypeBatchStatusUpdateDTO;
import com.example.tooltestingdemo.dto.ProtocolTypeQueryDTO;
import com.example.tooltestingdemo.dto.ProtocolTypeModifyDTO;
import com.example.tooltestingdemo.dto.ProtocolTypeStatusUpdateDTO;
import com.example.tooltestingdemo.entity.protocol.ProtocolType;
import com.example.tooltestingdemo.vo.ProtocolTypeDeleteResultVO;
import com.example.tooltestingdemo.vo.ProtocolTypeImportResultVO;
import com.example.tooltestingdemo.vo.ProtocolTypeBatchStatusChangeVO;
import com.example.tooltestingdemo.vo.ProtocolTypeStatusChangeVO;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.multipart.MultipartFile;

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

    ProtocolType createProtocolType(ProtocolTypeCreateDTO dto);

    IPage<ProtocolType> getProtocolTypeList(ProtocolTypeQueryDTO dto);

    ProtocolTypeImportResultVO importProtocolTypes(MultipartFile file, String strategy) throws IOException;

    ProtocolTypeStatusChangeVO updateProtocolTypeStatus(ProtocolTypeStatusUpdateDTO dto);

    ProtocolTypeBatchStatusChangeVO batchUpdateProtocolTypeStatus(ProtocolTypeBatchStatusUpdateDTO dto);

    ProtocolType modifyProtocolType(ProtocolTypeModifyDTO dto);

    void downloadImportTemplate(HttpServletResponse response) throws IOException;

    void downloadImportFailureReport(String reportId, HttpServletResponse response) throws IOException;

    void exportProtocolTypes(ProtocolTypeQueryDTO dto, HttpServletResponse response) throws IOException;

    void deleteProtocolType(Long id);

    ProtocolTypeDeleteResultVO batchDeleteProtocolTypes(Long[] ids);
}
