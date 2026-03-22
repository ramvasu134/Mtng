# 🚀 Deploying Mtng on Your Cloudflare Domain — Step-by-Step Guide

This guide walks you through deploying the **Mtng** Spring Boot application on a
Virtual Private Server (VPS) and pointing your **Cloudflare-managed domain** to it.

> **How it works:**  
> `User's browser → Cloudflare CDN (HTTPS, DDoS protection) → Your VPS (Nginx → Spring Boot)`

---

## 📋 What You Need Before Starting

| Item | Details |
|------|---------|
| A domain | Managed by Cloudflare (e.g. `yourdomain.com` or `meet.yourdomain.com`) |
| A VPS | Ubuntu 22.04 LTS recommended (DigitalOcean, Vultr, Linode, Hetzner, AWS EC2, etc.) — minimum **1 vCPU / 1 GB RAM** |
| SSH access | You can `ssh root@YOUR_VPS_IP` into the server |
| Java knowledge | Basic comfort with the command line |

---

## 📌 Overview of Steps

```
Step 1 → Prepare your VPS (install Java, create user, open firewall)
Step 2 → Set up PostgreSQL (production database)
Step 3 → Build the JAR on your local machine
Step 4 → Upload the JAR to the VPS
Step 5 → Create a systemd service so the app auto-starts
Step 6 → Install Nginx as a reverse proxy
Step 7 → Get a free SSL certificate with Certbot (Let's Encrypt)
Step 8 → Point your Cloudflare DNS to the VPS
Step 9 → Set Cloudflare SSL mode to "Full (Strict)"
Step 10 → Test everything end-to-end ✅
```

---

## Step 1 — Prepare Your VPS

SSH into your server as `root`:

```bash
ssh root@YOUR_VPS_IP
```

### 1a. Update the system

```bash
apt update && apt upgrade -y
```

### 1b. Install Java 17

```bash
apt install -y openjdk-17-jre-headless
java -version
# Should print: openjdk version "17.x.x"
```

### 1c. Create a dedicated non-root user for the app

```bash
useradd -m -s /bin/bash mtng
mkdir -p /opt/mtng/logs
chown -R mtng:mtng /opt/mtng
```

### 1d. Open the firewall

Allow SSH, HTTP (80), and HTTPS (443). **Do NOT expose port 8080 publicly** — Nginx handles that internally.

```bash
ufw allow OpenSSH
ufw allow 80/tcp
ufw allow 443/tcp
ufw enable
ufw status
```

---

## Step 2 — Set Up PostgreSQL (Production Database)

The app uses an **in-memory H2 database** by default. For production you need a
persistent PostgreSQL database so your data survives restarts.

### 2a. Install PostgreSQL

```bash
apt install -y postgresql postgresql-contrib
systemctl enable postgresql
systemctl start postgresql
```

### 2b. Create the database and user

```bash
sudo -u postgres psql
```

Inside the PostgreSQL shell:

```sql
CREATE DATABASE mtngdb;
CREATE USER mtng_user WITH ENCRYPTED PASSWORD 'YourStrongPassword123!';
GRANT ALL PRIVILEGES ON DATABASE mtngdb TO mtng_user;
\q
```

> ⚠️ **Write down your password** — you'll need it in the next steps.

---

## Step 3 — Build the JAR on Your Local Machine

On your **local machine** (Windows/macOS/Linux), open a terminal in the project folder:

```bash
# Windows
mvnw.cmd clean package -DskipTests

# macOS / Linux
./mvnw clean package -DskipTests
```

This creates:

```
target/Mtng-0.0.1-SNAPSHOT.jar
```

---

## Step 4 — Upload the JAR to the VPS

```bash
# On your local machine:
scp target/Mtng-0.0.1-SNAPSHOT.jar root@YOUR_VPS_IP:/opt/mtng/
```

Set ownership:

```bash
# On the VPS:
chown mtng:mtng /opt/mtng/Mtng-0.0.1-SNAPSHOT.jar
```

---

## Step 5 — Create a Systemd Service (Auto-Start on Reboot)

### 5a. Copy the service file

The file `deploy/mtng.service` is included in this repository. Upload it to the server:

```bash
# On your local machine:
scp deploy/mtng.service root@YOUR_VPS_IP:/etc/systemd/system/mtng.service
```

### 5b. Create the environment file with your database credentials

