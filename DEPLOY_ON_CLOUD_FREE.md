# 🚀 Free Cloud Deployment Guide for MTNG Meeting App

This guide shows you how to deploy the **MTNG Meeting Platform** (Spring Boot + Java 17) on a
**free cloud server** — no credit card required — so anyone on the internet can access it.

---

## 📌 Recommended Platform: **Render.com**

| Feature | Details |
|---------|---------|
| **Free tier** | ✅ Yes – no credit card required |
| **Public URL** | ✅ `https://your-app-name.onrender.com` |
| **Supports Java / Docker** | ✅ Yes |
| **Auto-deploy from GitHub** | ✅ Yes |
| **Sleep on inactivity** | ⚠️ Free tier sleeps after 15 min of no traffic (wakes up in ~30 s on next request) |

> **Other free options** are listed at the bottom of this guide.

---

## 🛠️ Prerequisites

- A **GitHub account** (repository must be public or you must be on a plan that allows private repos)
- The MTNG repository pushed to GitHub (it already is – this is `ramvasu134/Mtng`)
- A **Render.com account** (free, sign up with GitHub – no credit card)

---

## 📋 Step-by-Step Deployment on Render.com

### Step 1 – Sign Up on Render

1. Go to **https://render.com**
2. Click **"Get Started for Free"**
3. Sign up with your **GitHub account** (one click, no credit card)

---

### Step 2 – Connect Your GitHub Repository

1. After sign-in, click **"New +"** → **"Web Service"**
2. Click **"Connect account"** under GitHub (first time only)
3. Search for **`Mtng`** and click **"Connect"**

---

### Step 3 – Configure the Web Service

Fill in the form exactly as shown below:

| Field | Value |
|-------|-------|
| **Name** | `mtng-meeting-app` (or any name you like) |
| **Region** | Oregon (US West) — free tier |
| **Branch** | `main` |
| **Runtime** | **Docker** |
| **Instance Type** | **Free** |

> Render will automatically detect the `Dockerfile` in the root of the repository.

Leave all other fields at their defaults, then click **"Create Web Service"**.

---

### Step 4 – Wait for the Build

- Render will clone your repo, build the Docker image, and start the container.
- The first build takes **3–5 minutes**.
- You can watch live logs in the **Logs** tab.
- When you see `Started MtngApplication`, the deployment is complete ✅

---

### Step 5 – Access Your Live App

Your app is now live at:

```
https://mtng-meeting-app.onrender.com
```

(Replace `mtng-meeting-app` with the name you chose in Step 3.)

Share this URL with anyone — they can open it in any browser worldwide.

---

### Step 6 – Login Credentials

| Role | Username | Password |
|------|----------|----------|
| Admin | `admin` | `admin123` |
| User | `user` | `user123` |
| Student | `HARI34` | `pass1234` |
| Student | `PRIYA01` | `pass1234` |
| Student | `RAM22` | `pass1234` |

---

### Step 7 – Auto-Deploy on Every Push

Every time you `git push` to the `main` branch, Render automatically rebuilds and redeploys
the app — **no manual action needed**.

---

## 🔄 Using the render.yaml Blueprint (Optional — Even Easier)

The repository already contains a `render.yaml` file. You can use it for one-click deployment:

1. Go to **https://dashboard.render.com/blueprints**
2. Click **"New Blueprint Instance"**
3. Connect your GitHub account and select the `Mtng` repo
4. Render reads `render.yaml` and creates the service automatically
5. Click **"Apply"** — done!

---

## 🐳 How the Dockerfile Works

The repository contains a multi-stage `Dockerfile`:

```
Stage 1 (build)  – eclipse-temurin:17-jdk-alpine
  └─ Runs: mvnw package -DskipTests
  └─ Output: target/Mtng-0.0.1-SNAPSHOT.jar

Stage 2 (runtime) – eclipse-temurin:17-jre-alpine (tiny image ~200 MB)
  └─ Copies only the JAR
  └─ Runs as a non-root user (security best practice)
  └─ Listens on the PORT injected by the cloud platform
```

The app uses an **H2 in-memory database**, so no external database setup is needed. Data
resets on every restart (perfect for demos).

---

## ⚠️ Important Notes for Free Tier

| Topic | Note |
|-------|------|
| **Sleep** | The free tier sleeps after 15 minutes of inactivity. The first request after sleep takes **30–60 seconds**. |
| **Data persistence** | H2 is in-memory — all data is lost when the container restarts or sleeps. |
| **Custom domain** | Not available on the free tier. You get a `.onrender.com` subdomain. |
| **HTTPS** | ✅ Automatically enabled (free SSL certificate). |
| **Uptime** | Free tier: 750 free instance hours/month (~31 days for one service). |

---

## 🌐 Alternative Free Clouds (No Credit Card)

### Option 2 – Railway.app

> ⚠️ **Note:** Railway now requires a credit card for verification after the free trial period. You still get **$5 free credit per month**, but payment info is required for ongoing use.

1. Go to **https://railway.app** → Sign up with GitHub
2. Click **"New Project"** → **"Deploy from GitHub Repo"**
3. Select the `Mtng` repo
4. Railway detects the `Dockerfile` automatically
5. Click **"Deploy"**
6. Your app gets a URL like `https://mtng.up.railway.app`

> Railway free trial: 500 hours, $5 credit. After that, a credit card is required.

---

### Option 3 – Koyeb

1. Go to **https://www.koyeb.com** → Sign up (free, no credit card for free tier)
2. Click **"Create App"** → **"GitHub"**
3. Select the `Mtng` repo
4. Set **Builder**: Docker
5. Set **Port**: `8080`
6. Click **"Deploy"**

> Koyeb free tier: 1 nano service (0.1 vCPU, 256 MB RAM), always-on.

---

### Option 4 – Fly.io (requires credit card for verification, but free)

> ⚠️ Fly.io requires a credit card for account verification (but does NOT charge for free tier usage).

1. Install flyctl: `curl -L https://fly.io/install.sh | sh`
2. Run: `fly auth signup`
3. In the repo root: `fly launch` (detects Dockerfile automatically)
4. `fly deploy`

---

## 🔧 Troubleshooting

### App shows 502 Bad Gateway
- Wait **30–60 seconds** — the container may still be starting.
- Check the **Logs** tab in the Render dashboard for errors.

### Build fails: "Permission denied ./mvnw"
- The `mvnw` script must be executable. Run this locally and push:
  ```bash
  git update-index --chmod=+x mvnw
  git commit -m "fix: make mvnw executable"
  git push
  ```

### App sleeps too quickly (free tier)
- Use a free uptime monitor like **UptimeRobot** (https://uptimerobot.com) to ping your
  app every 10 minutes and keep it awake.

### Data is lost after restart
- This is expected — the app uses an H2 in-memory database.
- For persistent data, switch to a free PostgreSQL database:
  1. On Render: create a **free PostgreSQL** database service
  2. Add the `DATABASE_URL` environment variable to your web service
  3. Update `application.properties` to use PostgreSQL

---

## 📞 Summary

| Step | Action |
|------|--------|
| 1 | Sign up at https://render.com with GitHub |
| 2 | New → Web Service → Connect `Mtng` repo |
| 3 | Runtime: Docker, Plan: Free → Create |
| 4 | Wait 3–5 min for build |
| 5 | Open `https://your-app.onrender.com` |
| 6 | Login with `admin / admin123` |
| 7 | Share the URL — anyone can access it! |

---

**That's it!** Your MTNG Meeting Platform is live on the internet for free. 🎉
