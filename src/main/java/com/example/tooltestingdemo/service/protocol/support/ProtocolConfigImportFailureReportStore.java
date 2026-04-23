package com.example.tooltestingdemo.service.protocol.support;

import lombok.Getter;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 协议配置导入失败报告临时存储
 */
@Component
public class ProtocolConfigImportFailureReportStore {

    private static final Duration EXPIRE_DURATION = Duration.ofHours(24);

    private final Map<String, FailureReportResource> reportStore = new ConcurrentHashMap<>();

    public String save(String fileName, byte[] content) {
        cleanupExpiredReports();
        String reportId = UUID.randomUUID().toString().replace("-", "");
        reportStore.put(reportId, new FailureReportResource(fileName, content, LocalDateTime.now()));
        return reportId;
    }

    public FailureReportResource get(String reportId) {
        cleanupExpiredReports();
        return reportStore.get(reportId);
    }

    private void cleanupExpiredReports() {
        LocalDateTime now = LocalDateTime.now();
        reportStore.entrySet().removeIf(entry -> Duration.between(entry.getValue().createdAt, now).compareTo(EXPIRE_DURATION) > 0);
    }

    public static class FailureReportResource {
        @Getter
        private final String fileName;
        @Getter
        private final byte[] content;
        private final LocalDateTime createdAt;

        public FailureReportResource(String fileName, byte[] content, LocalDateTime createdAt) {
            this.fileName = fileName;
            this.content = content;
            this.createdAt = createdAt;
        }
    }
}
