# CI/CD Pipeline Architecture (Free Tier)

This project uses a MAANG-standard automated pipeline to ensure code quality and reliable deployments. We respect the "Free Tier" constraints while maintaining strict engineering rigor.

## 1. Philosophy: Validated Deployments Only
- **Zero-Touch Prod**: No developer deploys from their laptop.
- **Test Gating**: If unit tests fail, deployment is strictly blocked.
- **Workflow Separation**: CI (Integration) and CD (Delivery) are decoupled.

## 2. Frontend Pipeline (GitHub Pages)
**File**: `.github/workflows/frontend-ci.yml` & `frontend-cd.yml`

### CI Phase (Pull Request / Push)
1.  **Checkout**: Fetches code.
2.  **Lint**: Runs `eslint` to enforce code style.
3.  **Test**: Runs `vitest` to verify logic components.
4.  **Build**: Compiles React to static assets (`dist/`).
5.  **Artifact**: Uploads the `dist/` folder for use in the CD phase.

### CD Phase (Success Only)
*Triggered ONLY if CI passes on `main`.*
1.  **Download**: Retrieves the `dist/` artifact (no rebuild needed, saving CPU).
2.  **Deploy**: Pushes the static assests to the `gh-pages` branch.

## 3. Backend Pipeline (Render)
**File**: `.github/workflows/backend-ci.yml` & `backend-cd.yml`

### CI Phase (Pull Request / Push)
1.  **Checkout**: Fetches code.
2.  **Setup**: Java 21 (Temurin).
3.  **Test**: Runs `mvn test` using **H2 Database** (In-Memory).
    -   *Why H2?* GitHub Actions doesn't have access to the production Supabase DB. H2 mocks the DB integration, proving JPA mappings work without network calls.
4.  **Verify**: Runs `mvn verify` to package the JAR.

### CD Phase (Success Only)
*Triggered ONLY if CI passes on `main`.*
1.  **Trigger**: Calls Render's **Deploy Hook URL** via `curl`.
2.  **Render**: Pulls the verified commit, builds the Docker container (or JAR), and deploys.

## 4. Setup Instructions

### frontend-cd.yml
This workflow requires `GITHUB_TOKEN` (built-in). You do not need to set anything manually.

### backend-cd.yml
1.  Go to your Render Dashboard -> Settings -> **Deploy Hook**.
2.  Copy the Deploy Hook URL.
3.  Go to GitHub Repository -> Settings -> Secrets and variables -> Actions.
4.  Create a New Repository Secret:
    -   Name: `RENDER_DEPLOY_HOOK`
    -   Value: *(Paste URL)*

## 5. Free-Tier Constraints
-   **Parallelism**: GitHub Actions free tier allows 20 concurrent jobs. Our split pipelines fit well within this.
-   **Storage**: Artifacts are retained for 1 day to save storage limits.
-   **Compute**: We avoid Docker-in-Docker builds on GitHub, deferring the heavy container build to Render (where it's also free).
