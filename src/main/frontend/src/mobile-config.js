/**
 * mobile-config.js
 * Configuration for the mobile (Capacitor) build.
 *
 * When running as a mobile app, the app is NOT same-origin with your server.
 * It needs to know the server URL to connect to.
 *
 * HOW TO USE:
 * Before building the APK, change SERVER_URL below to your actual server's
 * IP address on your local network (or your public domain).
 *
 * To find your PC's local IP:
 *   1. Open Command Prompt
 *   2. Type: ipconfig
 *   3. Look for "IPv4 Address" under your Wi-Fi/Ethernet adapter
 *   4. It will be something like 192.168.1.100
 *
 * Then set: SERVER_URL = 'https://192.168.1.100:8443'
 */

// ⚠️ CHANGE THIS to your server's IP address before building!
const SERVER_URL = 'https://192.168.29.129:8443';

// Detect if running inside Capacitor (mobile) or browser (web)
const isCapacitor = typeof window !== 'undefined' && window.Capacitor !== undefined;

/**
 * Returns the base URL for API calls.
 * - In browser (web): empty string (same-origin)
 * - In mobile app: the configured SERVER_URL
 */
export function getBaseUrl() {
  if (isCapacitor) {
    return SERVER_URL;
  }
  return '';  // same-origin for web
}

export function getWsUrl() {
  if (isCapacitor) {
    return SERVER_URL + '/ws';
  }
  return '/ws';  // same-origin for web
}

export default { getBaseUrl, getWsUrl, SERVER_URL, isCapacitor };

