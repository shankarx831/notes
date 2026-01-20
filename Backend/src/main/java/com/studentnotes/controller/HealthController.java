package com.studentnotes.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api") // Maps to /api
public class HealthController {

    @org.springframework.beans.factory.annotation.Autowired
    private javax.sql.DataSource dataSource;

    // Maps to /api/health - Pure ping, NO DB interaction (Safe for 5min interval)
    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("OK");
    }

    // Maps to /api/health/db - Touches DB (For manual debugging / Supabase
    // verification)
    @GetMapping("/health/db")
    public ResponseEntity<String> healthCheckDb() {
        try (java.sql.Connection conn = dataSource.getConnection()) {
            java.sql.DatabaseMetaData metaData = conn.getMetaData();
            String dbInfo = String.format("DB OK | %s %s",
                    metaData.getDatabaseProductName(),
                    metaData.getDatabaseProductVersion());
            return ResponseEntity.ok(dbInfo);
        } catch (Exception e) {
            return ResponseEntity.status(503).body("DB FAIL: " + e.getMessage());
        }
    }
}