```bash
# On the VPS:
mkdir -p /etc/mtng
cat > /etc/mtng/env <<'EOF'
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/mtngdb
SPRING_DATASOURCE_USERNAME=mtng_user
SPRING_DATASOURCE_PASSWORD=YourStrongPassword123!
EOF

# Restrict read access to root only (keeps your password out of process listings)
chmod 600 /etc/mtng/env
```

### 5c. Enable and start the service

```bash
systemctl daemon-reload
systemctl enable mtng
systemctl start mtng
```

### 5d. Verify the app is running

```bash
systemctl status mtng
# Should say: Active: active (running)

# Tail the logs:
journalctl -u mtng -f
# Or:
tail -f /opt/mtng/logs/app.log
```

Wait until you see a line like:

```
Tomcat started on port 8080 (http) with context path '/'
```

Then test it locally on the VPS:

```bash
curl http://localhost:8080/login
# Should return HTML with the login form
```

---

## Step 6 — Install Nginx (Reverse Proxy)

Nginx sits between the internet and Spring Boot. It handles HTTPS and forwards
requests to `localhost:8080`.

### 6a. Install Nginx

```bash
apt install -y nginx
systemctl enable nginx
systemctl start nginx
```

### 6b. Copy the Nginx config

The file `deploy/nginx/mtng.conf` is included in this repository. Upload it:

```bash
# On your local machine:
scp deploy/nginx/mtng.conf root@YOUR_VPS_IP:/etc/nginx/sites-available/mtng
```

### 6c. Edit the config — replace `YOURDOMAIN.COM`

```bash
# On the VPS:
nano /etc/nginx/sites-available/mtng
```

Replace every occurrence of `YOURDOMAIN.COM` with your real domain, e.g. `meet.example.com`.

> **Note:** Leave SSL config commented out for now — we'll get the certificate in Step 7.

For Step 6 only, temporarily use this minimal config to test HTTP first:

```nginx
server {
    listen 80;
    server_name meet.example.com;  # ← your real domain
    location / {
        proxy_pass http://127.0.0.1:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
}
```

### 6d. Enable the site and reload Nginx

```bash
ln -s /etc/nginx/sites-available/mtng /etc/nginx/sites-enabled/mtng
nginx -t               # Test config syntax
systemctl reload nginx
```

---

## Step 7 — Get a Free SSL Certificate (Let's Encrypt + Certbot)

### 7a. Install Certbot

```bash
apt install -y certbot python3-certbot-nginx
```

### 7b. Issue the certificate

```bash
certbot --nginx -d meet.example.com
```

Certbot will:
1. Ask for your email address (for renewal reminders)
2. Ask you to agree to the Terms of Service
3. Automatically update your Nginx config with the SSL certificate paths
4. Set up automatic renewal (via a cron/systemd timer)

### 7c. Verify renewal works

```bash
certbot renew --dry-run
# Should print: "Congratulations, all simulated renewals succeeded"
```

### 7d. Replace the full Nginx config

Now replace the temporary config with the full `deploy/nginx/mtng.conf` (make sure
`YOURDOMAIN.COM` is replaced with your domain and the certificate paths match what
Certbot created):

```bash
nginx -t && systemctl reload nginx
```

---

## Step 8 — Point Your Cloudflare DNS to the VPS

