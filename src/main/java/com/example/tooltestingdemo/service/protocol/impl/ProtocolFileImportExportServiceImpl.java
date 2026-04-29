package com.example.tooltestingdemo.service.protocol.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.tooltestingdemo.dto.ProtocolFileImportExportQueryDTO;
import com.example.tooltestingdemo.entity.protocol.ProtocolFileImportExport;
import com.example.tooltestingdemo.mapper.protocol.ProtocolFileImportExportMapper;
import com.example.tooltestingdemo.service.protocol.IProtocolFileImportExportService;
import com.example.tooltestingdemo.vo.ProtocolFileImportExportVO;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * 协议文件导入导出记录表 服务实现类
 */
@Service
public class ProtocolFileImportExportServiceImpl
        extends ServiceImpl<ProtocolFileImportExportMapper, ProtocolFileImportExport>
        implements IProtocolFileImportExportService {

    @Override
    public IPage<ProtocolFileImportExportVO> getProtocolFileImportExportList(ProtocolFileImportExportQueryDTO dto) {
        ProtocolFileImportExportQueryDTO query = dto == null ? new ProtocolFileImportExportQueryDTO() : dto;
        IPage<ProtocolFileImportExport> page = this.page(query.toPage(), buildQueryWrapper(query));
        return page.convert(this::toVO);
    }

    private LambdaQueryWrapper<ProtocolFileImportExport> buildQueryWrapper(ProtocolFileImportExportQueryDTO query) {
        LambdaQueryWrapper<ProtocolFileImportExport> wrapper = new LambdaQueryWrapper<>();
        if (query != null) {
            if (StringUtils.isNotBlank(query.getOperationType())) {
                wrapper.eq(ProtocolFileImportExport::getOperationType, query.getOperationType().trim().toUpperCase());
            }
            if (query.getProtocolConfigId() != null) {
                // 协议配置ID集合为字符串字段，这里做模糊匹配以满足查询需求
                wrapper.like(ProtocolFileImportExport::getProtocolConfigIds, String.valueOf(query.getProtocolConfigId()));
            }
            if (StringUtils.isNotBlank(query.getFileName())) {
                wrapper.like(ProtocolFileImportExport::getFileName, query.getFileName().trim());
            }
            if (StringUtils.isNotBlank(query.getFileFormat())) {
                wrapper.like(ProtocolFileImportExport::getFileFormat, query.getFileFormat().trim());
            }
            if (query.getStatus() != null) {
                wrapper.eq(ProtocolFileImportExport::getStatus, query.getStatus());
            }

            // 图片字段：开始/结束时间
            applyDateTimeRange(wrapper, query.getStartTimeStart(), query.getStartTimeEnd(), ProtocolFileImportExport::getStartTime);
            applyDateTimeRange(wrapper, query.getEndTimeStart(), query.getEndTimeEnd(), ProtocolFileImportExport::getEndTime);

            // 创建时间筛选
            applyDateTimeRange(wrapper, query.getCreateTimeStart(), query.getCreateTimeEnd(), ProtocolFileImportExport::getCreateTime);
        }

        wrapper.orderByDesc(ProtocolFileImportExport::getCreateTime).orderByDesc(ProtocolFileImportExport::getId);
        return wrapper;
    }

    private void applyDateTimeRange(LambdaQueryWrapper<ProtocolFileImportExport> queryWrapper,
                                     LocalDateTime start,
                                     LocalDateTime end,
                                     SFunction<ProtocolFileImportExport, ?> column) {
        Optional.ofNullable(start).ifPresent(value -> queryWrapper.ge(column, value));
        Optional.ofNullable(end).ifPresent(value -> queryWrapper.le(column, value));
    }

    private ProtocolFileImportExportVO toVO(ProtocolFileImportExport entity) {
        if (entity == null) {
            return null;
        }
        ProtocolFileImportExportVO vo = new ProtocolFileImportExportVO();
        vo.setId(entity.getId());
        vo.setOperationType(entity.getOperationType());
        vo.setOperationTypeText(resolveOperationTypeText(entity.getOperationType()));
        vo.setProtocolConfigIds(entity.getProtocolConfigIds());
        vo.setFileName(entity.getFileName());
        vo.setFileFormat(entity.getFileFormat());
        vo.setStatus(entity.getStatus());
        vo.setStatusText(resolveStatusText(entity.getStatus()));
        vo.setSuccessCount(entity.getSuccessCount());
        vo.setFailCount(entity.getFailCount());
        vo.setErrorMessage(entity.getErrorMessage());
        vo.setStartTime(entity.getStartTime());
        vo.setEndTime(entity.getEndTime());
        vo.setCreateId(entity.getCreateId());
        vo.setCreateName(entity.getCreateName());
        vo.setCreateTime(entity.getCreateTime());
        return vo;
    }

    private String resolveOperationTypeText(String operationType) {
        if (operationType == null) {
            return "";
        }
        String type = operationType.trim().toUpperCase();
        return switch (type) {
            case "EXPORT" -> "导出";
            case "IMPORT" -> "导入";
            default -> type;
        };
    }

    private String resolveStatusText(Integer status) {
        if (status == null) {
            return "";
        }
        return switch (status) {
            case 0 -> "处理中";
            case 1 -> "成功";
            case 2 -> "部分成功";
            case 3 -> "失败";
            default -> String.valueOf(status);
        };
    }
}

