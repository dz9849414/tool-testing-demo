package com.example.tooltestingdemo.enums;

/**
 * 枚举使用示例
 */
public class TemplateEnumsUsage {

    /**
     * 使用示例1：根据code获取枚举
     */
    public void example1() {
        // 获取协议类型
        TemplateEnums.ProtocolType protocolType = 
            TemplateEnums.ProtocolType.getByCode("HTTP");
        
        if (protocolType != null) {
            System.out.println(protocolType.getCode());   // HTTP
            System.out.println(protocolType.getDesc());   // HTTP协议
        }
    }

    /**
     * 使用示例2：switch判断
     */
    public void example2(String operationType) {
        TemplateEnums.OperationType type = 
            TemplateEnums.OperationType.getByCode(operationType);
        
        if (type == null) {
            type = TemplateEnums.OperationType.CREATE; // 默认
        }
        
        switch (type) {
            case IMPORT:
                // 处理导入
                break;
            case EXPORT:
                // 处理导出
                break;
            case CREATE:
                // 处理创建
                break;
            default:
                // 其他操作
        }
    }

    /**
     * 使用示例3：验证并返回描述
     */
    public String getProtocolDesc(String code) {
        TemplateEnums.ProtocolType type = 
            TemplateEnums.ProtocolType.getByCode(code);
        
        return type != null ? type.getDesc() : "未知协议";
    }

    /**
     * 使用示例4：遍历所有枚举值
     */
    public void listAllAuthTypes() {
        for (TemplateEnums.AuthType authType : 
             TemplateEnums.AuthType.values()) {
            System.out.println(authType.getCode() + " - " + 
                authType.getDesc());
        }
    }

    /**
     * 使用示例5：在代码中直接判断
     */
    public boolean isSecureProtocol(String protocolCode) {
        return TemplateEnums.ProtocolType.HTTPS.getCode()
            .equalsIgnoreCase(protocolCode);
    }
}
