package com.example.tooltestingdemo.service.impl.mock;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.tooltestingdemo.dto.MockPdmJsonInsertRequest;
import com.example.tooltestingdemo.entity.mock.MockPdmJsonData;
import com.example.tooltestingdemo.exception.TemplateValidationException;
import com.example.tooltestingdemo.mapper.mock.MockPdmJsonDataMapper;
import com.example.tooltestingdemo.service.mock.MockPdmJsonDataService;
import com.example.tooltestingdemo.vo.MockPdmJsonDataVO;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

import static com.example.tooltestingdemo.exception.TemplateValidationException.ErrorType.RWEMARK_EXISTS;

/**
 * PDM 模拟 JSON 数据服务实现。
 */
@Service
@RequiredArgsConstructor
public class MockPdmJsonDataServiceImpl implements MockPdmJsonDataService {

    private static final DateTimeFormatter CODE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    private static final DateTimeFormatter DISPLAY_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final MockPdmJsonDataMapper mockPdmJsonDataMapper;
    private final ObjectMapper objectMapper;

    @Override
    public MockPdmJsonDataVO insert(Object dataJson, MockPdmJsonInsertRequest request) {
        if (StringUtils.isBlank(request.getRemark())) {
            throw new TemplateValidationException(RWEMARK_EXISTS,"缺少remark字段");
        }
        MockPdmJsonData entity = new MockPdmJsonData();
        entity.setDataJson(toJson(dataJson != null ? dataJson : buildSampleData()));
        entity.setRemark(request.getRemark());
        mockPdmJsonDataMapper.insert(entity);
        return toVO(entity);
    }

    @Override
    public MockPdmJsonDataVO getById(Long id) {
        MockPdmJsonData entity = mockPdmJsonDataMapper.selectById(id);
        return entity == null ? null : toVO(entity);
    }

    @Override
    public List<MockPdmJsonDataVO> listLatest(int limit) {
        LambdaQueryWrapper<MockPdmJsonData> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(MockPdmJsonData::getId).last("LIMIT " + Math.max(limit, 1));
        List<MockPdmJsonData> entities = mockPdmJsonDataMapper.selectList(wrapper);
        List<MockPdmJsonDataVO> result = new ArrayList<>();
        for (MockPdmJsonData entity : entities) {
            result.add(toVO(entity));
        }
        return result;
    }

    private MockPdmJsonDataVO toVO(MockPdmJsonData entity) {
        MockPdmJsonDataVO vo = new MockPdmJsonDataVO();
        vo.setId(entity.getId());
        vo.setDataJson(parseJson(entity.getDataJson()));
        vo.setRemark(entity.getRemark());
        return vo;
    }

    private String toJson(Object dataJson) {
        try {
            return objectMapper.writeValueAsString(dataJson);
        } catch (Exception e) {
            throw new IllegalArgumentException("JSON 数据序列化失败: " + e.getMessage(), e);
        }
    }

    private Object parseJson(String dataJson) {
        try {
            return objectMapper.readValue(dataJson, new TypeReference<Object>() {
            });
        } catch (Exception e) {
            return dataJson;
        }
    }

    private Map<String, Object> buildSampleData() {
        LocalDateTime now = LocalDateTime.now();
        String codeTime = now.format(CODE_TIME_FORMATTER);
        String displayTime = now.format(DISPLAY_TIME_FORMATTER);
        String projectCode = "SHIP-" + codeTime;
        String shipCode = "VESSEL-" + codeTime;
        String partCode = "STR-" + codeTime;

        Map<String, Object> root = new LinkedHashMap<>();
        root.put("project", buildProject(projectCode, displayTime));
        root.put("ship", buildShip(projectCode, shipCode));
        root.put("part", buildPart(partCode));
        root.put("document", buildDocument(codeTime, displayTime));
        root.put("bom", buildBom(codeTime));
        root.put("block", buildBlock());
        root.put("system", buildSystem());
        root.put("equipment", buildEquipment());
        root.put("pipe", buildPipe());
        root.put("audit", buildAudit(displayTime));
        return root;
    }

    private Map<String, Object> buildProject(String projectCode, String displayTime) {
        Map<String, Object> project = new LinkedHashMap<>();
        project.put("projectId", null);
        project.put("projectCode", projectCode);
        project.put("projectName", "25K DWT 散货船");
        project.put("projectType", "散货船");
        project.put("owner", "中远海运");
        project.put("shipyard", "沪东船厂");
        project.put("startDate", "2026-04-01");
        project.put("deliveryDate", "2027-02-28");
        project.put("status", "建造");
        project.put("creator", "system");
        project.put("createTime", displayTime);
        project.put("updateBy", "system");
        project.put("updateTime", displayTime);
        project.put("remark", "接口自动生成模拟项目");
        return project;
    }