1. Log in to [Cloudflare Dashboard](https://dash.cloudflare.com)
2. Select your domain
3. Click **DNS → Records**
4. Click **Add Record** and enter:

   | Type | Name | IPv4 address | Proxy status |
   |------|------|-------------|--------------|
   | `A`  | `meet` (or `@` for root) | `YOUR_VPS_IP` | **Proxied** (orange cloud ☁️) |

   > If you want `meet.yourdomain.com`, set **Name** to `meet`.  
   > If you want `yourdomain.com` itself, set **Name** to `@`.

5. Click **Save**

DNS changes propagate within a few minutes when proxied through Cloudflare.

---

## Step 9 — Configure Cloudflare SSL Mode

Because Cloudflare sits between the browser and your server, you need to tell it
how to handle the Cloudflare → VPS connection.

1. In Cloudflare Dashboard → **SSL/TLS → Overview**
2. Set the encryption mode to **Full (Strict)**

   | Mode | Meaning |
   |------|---------|
   | Off | No HTTPS anywhere (not recommended) |
   | Flexible | HTTPS only browser → Cloudflare; plain HTTP to your server (risky) |
   | Full | HTTPS to your server but doesn't validate the certificate |
   | **Full (Strict)** ✅ | HTTPS to your server **and** validates the cert (use this) |

3. In **SSL/TLS → Edge Certificates**, enable:
   - ✅ **Always Use HTTPS** — redirects all HTTP to HTTPS automatically
   - ✅ **Automatic HTTPS Rewrites** — fixes mixed-content issues
   - ✅ **HSTS** (optional, but recommended for security)

---

## Step 10 — Test Everything End-to-End ✅

Open your browser and visit:

```
https://meet.yourdomain.com/login
```

You should see the **Mtng login page** served over HTTPS with Cloudflare's CDN.

### Quick checklist

| Check | Command / Action |
|-------|-----------------|
| App running? | `systemctl status mtng` |
| Nginx OK? | `nginx -t && systemctl status nginx` |
| Port 8080 NOT public? | `curl http://YOUR_VPS_IP:8080` should time out from outside |
| HTTPS works? | Visit `https://yourdomain.com` in browser |
| Login works? | Use `admin / admin123` |
| Database persists? | Restart app with `systemctl restart mtng`, data still there |

---

## 🐳 Alternative: Docker Compose Deployment

If you prefer Docker (no need to install Java or PostgreSQL separately):

### On the VPS

```bash
# Install Docker
curl -fsSL https://get.docker.com | sh
apt install -y docker-compose-plugin

# Upload project files
# (On your local machine)
scp -r . root@YOUR_VPS_IP:/opt/mtng-src/

# On the VPS
cd /opt/mtng-src
DB_PASSWORD=YourStrongPassword123! docker compose up -d --build
```

The app will be reachable at `http://YOUR_VPS_IP:8080`.  
Then follow Steps 6–9 above to add Nginx + SSL + Cloudflare DNS.

---

## ⚙️ Environment Variables Reference

| Variable | Default | Description |
|----------|---------|-------------|
| `SPRING_PROFILES_ACTIVE` | `prod` | Activates `application-prod.properties` |
| `SPRING_DATASOURCE_URL` | `jdbc:postgresql://localhost:5432/mtngdb` | PostgreSQL JDBC URL |
| `SPRING_DATASOURCE_USERNAME` | `mtng_user` | Database username |
| `SPRING_DATASOURCE_PASSWORD` | `changeme` | Database password — **change this!** |

---

## 🔒 Security Checklist for Production

- [ ] Change all default passwords (`admin123`, `user123`, `pass1234`) after first login
- [ ] Use a **strong** PostgreSQL password
- [ ] Never expose port `8080` directly to the internet
- [ ] Keep your VPS updated: `apt update && apt upgrade`
- [ ] Enable **UFW firewall** (only 22, 80, 443 open)
- [ ] Enable **Cloudflare Bot Fight Mode** (Security → Bots)
- [ ] Set a **Rate Limiting** rule in Cloudflare to protect `/login`
- [ ] Rotate the `app.log` file regularly (already configured in `application-prod.properties`)
- [ ] Back up your PostgreSQL database regularly:
  ```bash
  pg_dump -U mtng_user mtngdb > /opt/mtng/backup_$(date +%F).sql
  ```

---

## 🛠️ Troubleshooting

### App won't start
```bash
journalctl -u mtng -n 100 --no-pager
# Look for "APPLICATION FAILED TO START"
```
Common causes: wrong database password, PostgreSQL not running, port 8080 already in use.

### Nginx 502 Bad Gateway
The Spring Boot app isn't running. Check:
```bash
curl http://localhost:8080/login   # Should return HTML
systemctl status mtng
```

### Cloudflare shows "Error 521"
Your origin server (VPS) is refusing the connection. Check:
```bash
systemctl status nginx
ufw status  # Make sure port 80/443 are open
```

### Database lost on restart
Make sure you're using the **prod** profile (`-Dspring.profiles.active=prod`), which
sets `spring.jpa.hibernate.ddl-auto=update` instead of `create-drop`.

---

## 📁 Files Added by This Guide

```
Mtng/
├── Dockerfile                          # Container image definition
├── docker-compose.yml                  # App + PostgreSQL stack
├── deploy/
│   ├── mtng.service                    # systemd service file
│   └── nginx/
│       └── mtng.conf                   # Nginx reverse proxy config
└── src/main/resources/
    └── application-prod.properties     # Production Spring Boot config
```

---

**Happy deploying! 🎉**  
If you run into any issues, check `app.log` on the server first — it usually tells you exactly what went wrong.
