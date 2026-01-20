# Operational Documentation

This document provides instructions for deploying, monitoring, and managing the StudentNotes production environment.

## ⚙️ Environment Variables

The system relies on the following environment variables. In production, these should be managed via Secrets Manager (AWS/GCP) or Vault.

### Backend (`/Backend`)
| Variable | Description | Example (Supabase) |
| :--- | :--- | :--- |
| `DB_URL` | JDBC connection string (pooled). | `jdbc:postgresql://aws-0-[region].pooler.supabase.com:6543/postgres?sslmode=require` |
| `DB_USERNAME` | Database user. | `postgres.[project-ref]` |
| `DB_PASSWORD` | Database password. | `[your-supabase-password]` |
| `JWT_SECRET` | 256-bit secret for token signing. | `[REDACTED_IN_PROD]` |
| `CORS_ALLOWED_ORIGINS` | Permitted frontend origins. | `http://localhost:5173` |

### Frontend (`/Frontend`)
| Variable | Description | Default |
| :--- | :--- | :--- |
| `VITE_API_URL` | Base URL for the Spring Boot API. | `http://localhost:8080/api` |

---

## 🗄️ Database (Supabase PostgreSQL)

The production database is hosted on **Supabase**, a fully-managed PostgreSQL service.

### Key Characteristics
-   **Always-On**: No cold starts. Database is immediately available 24/7.
-   **Connection Pooling**: Use Supavisor (port 6543) for application connections.
-   **Storage Limit**: 500 MB on free tier.
-   **SSL Required**: All connections must use `sslmode=require`.

### Connection String Format
```
jdbc:postgresql://aws-0-[region].pooler.supabase.com:6543/postgres?sslmode=require
```

### Direct Connection (Admin/Migrations Only)
For running migrations or manual SQL:
```
postgresql://postgres.[project-ref]:[password]@db.[project-ref].supabase.co:5432/postgres
```

### Supabase Dashboard
-   **URL**: https://supabase.com/dashboard
-   **Table Editor**: Visual interface for inspecting data.
-   **SQL Editor**: Run ad-hoc queries.
-   **Logs**: View database query logs and errors.

---

## 🚀 Deployment Profiles

We use Spring Profiles to manage configurations.

*   **`dev` (Default)**:
    *   `h2` console enabled.
    *   `ddl-auto: update`.
    *   Console logging only.
*   **`prod`**:
    *   `ddl-auto: update` (Migrations via Flyway/Liquibase recommended for complex schemas).
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

### 4. Database Connection Failed
*   **Cause**: Wrong Supabase connection format.
*   **Checklist**:
    -   ✅ Using port `6543` (pooled connection)?
    -   ✅ Username is `postgres.[project-ref]` (not just `postgres`)?
    -   ✅ `?sslmode=require` appended to URL?
    -   ✅ Password is URL-encoded if it contains special characters?

### 5. Slow First Request (Render Only)
*   **Cause**: Render free tier spins down after 15 min inactivity.
*   **Note**: This is a Render limitation, NOT Supabase. The database is always ready.
*   **Fix**: Use UptimeRobot to ping `/api/health` every 5 minutes.

---

## 📈 Monitoring & Logging

*   **Backend Logs**: Render Dashboard -> Logs (or stdout on local).
*   **Database Metrics**: Supabase Dashboard -> Reports -> Database.
*   **Health Endpoints**:
    -   `/api/health` - App liveness (no DB interaction).
    -   `/api/health/db` - DB connectivity (returns PostgreSQL version).
*   **Audit**: Directly query the `audit_log` table for sensitive user activities.

---

## 🔄 Recovery & Restart

1.  **Stop**: `kill -15 [PID]` (allows graceful shutdown of Spring context).
2.  **Backup**: 
    ```bash
    pg_dump "postgresql://postgres.[ref]:[pass]@db.[ref].supabase.co:5432/postgres" > backup.sql
    ```
3.  **Restore**:
    ```bash
    psql "postgresql://postgres.[ref]:[pass]@db.[ref].supabase.co:5432/postgres" < backup.sql
    ```
4.  **Start**: Database is always reachable with Supabase. Start the JAR immediately.

---

## 🔐 Security Best Practices

### Supabase-Specific
-   **Row Level Security (RLS)**: Enable if you ever add direct Supabase client access from frontend.
-   **Connection Limits**: Free tier has connection limits; use pooled connections only.
-   **Password Rotation**: Rotate database password via Supabase Dashboard periodically.

### General
-   **JWT Secret**: Must be 32+ characters. Rotate every 90 days in production.
-   **CORS**: Restrict to known frontend domains; avoid `*` in production.
-   **HTTPS Only**: Never allow HTTP connections to backend API.