    private Map<String, Object> buildShip(String projectCode, String shipCode) {
        Map<String, Object> ship = new LinkedHashMap<>();
        ship.put("shipId", null);
        ship.put("shipCode", shipCode);
        ship.put("shipName", "海盛001");
        ship.put("imoNumber", "IMO" + randomDigits(6));
        ship.put("classSociety", "CCS");
        ship.put("lengthOverall", 180.5);
        ship.put("breadth", 32.2);
        ship.put("depth", 18.6);
        ship.put("draft", 11.2);
        ship.put("deadweight", 25000.0);
        ship.put("shipType", "散货船");
        ship.put("projectId", projectCode);
        ship.put("status", "建造");
        return ship;
    }

    private Map<String, Object> buildPart(String partCode) {
        Map<String, Object> part = new LinkedHashMap<>();
        part.put("partId", null);
        part.put("partCode", partCode);
        part.put("partName", "船底板");
        part.put("partType", "结构件");
        part.put("material", "AH36");
        part.put("specification", "18x2400x12000");
        part.put("unit", "件");
        part.put("standard", "GB712");
        part.put("drawingNo", "DWG-STR-001");
        part.put("parentId", null);
        part.put("status", "发布");
        part.put("version", "V1.0");
        return part;
    }

    private Map<String, Object> buildDocument(String codeTime, String displayTime) {
        Map<String, Object> document = new LinkedHashMap<>();
        document.put("docId", null);
        document.put("docCode", "DWG-" + codeTime);
        document.put("docName", "总布置图");
        document.put("docType", "图纸");
        document.put("format", "PDF");
        document.put("version", "V1.0");
        document.put("status", "发布");
        document.put("filePath", "/mock/pdm/" + codeTime + "/general-arrangement.pdf");
        document.put("relatedPartId", null);
        document.put("relatedShipId", null);
        document.put("creator", "system");
        document.put("createTime", displayTime);
        return document;
    }

    private Map<String, Object> buildBom(String codeTime) {
        Map<String, Object> bom = new LinkedHashMap<>();
        bom.put("bomId", null);
        bom.put("bomCode", "BOM-" + codeTime);
        bom.put("bomName", "船体结构BOM");
        bom.put("parentPartId", null);
        bom.put("childPartId", null);
        bom.put("quantity", 12.0);
        bom.put("usage", "货舱区");
        bom.put("shipId", null);
        bom.put("version", "V1.0");
        bom.put("status", "生效");
        return bom;
    }

    private Map<String, Object> buildBlock() {
        Map<String, Object> block = new LinkedHashMap<>();
        block.put("blockId", null);
        block.put("blockCode", "001-01-001");
        block.put("blockName", "船底分段");
        block.put("shipId", null);
        block.put("blockType", "底部分段");
        block.put("weight", 128.5);
        block.put("constructionArea", "一号车间");
        block.put("preOutfitDate", "2026-05-20");
        block.put("erectionDate", "2026-06-15");
        block.put("status", "组装");
        return block;
    }

    private Map<String, Object> buildSystem() {
        Map<String, Object> system = new LinkedHashMap<>();
        system.put("systemId", null);
        system.put("systemCode", "SYS-MECH-001");
        system.put("systemName", "轮机系统");
        system.put("systemType", "机械");
        system.put("shipId", null);
        system.put("description", "主推进与辅机系统");
        return system;
    }

    private Map<String, Object> buildEquipment() {
        Map<String, Object> equipment = new LinkedHashMap<>();
        equipment.put("equipId", null);
        equipment.put("equipCode", "EQ-MAIN-001");
        equipment.put("equipName", "主机");
        equipment.put("equipType", "动力");
        equipment.put("manufacturer", "MAN");
        equipment.put("model", "6S50ME-C");
        equipment.put("specification", "9480kW");
        equipment.put("systemId", null);
        equipment.put("shipId", null);
        equipment.put("drawingNo", "EQ-DWG-001");
        return equipment;
    }

    private Map<String, Object> buildPipe() {
        Map<String, Object> pipe = new LinkedHashMap<>();
        pipe.put("pipeId", null);
        pipe.put("pipeCode", "PIPE-FO-001");
        pipe.put("pipeName", "燃油主管");
        pipe.put("pipeMaterial", "碳钢");
        pipe.put("diameter", 168.3);
        pipe.put("thickness", 7.11);
        pipe.put("fluidType", "燃油");
        pipe.put("pressureClass", "PN16");
        pipe.put("systemId", null);
        return pipe;
    }

    private Map<String, Object> buildAudit(String displayTime) {
        Map<String, Object> audit = new LinkedHashMap<>();
        audit.put("creator", "system");
        audit.put("createTime", displayTime);
        audit.put("updateBy", "system");
        audit.put("updateTime", displayTime);
        audit.put("status", "有效");
        audit.put("version", "V1.0");
        audit.put("remark", "接口自动生成模拟数据");
        audit.put("tenantId", 1L);
        audit.put("deleteFlag", "0");
        return audit;
    }

    private String randomDigits(int length) {
        StringBuilder builder = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            builder.append(ThreadLocalRandom.current().nextInt(10));
        }
        return builder.toString();
    }
}
