package com.zionysus.dearme.application;

import com.zionysus.dearme.domain.session.Session;
import com.zionysus.dearme.node.ReportBuildNode;
import com.zionysus.dearme.south.port.SessionRepositoryPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 报告应用服务（编排）。调 ReportBuildNode 生成报告并回写 session。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ReportAppService {

    private final ReportBuildNode reportBuildNode;
    private final SessionRepositoryPort sessionRepository;

    public String generateReport(String sessionId) {
        Session s = sessionRepository.findById(sessionId).orElseGet(() -> {
            log.error("[ReportAppService] session 不存在: {}", sessionId);
            return null;
        });
        if (s == null) {
            return null;
        }
        String markdown = reportBuildNode.build(s);
        if (markdown == null) {
            return null;
        }
        sessionRepository.save(s);
        return markdown;
    }
}