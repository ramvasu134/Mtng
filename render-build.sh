#!/usr/bin/env bash
# ═══════════════════════════════════════════════════════════════════════════════
# render-build.sh – Build script for Render.com (Native Runtime, no Docker)
# ═══════════════════════════════════════════════════════════════════════════════
# Use this if you choose Render's "Native" runtime instead of Docker.
#
# Render settings:
#   Build Command  : chmod +x render-build.sh && ./render-build.sh
#   Start Command  : java -XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -Djava.security.egd=file:/dev/./urandom -jar target/Mtng-0.0.1-SNAPSHOT.jar
#   Environment    : Java 17  (select in Render dashboard)
#   Env Vars       : SPRING_PROFILES_ACTIVE=render
# ═══════════════════════════════════════════════════════════════════════════════

set -euo pipefail

echo "══════════════════════════════════════════════════════"
echo " Mtng – Render Build"
echo "══════════════════════════════════════════════════════"

# ── Step 1: Install Maven if not present ─────────────────────────────────────
if ! command -v mvn &>/dev/null; then
  echo "→ Maven not found, installing via wrapper..."
  chmod +x mvnw
  MVN="./mvnw"
else
  echo "→ Maven found: $(mvn --version | head -1)"
  MVN="mvn"
fi

# ── Step 2: Build the application ────────────────────────────────────────────
echo "→ Building application (frontend + backend)..."
$MVN clean package -DskipTests -B

echo ""
echo "✅  Build complete!  JAR: target/Mtng-0.0.1-SNAPSHOT.jar"
echo "══════════════════════════════════════════════════════"

