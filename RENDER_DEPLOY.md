# 🚀 Deploying Mtng on Render.com

## Prerequisites
- A [Render.com](https://render.com) account (free tier works)
- Your code pushed to a **GitHub** or **GitLab** repository

---

## Option A: Blueprint Deploy (Recommended – One-Click)

1. **Push all files** to your repository (including `render.yaml` and `Dockerfile`).
2. Go to [Render Dashboard](https://dashboard.render.com).
3. Click **New → Blueprint**.
4. Select your repository.
5. Render detects `render.yaml` and creates the service automatically.
6. Click **Apply** → deployment starts.

> The `render.yaml` sets `SPRING_PROFILES_ACTIVE=render` automatically,
> which disables SSL and binds to Render's `PORT`.

---

## Option B: Manual Docker Deploy

1. Go to [Render Dashboard](https://dashboard.render.com).
2. Click **New → Web Service**.
3. Connect your GitHub/GitLab repo.
4. Configure:

   | Setting              | Value                              |
   |----------------------|------------------------------------|
   | **Name**             | `mtng-app`                         |
   | **Region**           | Oregon (or closest to you)         |
   | **Runtime**          | **Docker**                         |
   | **Dockerfile Path**  | `./Dockerfile`                     |
   | **Docker Context**   | `.`                                |
   | **Plan**             | Free                               |

5. Add **Environment Variables**:

   | Key                        | Value                                                        |
   |----------------------------|--------------------------------------------------------------|
   | `SPRING_PROFILES_ACTIVE`   | `render`                                                     |
   | `JAVA_TOOL_OPTIONS`        | `-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0`         |

6. Click **Create Web Service** → deployment starts.

---

## Option C: Native Runtime (No Docker)

If you prefer not to use Docker:

1. Go to **New → Web Service** → connect repo.
2. Configure:

   | Setting           | Value                                                                                                                                    |
   |-------------------|------------------------------------------------------------------------------------------------------------------------------------------|
   | **Runtime**       | **Java**                                                                                                                                 |
   | **Build Command** | `chmod +x render-build.sh && ./render-build.sh`                                                                                          |
   | **Start Command** | `java -XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -Djava.security.egd=file:/dev/./urandom -jar target/Mtng-0.0.1-SNAPSHOT.jar`    |
   | **Plan**          | Free                                                                                                                                     |

3. Add **Environment Variables** (same as Option B above).
4. Click **Create Web Service**.

---

## What the `render` Profile Changes

| Feature                | Local (default)                          | Render (`render` profile)                 |
|------------------------|------------------------------------------|-------------------------------------------|
| **Port**               | 8443 (HTTPS)                             | `$PORT` (set by Render, usually 10000)    |
| **SSL/TLS**            | Self-signed PKCS12 cert                  | Disabled (Render provides edge TLS)       |
| **HTTP→HTTPS redirect**| App-level redirect via Spring Security   | Disabled (Render handles this)            |
| **H2 Console**         | Enabled at `/h2-console`                 | Disabled (security)                       |
| **File Logging**       | `app.log`                                | Disabled (Render captures stdout)         |
| **Thymeleaf Cache**    | Off (dev reload)                         | On (performance)                          |

---

## Troubleshooting Common Failures

### ❌ Build fails: "mvnw: Permission denied"
**Cause:** The `mvnw` script isn't executable on Linux.  
**Fix:** The `Dockerfile` handles this. If using native runtime, the `render-build.sh` script runs `chmod +x mvnw`.

### ❌ Build fails: "Cannot resolve frontend-maven-plugin"
**Cause:** Network issue downloading Node.js during build.  
**Fix:** The `Dockerfile` uses `maven:3.9-eclipse-temurin-17` which has reliable network access. Retry the build.

### ❌ App starts but returns 502 Bad Gateway
**Cause:** The app is listening on the wrong port, or SSL is still enabled.  
**Fix:** Ensure `SPRING_PROFILES_ACTIVE=render` is set. This disables SSL and uses `$PORT`.

### ❌ Health check fails (deploy marked as failed)
**Cause:** The app takes too long to start on the free tier.  
**Fix:** The `render.yaml` sets `healthCheckPath: /login`. Free tier has limited CPU; first deploy may take 2-3 minutes. The Dockerfile `HEALTHCHECK` has a 90-second start period.

### ❌ "Request method 'POST' not supported" on login
**Cause:** HTTPS redirect loop. The app forces HTTPS but Render sends HTTP internally.  
**Fix:** The `render` profile disables `requiresSecure()` in Spring Security.

### ❌ WebSocket connection fails
**Cause:** Render supports WebSocket on paid plans. Free tier has limited support.  
**Fix:** The app uses SockJS fallback (`/ws` endpoint) which works over HTTP long-polling.

### ❌ H2 data lost on redeploy
**Expected:** H2 is in-memory; data resets every time the service restarts. For persistent data on Render, migrate to PostgreSQL (Render offers free PostgreSQL).

---

## Login Credentials (Seeded on Startup)

| Role    | Username | Password   |
|---------|----------|------------|
| Admin   | `admin`  | `admin123` |
| User    | `user`   | `user123`  |

---

## Files Created for Render Deployment

| File                                           | Purpose                                         |
|------------------------------------------------|-------------------------------------------------|
| `Dockerfile`                                   | Multi-stage Docker build (Maven + JRE Alpine)   |
| `.dockerignore`                                | Excludes unnecessary files from Docker context   |
| `render.yaml`                                  | Render Blueprint (one-click infrastructure)      |
| `render-build.sh`                              | Build script for native (non-Docker) deployment  |
| `src/main/resources/application-render.properties` | Spring profile: no SSL, uses PORT env-var    |

---

## Post-Deploy Checklist

- [ ] App is accessible at `https://your-app.onrender.com`
- [ ] Login with `admin` / `admin123` works
- [ ] Student list loads
- [ ] Meeting room page loads (WebSocket may need paid plan for full functionality)
- [ ] WhatsApp sharing uses the Render URL (auto-detected from request)

