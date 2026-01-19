# Operational Documentation

This document provides instructions for deploying, monitoring, and managing the StudentNotes production environment.

## ⚙️ Environment Variables

The system relies on the following environment variables. In production, these should be managed via Secrets Manager (AWS/GCP) or Vault.

### Backend (`/Backend`)
| Variable | Description | Default |
| :--- | :--- | :--- |
| `DB_URL` | JDBC connection string. | `jdbc:postgresql://localhost:5432/studentnotes_db` |
| `DB_USERNAME` | Database user. | `postgres` |
| `DB_PASSWORD` | Database password. | `password` |
| `JWT_SECRET` | 256-bit secret for token signing. | `[REDACTED_IN_PROD]` |
| `CORS_ALLOWED_ORIGINS` | Permitted frontend origins. | `http://localhost:5173` |

### Frontend (`/Frontend`)
| Variable | Description | Default |
| :--- | :--- | :--- |
| `VITE_API_URL` | Base URL for the Spring Boot API. | `http://localhost:8080/api` |

---

## 🚀 Deployment Profiles

We use Spring Profiles to manage configurations.

*   **`dev` (Default)**:
    *   `h2` console enabled.
    *   `ddl-auto: update`.
    *   Console logging only.
*   **`prod`**:
    *   `ddl-auto: none` (Migrations via Flyway/Liquibase).
    *   JSON logging for ELK/CloudWatch integration.
    *   Enabled HTTPS enforcement.

### Running with Production Profile
```bash
java -jar studentnotes-backend.jar --spring.profiles.active=prod
```

---

## 🛠 Troubleshooting & Debugging

### 1. "Sign In" Button Missing
*   **Cause**: Frontend handshake with `/api/health` failed.
*   **Debug**: Open Browser DevTools -> Network. Check if `GET /api/health` returned 200.
*   **Fix**: Ensure Backend is running and `CORS_ALLOWED_ORIGINS` includes the current frontend URL.

### 2. PDF Download Hanging
*   **Cause**: Large Markdown file (>50 pages) causing `html2canvas` to timeout on mobile browser.
*   **Fix**: Reduce image resolution in Markdown or suggest desktop view for large notes.

### 3. JWT 401 Unauthorized
*   **Cause**: Clock drift between client and server or token expiry (default 24h).
*   **Fix**: User must re-login. Check server logs for `ExpiredJwtException`.

---

## 📈 Monitoring & Logging

*   **Logs**: Backend logs are located in `/var/log/studentnotes/` (if configured) or stdout.
*   **Metrics**: `/api/actuator/metrics` provides JVM and HTTP request throughput data.
*   **Audit**: Directly query the `audit_log` table for sensitive user activities.

---

## 🔄 Recovery & Restart

1.  **Stop**: `kill -15 [PID]` (allows graceful shutdown of Spring context).
2.  **Backup**: Run `pg_dump studentnotes_db > backup.sql`.
3.  **Start**: Ensure database is reachable before starting the JAR.
