package com.example.tooltestingdemo.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class StoragePathUtil {

    private static final Logger log = LoggerFactory.getLogger(StoragePathUtil.class);
    
    private static volatile String exportBasePath;

    public static String getExportBasePath() {
        if (exportBasePath == null) {
            synchronized (StoragePathUtil.class) {
                if (exportBasePath == null) {
                    exportBasePath = initExportBasePath();
                }
            }
        }
        return exportBasePath;
    }

    private static String initExportBasePath() {
        String customPath = System.getenv("TOOL_TESTING_EXPORT_PATH");
        if (customPath != null && !customPath.isEmpty()) {
            File customDir = new File(customPath);
            if (customDir.exists() || customDir.mkdirs()) {
                log.info("使用自定义导出路径: {}", customPath);
                return customPath;
            }
            log.warn("自定义导出路径创建失败: {}", customPath);
        }

        boolean isDocker = isRunningInDocker();
        String os = System.getProperty("os.name").toLowerCase();
        
        if (os.contains("win")) {
            return initWindowsPath();
        } else {
            return initLinuxPath(isDocker);
        }
    }

    private static boolean isRunningInDocker() {
        try {
            File dockerFile = new File("/.dockerenv");
            if (dockerFile.exists()) {
                return true;
            }
            
            String cgroup = readFile("/proc/1/cgroup");
            if (cgroup != null && (cgroup.contains("docker") || cgroup.contains("containerd"))) {
                return true;
            }
        } catch (Exception e) {
            log.debug("检测Docker环境失败: {}", e.getMessage());
        }
        return false;
    }
    
    private static String readFile(String path) {
        try {
            return Files.readString(Paths.get(path));
        } catch (Exception e) {
            return null;
        }
    }

    private static String initWindowsPath() {
        char[] drives = {'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z'};
        
        for (char drive : drives) {
            String drivePath = drive + ":\\tool-testing-export";
            File dir = new File(drivePath);
            
            if (isDriveAvailable(drive + ":")) {
                try {
                    if (!dir.exists()) {
                        if (dir.mkdirs()) {
                            log.info("创建导出目录成功: {}", drivePath);
                            return drivePath;
                        } else {
                            log.warn("创建导出目录失败: {}", drivePath);
                            continue;
                        }
                    } else {
                        log.info("使用已有导出目录: {}", drivePath);
                        return drivePath;
                    }
                } catch (Exception e) {
                    log.warn("检查盘符 {} 失败: {}", drive, e.getMessage());
                    continue;
                }
            }
        }

        String fallbackPath = "C:\\tool-testing-export";
        log.warn("未找到可用盘符，使用备用路径: {}", fallbackPath);
        File fallbackDir = new File(fallbackPath);
        if (!fallbackDir.exists()) {
            fallbackDir.mkdirs();
        }
        return fallbackPath;
    }

    private static boolean isDriveAvailable(String driveLetter) {
        try {
            File file = new File(driveLetter);
            return file.exists() && file.canRead();
        } catch (Exception e) {
            return false;
        }
    }

    private static String initLinuxPath(boolean isDocker) {
        String[] possiblePaths;
        
        if (isDocker) {
            possiblePaths = new String[]{
                "/exports",
                "/app/exports",
                "/app/data/exports",
                System.getProperty("java.io.tmpdir") + "/exports"
            };
            log.info("检测到运行环境: Docker容器");
        } else {
            possiblePaths = new String[]{
                "/data/tool-testing-export",
                "/var/tool-testing-export",
                "/tmp/tool-testing-export",
                System.getProperty("user.home") + "/tool-testing-export"
            };
            log.info("检测到运行环境: Linux物理机/虚拟机");
        }

        for (String path : possiblePaths) {
            File dir = new File(path);
            try {
                if (!dir.exists()) {
                    if (dir.mkdirs()) {
                        log.info("创建导出目录成功: {}", path);
                        return path;
                    } else {
                        log.warn("创建导出目录失败: {}", path);
                        continue;
                    }
                } else if (dir.canWrite()) {
                    log.info("使用已有导出目录: {}", path);
                    return path;
                } else {
                    log.warn("目录存在但不可写: {}", path);
                }
            } catch (Exception e) {
                log.warn("检查路径 {} 失败: {}", path, e.getMessage());
                continue;
            }
        }

        String fallbackPath = System.getProperty("java.io.tmpdir") + "/tool-testing-export";
        log.warn("未找到合适路径，使用备用路径: {}", fallbackPath);
        File fallbackDir = new File(fallbackPath);
        if (!fallbackDir.exists()) {
            fallbackDir.mkdirs();
        }
        return fallbackPath;
    }

    public static String getReportExportPath() {
        return getExportBasePath() + File.separator + "reports";
    }

    public static String getChartExportPath() {
        return getExportBasePath() + File.separator + "charts";
    }

    public static void ensureDirectoryExists(String path) {
        File dir = new File(path);
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }

    public static void reset() {
        exportBasePath = null;
    }
}