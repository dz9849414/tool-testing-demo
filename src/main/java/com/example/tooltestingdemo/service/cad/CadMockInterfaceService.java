package com.example.tooltestingdemo.service.cad;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.tooltestingdemo.dto.cad.*;
import com.example.tooltestingdemo.entity.cad.CadDataConvertMapping;
import com.example.tooltestingdemo.entity.cad.CadFileConvertConfig;
import com.example.tooltestingdemo.entity.cad.CadMockInterface;
import com.example.tooltestingdemo.vo.cad.CadFileConvertTaskVO;
import com.example.tooltestingdemo.vo.cad.CadConnectivityTestVO;
import com.example.tooltestingdemo.vo.cad.CadMockTestVO;
import com.example.tooltestingdemo.vo.cad.CadUnifiedExecuteVO;

import java.util.List;

public interface CadMockInterfaceService {
    IPage<CadMockInterface> page(CadMockInterfaceQueryDTO dto);

    CadMockInterface saveMockInterface(CadMockInterfaceCreateDTO dto);

    CadMockInterface saveAuthConfig(CadAuthConfigSaveDTO dto);

    List<CadDataConvertMapping> saveDataMappings(CadDataConvertSaveBatchDTO dto);

    List<CadDataConvertMapping> listDataMappings(Long mockInterfaceId);

    CadFileConvertConfig saveFileConvertConfig(CadFileConvertConfigSaveDTO dto);

    List<CadFileConvertConfig> listFileConvertConfigs(CadFileConvertConfigQueryDTO dto);

    CadFileConvertTaskVO executeFileConvert(CadFileConvertExecuteDTO dto);

    CadFileConvertTaskVO getTask(String taskNo);

    CadUnifiedExecuteVO executeUnified(CadMockUnifiedExecuteDTO dto);

    CadMockTestVO test(CadMockTestDTO dto);

    CadConnectivityTestVO connectivityTest(CadConnectivityTestDTO dto);
}
