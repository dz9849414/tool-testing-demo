package com.example.tooltestingdemo.util;

import java.util.HashMap;
import java.util.Map;

public final class OperationLogNameUtils {

    private static final Map<String, String> MODULE_NAME_MAP = new HashMap<>();

    static {
        MODULE_NAME_MAP.put("Auth", "认证");
        MODULE_NAME_MAP.put("Hello", "示例");
        MODULE_NAME_MAP.put("SysConfig", "系统配置");
        MODULE_NAME_MAP.put("SysDictionary", "系统字典");
        MODULE_NAME_MAP.put("SysMenu", "系统菜单");
        MODULE_NAME_MAP.put("SysOperationLog", "操作日志");
        MODULE_NAME_MAP.put("SysRole", "角色管理");
        MODULE_NAME_MAP.put("SysUser", "用户管理");
        MODULE_NAME_MAP.put("MockPdmJsonData", "模拟数据");
        MODULE_NAME_MAP.put("ProtocolConfig", "协议配置");
        MODULE_NAME_MAP.put("ProtocolTestRecord", "协议测试记录");
        MODULE_NAME_MAP.put("ProtocolType", "协议类型");
        MODULE_NAME_MAP.put("ReportChart", "报表图表");
        MODULE_NAME_MAP.put("Report", "报表管理");
        MODULE_NAME_MAP.put("ReportTemplate", "报表模板");
        MODULE_NAME_MAP.put("TemplateStatistics", "模板统计");
        MODULE_NAME_MAP.put("InterfaceTemplate", "接口模板");
        MODULE_NAME_MAP.put("TemplateEnvironment", "模板环境");
        MODULE_NAME_MAP.put("TemplateExecute", "模板执行");
        MODULE_NAME_MAP.put("TemplateExecuteLog", "执行日志");
        MODULE_NAME_MAP.put("TemplateFavorite", "模板收藏");
        MODULE_NAME_MAP.put("TemplateFolder", "模板目录");
        MODULE_NAME_MAP.put("TemplateHistory", "模板历史");
        MODULE_NAME_MAP.put("TemplateImport", "模板导入");
        MODULE_NAME_MAP.put("TemplateJob", "模板任务");
    }

    private OperationLogNameUtils() {
    }

    public static String getModuleDisplayName(String moduleName) {
        String chineseName = MODULE_NAME_MAP.get(moduleName);
        if (chineseName == null || chineseName.isEmpty()) {
            return moduleName;
        }
        return chineseName + "(" + moduleName + ")";
    }
}
