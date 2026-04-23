import { getToken } from "./auth";

const BASE_URL = import.meta.env.VITE_API_BASE_URL || "http://localhost:18080";

export async function request(path, options = {}) {
  const headers = {
    "Content-Type": "application/json",
    ...(options.headers || {})
  };

  const token = getToken();
  if (token) {
    headers.Authorization = `Bearer ${token}`;
  }

  const response = await fetch(`${BASE_URL}${path}`, {
    ...options,
    headers
  });

  const data = await response.json().catch(() => ({}));
  if (!response.ok || data.code !== "00000") {
    const message = data.message || `HTTP ${response.status}`;
    throw new Error(message);
  }
  return data.data;
}

export async function login(username, password) {
  return request("/api/user/auth/login", {
    method: "POST",
    body: JSON.stringify({ username, password })
  });
}

export async function register(username, password, nickname) {
  return request("/api/user/auth/register", {
    method: "POST",
    body: JSON.stringify({ username, password, nickname })
  });
}

export async function me() {
  return request("/api/user/auth/me", { method: "GET" });
}

export async function pingProduct() {
  return request("/api/product/ping", { method: "GET" });
}