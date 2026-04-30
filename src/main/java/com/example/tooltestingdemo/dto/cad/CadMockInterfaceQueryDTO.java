package com.example.tooltestingdemo.dto.cad;

import com.example.tooltestingdemo.common.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class CadMockInterfaceQueryDTO extends PageQuery {
    private String cadType;
    private String convertType;
    private String interfaceName;
    private String applyFlow;
    private Integer status;
}
