# Production Deployment Guide (Free Tier)

**Version**: 4.0 (Supabase Migration)
**Status**: Verified for Render + Supabase + GitHub Pages

This guide provides a strictly chronological, step-by-step walkthrough to deploying the Student Notes Platform. It is engineered to respect the constraints of free-tier infrastructure (Render 512MB RAM, Supabase PostgreSQL 500MB).

---

## Phase 1: Database Setup (Supabase)

We start here because the Backend needs database credentials to launch.

### Why Supabase over Neon?

| Feature | Neon (Free) | Supabase (Free) |
|---------|-------------|-----------------|
| **Cold Starts** | Yes (5 min idle = 3-5s wake) | **No** (always-on compute) |
| **Compute Hours** | 100 hrs/month limit | **Unlimited** |
| **Storage** | 0.5 GB | **500 MB** |
| **File Storage** | ❌ Not included | **1 GB** (optional bonus) |
| **API Requests** | Standard | **Unlimited** |
| **Connection Pooling** | PgBouncer | **Supavisor** (built-in) |

**Bottom Line**: Supabase eliminates the cold start latency penalty, providing a consistently snappy user experience without compute-hour budgeting.

---

### Step-by-Step Setup

1.  **Create Account & Project**
    -   Go to [Supabase Dashboard](https://supabase.com/dashboard).
    -   Click **New Project**.
    -   **Organization**: Create one if needed (free).
    -   **Project Name**: `student-notes`
    -   **Database Password**: Generate a strong password (save this immediately!).
    -   **Region**: Select the closest to your users (e.g., Singapore, US East).
    -   Click **Create new project** and wait ~2 minutes for provisioning.

2.  **Get Connection String**
    -   Navigate to **Project Settings** (gear icon) → **Database**.
    -   Scroll to **Connection string** section.
    -   Select **URI** tab.
    -   Copy the connection string. It looks like:
        ```
        postgresql://postgres.[project-ref]:[password]@aws-0-[region].pooler.supabase.com:6543/postgres
        ```
    -   **Important**: This is the **pooled connection** (port 6543). Use this for Spring Boot.
    -   For direct connections (migrations, manual SQL), use port `5432`.

3.  **Convert to JDBC Format**
    For Spring Boot, convert the URI to JDBC format:
    ```
    jdbc:postgresql://aws-0-[region].pooler.supabase.com:6543/postgres?user=postgres.[project-ref]&password=[your-password]&sslmode=require
    ```
    
    Or use separate username/password environment variables (recommended):
    ```
    DB_URL=jdbc:postgresql://aws-0-[region].pooler.supabase.com:6543/postgres?sslmode=require
    DB_USERNAME=postgres.[project-ref]
    DB_PASSWORD=[your-password]
    ```

4.  **Understand the Advantages**
    -   **No Cold Starts**: Unlike Neon, Supabase keeps your database always available.
    -   **No Compute Limits**: You won't run out of free hours mid-month.
    -   **Built-in Pooler**: Supavisor handles connection pooling automatically.
    -   **File Storage (Bonus)**: 1 GB S3-compatible storage if you ever need document uploads.

---

## Phase 2: Backend Deployment (Render)

Render hosts the Spring Boot application. We must tune the JVM to fit inside their free container.

1.  **Create Service**
    -   Go to [Render Dashboard](https://dashboard.render.com/).
    -   Click **New +** → **Web Service**.
    -   Connect your GitHub repository.

2.  **Configure Build & Runtime**
    -   **Name**: `student-notes-api`
    -   **Region**: Same as Database (minimize latency).
    -   **Root Directory**: `Backend` (Crucial!).
    -   **Runtime**: `Java` (OpenJDK 21).
    -   **Build Command**:
        ```bash
        mvn clean package -DskipTests
        ```
    -   **Start Command**:
        ```bash
        java -Xms128m -Xmx384m -jar target/*.jar
        ```
        -   *-Xms128m*: Start with 128MB RAM.
        -   *-Xmx384m*: Max out at 384MB. This leaves ~128MB for the OS/Container overhead within Render's 512MB limit, preventing crashes.

3.  **Set Environment Variables**
    Scroll down to **Environment Variables** and add:

    | Key | Value | Notes |
    |---|---|---|
    | `SPRING_PROFILES_ACTIVE` | `prod` | Loads `application-prod.properties` |
    | `DB_URL` | `jdbc:postgresql://aws-0-[region].pooler.supabase.com:6543/postgres?sslmode=require` | From Supabase. Port 6543 = pooled connection. |
    | `DB_USERNAME` | `postgres.[project-ref]` | From Supabase Connection String |
    | `DB_PASSWORD` | *(Your Password)* | The password you set during project creation |
    | `JWT_SECRET` | *(Random String)* | Generate 32+ chars (e.g., `openssl rand -hex 32`) |

    > **Note on Port**: Render injects a `PORT` variable (usually 10000). Our app uses `server.port=${PORT:8080}` to bind to it automatically.

4.  **Deploy**
    -   Click **Create Web Service**.
    -   Wait 3-5 minutes.
    -   **Success**: You should see "Your service is live".
    -   Copy your URL: `https://student-notes-api.onrender.com`

---

## Phase 3: Frontend Deployment (GitHub Pages)

The frontend must know where the backend lives.

1.  **Configure Environment**
    -   Locally, create or edit `Frontend/.env.production`.
    -   Add your Render URL:
        ```bash
        VITE_API_URL=https://student-notes-api.onrender.com/api
        ```
    -   *Ensure NO trailing slash after `/api` unless your code expects it.*

2.  **Build & Deploy (Manual Method)**
    -   Open terminal in `Frontend/`:
        ```bash
        npm ci
        npm run build
        ```
    -   Deploy using `gh-pages` (if installed):
        ```bash
        npx gh-pages -d dist
        ```
    -   *Alternatively, commit and push to main if you set up the GitHub Actions workflow.*

3.  **Verify**
    -   Visit your GitHub Pages URL (e.g., `https://yourname.github.io/exam-notes`).
    -   Open Console. If you see CORS errors, check Phase 4 troubleshooting.

---

## Phase 4: Keeping it Alive (Monitoring)

Render spins down the web service after 15 minutes of inactivity. We need to prevent this.

> **Good News**: With Supabase, you no longer need to worry about database cold starts! Only the Render web service needs a keep-alive ping.

1.  **Setup UptimeRobot**
    -   Go to [UptimeRobot](https://uptimerobot.com/).
    -   **Add New Monitor**.
    -   **Type**: HTTP(s).
    -   **Friendly Name**: `Student Notes API`.
    -   **URL**: `https://student-notes-api.onrender.com/api/health`
    -   **Interval**: **5 Minutes**.

2.  **The Strategy (Split Health Check)**
    -   We ping `/api/health` (Static OK).
        -   ✅ Keeps Render Web Service active/warm.
        -   ✅ Supabase DB is always available (no wake-up needed).
    -   Use `/api/health/db` for **manual debugging** only.

---

## Phase 5: Final Verification

Run these checks to ensure the system is Production-Correct.

### 1. Check Backend Health
Visit: `https://student-notes-api.onrender.com/api/health`
-   **Expect**: `200 OK` (Immediate).
-   *If this fails, Render is crashing. Check Logs.*

### 2. Check Database Connection
Visit: `https://student-notes-api.onrender.com/api/health/db`
-   **Expect**: `DB OK | PostgreSQL 15.x` (or higher).
-   **Response Time**: Should be instant (no cold start with Supabase!).

### 3. Check Architecture Limits
-   **Storage**: Monitor Supabase Dashboard for usage (500 MB limit).
-   **Memory**: Check Render Metrics. RAM usage should be stable around 200-300MB.

---

## Troubleshooting

| Symptom | Cause | Solution |
|---------|-------|----------|
| **502 Bad Gateway** | Java app hasn't started yet | Wait 30s, then check Render logs |
| **Network Error (Frontend)** | CORS blocked or mixed content | Ensure Backend allows Frontend domain in CORS config |
| **Connection refused** | Wrong port or host | Use port `6543` (pooled) in DB_URL |
| **SSL handshake failed** | Missing `sslmode=require` | Add `?sslmode=require` to DB_URL |
| **Password auth failed** | Wrong username format | Username must be `postgres.[project-ref]`, not just `postgres` |
| **Connection timeout** | Region mismatch | Deploy Render in same region as Supabase |

---

## Quick Reference: Environment Variables

### Backend (Render)
```bash
SPRING_PROFILES_ACTIVE=prod
DB_URL=jdbc:postgresql://aws-0-[region].pooler.supabase.com:6543/postgres?sslmode=require
DB_USERNAME=postgres.[project-ref]
DB_PASSWORD=[your-supabase-password]
JWT_SECRET=[32+ character random string]
```

### Frontend (GitHub Pages)
```bash
VITE_API_URL=https://student-notes-api.onrender.com/api
```

---

## Migration from Neon (If Applicable)

If you're migrating an existing Neon database:

1.  **Export from Neon**
    ```bash
    pg_dump "postgres://user:pass@ep-xyz.neon.tech/neondb?sslmode=require" > backup.sql
    ```

2.  **Create Tables in Supabase**
    -   Go to Supabase Dashboard → SQL Editor.
    -   Run your schema creation scripts first.

3.  **Import Data**
    ```bash
    psql "postgresql://postgres.[ref]:[pass]@db.[ref].supabase.co:5432/postgres" < backup.sql
    ```

4.  **Update Environment Variables**
    -   Replace `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` in Render.
    -   Trigger a manual deploy.

---

## Supabase Bonus Features (Optional)

### File Storage
Supabase offers 1 GB of S3-compatible storage. Useful if you want to:
-   Store PDFs or images uploaded by teachers.
-   Serve static assets through a CDN.

### Edge Functions
Serverless TypeScript/JavaScript functions if you ever need lightweight API logic outside Spring Boot.

### Row Level Security (RLS)
If you ever want to add direct Supabase client access from the frontend, enable RLS policies for fine-grained access control.

---

**Congratulations!** Your Student Notes Platform is now running on a modern, always-available Supabase backend with no cold start penalties. 🚀
