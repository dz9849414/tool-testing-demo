package com.example.tooltestingdemo.aspect;

import com.example.tooltestingdemo.entity.SysOperationLog;
import com.example.tooltestingdemo.service.SecurityService;
import com.example.tooltestingdemo.service.SysOperationLogService;
import com.example.tooltestingdemo.util.OperationLogNameUtils;
import jakarta.servlet.http.HttpServletRequest;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

@Aspect
@Component
@RequiredArgsConstructor
public class OperationLogAspect {

    private static final Map<String, String> ACTION_NAME_MAP = new HashMap<>();

    static {
        ACTION_NAME_MAP.put("login", "登录");
        ACTION_NAME_MAP.put("register", "注册");
        ACTION_NAME_MAP.put("create", "新增");
        ACTION_NAME_MAP.put("createconfig", "新增配置");
        ACTION_NAME_MAP.put("createchart", "新增图表");
        ACTION_NAME_MAP.put("createreport", "新增报表");
        ACTION_NAME_MAP.put("createtemplate", "新增模板");
        ACTION_NAME_MAP.put("createjob", "新增任务");
        ACTION_NAME_MAP.put("createfolder", "新增目录");
        ACTION_NAME_MAP.put("createenvironment", "新增环境");
        ACTION_NAME_MAP.put("createprotocoltype", "新增协议类型");
        ACTION_NAME_MAP.put("createuser", "新增用户");
        ACTION_NAME_MAP.put("createrole", "新增角色");
        ACTION_NAME_MAP.put("updatestatus", "更新状态");
        ACTION_NAME_MAP.put("updatebuiltinstatus", "更新内置状态");
        ACTION_NAME_MAP.put("updateprotocolconfigstatus", "更新协议状态");
        ACTION_NAME_MAP.put("updateprotocoltypestatus", "更新协议类型状态");
        ACTION_NAME_MAP.put("updaterolestatus", "更新角色状态");
        ACTION_NAME_MAP.put("updateuserstatus", "更新用户状态");
        ACTION_NAME_MAP.put("updateconfig", "更新配置");
        ACTION_NAME_MAP.put("updatedictionary", "更新字典");
        ACTION_NAME_MAP.put("updatemenu", "更新菜单");
        ACTION_NAME_MAP.put("updaterole", "更新角色");
        ACTION_NAME_MAP.put("updateuser", "更新用户");
        ACTION_NAME_MAP.put("updatetemplate", "更新模板");
        ACTION_NAME_MAP.put("updatejob", "更新任务");
        ACTION_NAME_MAP.put("updateenvironment", "更新环境");
        ACTION_NAME_MAP.put("updatechart", "更新图表");
        ACTION_NAME_MAP.put("updatereport", "更新报表");
        ACTION_NAME_MAP.put("updatetestresultfield", "更新测试结果字段");
        ACTION_NAME_MAP.put("batchupdatemenustatus", "批量更新菜单状态");
        ACTION_NAME_MAP.put("batchupdaterolestatus", "批量更新角色状态");
        ACTION_NAME_MAP.put("modify", "修改");
        ACTION_NAME_MAP.put("modifyprotocoltype", "修改协议类型");
        ACTION_NAME_MAP.put("customizechart", "自定义图表");
        ACTION_NAME_MAP.put("createcustomchart", "新增自定义图表");
        ACTION_NAME_MAP.put("updatecustomchart", "修改自定义图表");
        ACTION_NAME_MAP.put("savechartastemplate", "保存图表模板");
        ACTION_NAME_MAP.put("delete", "删除");
        ACTION_NAME_MAP.put("deleteconfig", "删除配置");
        ACTION_NAME_MAP.put("deletedictionary", "删除字典");
        ACTION_NAME_MAP.put("deletedictionaries", "批量删除字典");
        ACTION_NAME_MAP.put("deletemenu", "删除菜单");
        ACTION_NAME_MAP.put("deleterole", "删除角色");
        ACTION_NAME_MAP.put("deleteuser", "删除用户");
        ACTION_NAME_MAP.put("deleteprotocoltype", "删除协议类型");
        ACTION_NAME_MAP.put("batchdeleteprotocoltypes", "批量删除协议类型");
        ACTION_NAME_MAP.put("deletechart", "删除图表");
        ACTION_NAME_MAP.put("deletecharttemplate", "删除图表模板");
        ACTION_NAME_MAP.put("deletereport", "删除报表");
        ACTION_NAME_MAP.put("deletetemplate", "删除模板");
        ACTION_NAME_MAP.put("deletefile", "删除文件");
        ACTION_NAME_MAP.put("deleteenvironment", "删除环境");
        ACTION_NAME_MAP.put("deletefolder", "删除目录");
        ACTION_NAME_MAP.put("deletejob", "删除任务");
        ACTION_NAME_MAP.put("batchdeletetemplates", "批量删除模板");
        ACTION_NAME_MAP.put("batchdelete", "批量删除");
        ACTION_NAME_MAP.put("import", "导入");
        ACTION_NAME_MAP.put("importtemplates", "导入模板");
        ACTION_NAME_MAP.put("importjobs", "导入任务");
        ACTION_NAME_MAP.put("importprotocoltypes", "导入协议类型");
        ACTION_NAME_MAP.put("importtemplate", "导入模板");
        ACTION_NAME_MAP.put("export", "导出");
        ACTION_NAME_MAP.put("exportjobs", "导出任务");
        ACTION_NAME_MAP.put("exporttemplate", "导出模板");
        ACTION_NAME_MAP.put("exporttemplates", "导出模板");
        ACTION_NAME_MAP.put("exportprotocoltypes", "导出协议类型");
        ACTION_NAME_MAP.put("exportreport", "导出报表");
        ACTION_NAME_MAP.put("batchexportreports", "批量导出报表");
        ACTION_NAME_MAP.put("exportchart", "导出图表");
        ACTION_NAME_MAP.put("batchexportcharts", "批量导出图表");
        ACTION_NAME_MAP.put("exportroleoperationlogs", "导出操作日志");
        ACTION_NAME_MAP.put("exporttojson", "导出JSON");
        ACTION_NAME_MAP.put("exporttopostman", "导出Postman");
        ACTION_NAME_MAP.put("downloadimporttemplate", "下载导入模板");
        ACTION_NAME_MAP.put("downloadimportfailurereport", "下载导入失败报告");
        ACTION_NAME_MAP.put("downloadfile", "下载文件");
        ACTION_NAME_MAP.put("downloadfilebyname", "下载文件");
        ACTION_NAME_MAP.put("execute", "执行");
        ACTION_NAME_MAP.put("executetemplate", "执行模板");
        ACTION_NAME_MAP.put("triggerjob", "执行任务");
        ACTION_NAME_MAP.put("submitbatchtriggerasync", "批量执行任务");
        ACTION_NAME_MAP.put("batchstopjobs", "批量停止任务");
        ACTION_NAME_MAP.put("togglestatus", "切换状态");
        ACTION_NAME_MAP.put("validateimport", "校验导入");
        ACTION_NAME_MAP.put("validatetemplate", "校验模板");
        ACTION_NAME_MAP.put("previewrequest", "预览请求");
        ACTION_NAME_MAP.put("previewreport", "预览报表");
        ACTION_NAME_MAP.put("previewreportpdf", "预览报表PDF");
        ACTION_NAME_MAP.put("previewtemplatexml", "预览模板XML");
        ACTION_NAME_MAP.put("copytemplate", "复制模板");
        ACTION_NAME_MAP.put("publishtemplate", "发布模板");
        ACTION_NAME_MAP.put("archivetemplate", "归档模板");
        ACTION_NAME_MAP.put("movetemplate", "移动模板");
        ACTION_NAME_MAP.put("movefolder", "移动目录");
        ACTION_NAME_MAP.put("savedraft", "保存草稿");
        ACTION_NAME_MAP.put("updatedraft", "更新草稿");
        ACTION_NAME_MAP.put("submitforreview", "提交审核");
        ACTION_NAME_MAP.put("submitforreviewbyid", "提交审核");
        ACTION_NAME_MAP.put("approvetemplate", "审核通过");
        ACTION_NAME_MAP.put("rejecttemplate", "审核驳回");
        ACTION_NAME_MAP.put("favoriteTemplate".toLowerCase(), "收藏模板");
        ACTION_NAME_MAP.put("unfavoritetemplate", "取消收藏");
        ACTION_NAME_MAP.put("followtemplate", "关注模板");
        ACTION_NAME_MAP.put("unfollowtemplate", "取消关注");
        ACTION_NAME_MAP.put("setdefaultenvironment", "设置默认环境");
        ACTION_NAME_MAP.put("cloneenvironment", "复制环境");
        ACTION_NAME_MAP.put("rollbacktoversion", "回滚版本");
        ACTION_NAME_MAP.put("cleanoldhistories", "清理历史");
        ACTION_NAME_MAP.put("assignpermissions", "分配权限");
        ACTION_NAME_MAP.put("removepermissions", "移除权限");
        ACTION_NAME_MAP.put("assignusers", "分配用户");
        ACTION_NAME_MAP.put("removeusers", "移除用户");
        ACTION_NAME_MAP.put("assignroles", "分配角色");
        ACTION_NAME_MAP.put("approveuser", "审批用户");
        ACTION_NAME_MAP.put("changepassword", "修改密码");
        ACTION_NAME_MAP.put("relatebusinesstype", "关联业务类型");
        ACTION_NAME_MAP.put("setschedule", "设置计划");
        ACTION_NAME_MAP.put("setupautoreport", "设置自动报表");
        ACTION_NAME_MAP.put("autogeneratereport", "自动生成报表");
        ACTION_NAME_MAP.put("generatepresetchart", "生成预置图表");
        ACTION_NAME_MAP.put("generatetemplateusagechart", "生成模板使用图表");
        ACTION_NAME_MAP.put("generatetemplateefficiencychart", "生成模板效率图表");
        ACTION_NAME_MAP.put("comparecharts", "对比图表");
        ACTION_NAME_MAP.put("analyzechartdata", "分析图表数据");
        ACTION_NAME_MAP.put("insert", "新增数据");
    }

