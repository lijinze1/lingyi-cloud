import { getToken } from "./auth";

const BASE_URL = import.meta.env.VITE_API_BASE_URL || "http://localhost:18080";

function buildQuery(params) {
  const search = new URLSearchParams();
  Object.entries(params || {}).forEach(([key, value]) => {
    if (value === undefined || value === null || value === "") return;
    search.append(key, value);
  });
  const query = search.toString();
  return query ? `?${query}` : "";
}

export async function request(path, options = {}) {
  const isFormData = typeof FormData !== "undefined" && options.body instanceof FormData;
  const headers = { ...(options.headers || {}) };
  if (!isFormData) {
    headers["Content-Type"] = headers["Content-Type"] || "application/json";
  }
  const token = getToken();
  if (token) {
    headers.Authorization = `Bearer ${token}`;
  }

  const response = await fetch(`${BASE_URL}${path}${buildQuery(options.params)}`, {
    ...options,
    body: isFormData
      ? options.body
      : options.body != null && typeof options.body !== "string"
        ? JSON.stringify(options.body)
        : options.body,
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