    private final SysOperationLogService operationLogService;
    private final SecurityService securityService;

    @Pointcut("execution(* com.example.tooltestingdemo.controller..*Controller.*(..))")
    public void operationLogPointcut() {
    }

    @Around("operationLogPointcut()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        if (shouldSkip()) {
            return joinPoint.proceed();
        }

        StopWatch stopWatch = new StopWatch(joinPoint.getSignature().toShortString());
        stopWatch.start();
        Object result = joinPoint.proceed();
        stopWatch.stop();
        recordOperationLog(joinPoint, stopWatch.getTotalTimeMillis(), null);
        return result;
    }

    @AfterThrowing(pointcut = "operationLogPointcut()", throwing = "e")
    public void afterThrowing(JoinPoint joinPoint, Exception e) {
        if (shouldSkip()) {
            return;
        }
        recordOperationLog(joinPoint, 0, e.getMessage());
    }

    private void recordOperationLog(JoinPoint joinPoint, long executeTime, String errorMessage) {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            return;
        }

        HttpServletRequest request = attributes.getRequest();
        Long userId = securityService.getCurrentUserId();
        String username = securityService.getCurrentUsername();
        String roleId = securityService.getCurrentUserRoleId();

        if (userId == null) {
            userId = 123456L;
        }
        if (username == null) {
            username = "anonymous";
        }
        if (roleId == null) {
            roleId = "anonymous";
        }

        SysOperationLog operationLog = new SysOperationLog();
        operationLog.setUserId(String.valueOf(userId));
        operationLog.setUsername(username);
        operationLog.setRoleId(roleId);
        operationLog.setModule(getModuleName(joinPoint));
        operationLog.setOperation(getOperationName(joinPoint));
        operationLog.setMethod(joinPoint.getSignature().getName());
        operationLog.setRequestUrl(request.getRequestURI());
        operationLog.setRequestParams(buildRequestParams(joinPoint.getArgs()));
        operationLog.setIpAddress(request.getRemoteAddr());
        operationLog.setUserAgent(request.getHeader("User-Agent"));
        operationLog.setStatus(errorMessage == null ? 1 : 0);
        operationLog.setErrorMessage(errorMessage);
        operationLog.setExecuteTime(executeTime);
        operationLog.setCreateTime(LocalDateTime.now());

        operationLogService.recordOperationLog(operationLog);
    }

    private String getModuleName(JoinPoint joinPoint) {
        String className = joinPoint.getTarget().getClass().getName();
        String[] parts = className.split("\\.");
        if (parts.length > 0) {
            String controllerName = parts[parts.length - 1];
            if (controllerName.endsWith("Controller")) {
                return controllerName.substring(0, controllerName.length() - 10);
            }
        }
        return "Unknown";
    }

    private String getOperationName(JoinPoint joinPoint) {
        String moduleName = getModuleName(joinPoint);
        String moduleDisplayName = getModuleDisplayName(moduleName);
        String actionName = getActionName(joinPoint);
        if (actionName.equals(joinPoint.getSignature().getName())) {
            return actionName;
        }
        return moduleDisplayName + "-" + actionName;
    }

    private String getActionName(JoinPoint joinPoint) {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        String methodName = joinPoint.getSignature().getName();
        String lowerMethodName = methodName.toLowerCase();
        String exactActionName = ACTION_NAME_MAP.get(lowerMethodName);
        if (exactActionName != null) {
            return exactActionName;
        }
        if (attributes == null) {
            return methodName;
        }

        HttpServletRequest request = attributes.getRequest();
        String httpMethod = request.getMethod();

        if ("POST".equalsIgnoreCase(httpMethod)) {
            if (containsAny(lowerMethodName, "import", "upload")) {
                return "导入";
            }
            if (containsAny(lowerMethodName, "export", "download")) {
                return "导出";
            }
            if (containsAny(lowerMethodName, "execute", "trigger", "run", "start")) {
                return "执行";
            }
            if (containsAny(lowerMethodName, "delete", "remove")) {
                return "删除";
            }
            if (containsAny(lowerMethodName, "update", "edit", "modify", "change")) {
                return "修改";
            }
            return "新增";
        }

        if ("PUT".equalsIgnoreCase(httpMethod) || "PATCH".equalsIgnoreCase(httpMethod)) {
            return "修改";
        }

        if ("DELETE".equalsIgnoreCase(httpMethod)) {
            return "删除";
        }

        return methodName;
    }

    /**
     * 跳过GEt请求
     *
     * @return
     */
    private boolean shouldSkip() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            return true;
        }

        HttpServletRequest request = attributes.getRequest();
        return "GET".equalsIgnoreCase(request.getMethod());
    }

    private String buildRequestParams(Object[] args) {
        if (args == null || args.length == 0) {
            return "[]";
        }

        return Arrays.stream(args)
            .filter(Objects::nonNull)
            .filter(arg -> !(arg instanceof MultipartFile))
            .map(arg -> {
                try {
                    return String.valueOf(arg);
                } catch (Exception e) {
                    return arg.getClass().getSimpleName();
                }
            })
            .collect(Collectors.joining(", ", "[", "]"));
    }

    private boolean containsAny(String value, String... keywords) {
        for (String keyword : keywords) {
            if (value.contains(keyword)) {
                return true;
            }
        }
        return false;
    }

    private String getModuleDisplayName(String moduleName) {
        return OperationLogNameUtils.getModuleDisplayName(moduleName);
    }
}
