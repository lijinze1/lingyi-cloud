import { getToken } from "./auth";

const BASE_URL = import.meta.env.VITE_API_BASE_URL || "http://localhost:18080";

function toQueryString(params = {}) {
  const query = new URLSearchParams();
  Object.entries(params).forEach(([key, value]) => {
    if (value === undefined || value === null || value === "") {
      return;
    }
    query.set(key, String(value));
  });
  const text = query.toString();
  return text ? `?${text}` : "";
}

export async function request(path, options = {}) {
  const headers = {
    ...(options.headers || {})
  };

  if (options.body && !headers["Content-Type"]) {
    headers["Content-Type"] = "application/json";
  }

  const token = getToken();
  if (!options.skipAuth && token) {
    headers.Authorization = `Bearer ${token}`;
  }

  const response = await fetch(`${BASE_URL}${path}`, {
    ...options,
    headers
  });

  const data = await response.json().catch(() => ({}));
  if (!response.ok || data.code !== "00000") {
    const message = data.message || `HTTP ${response.status}`;
    const error = new Error(message);
    error.code = data.code || String(response.status);
    throw error;
  }
  return data.data;
}

export async function getCaptcha() {
  return request(`/api/user/auth/captcha?_=${Date.now()}`, {
    method: "GET",
    cache: "no-store",
    skipAuth: true
  });
}

export async function login(username, password, captchaId, captchaCode) {
  return request("/api/user/auth/login", {
    method: "POST",
    body: JSON.stringify({ username, password, captchaId, captchaCode }),
    skipAuth: true
  });
}

export async function register(username, password, nickname, captchaId, captchaCode) {
  return request("/api/user/auth/register", {
    method: "POST",
    body: JSON.stringify({ username, password, nickname, captchaId, captchaCode }),
    skipAuth: true
  });
}

export async function loginByPassword(phone, password, captchaId, captchaCode) {
  return request("/api/user/auth/login/password", {
    method: "POST",
    body: JSON.stringify({ phone, password, captchaId, captchaCode }),
    skipAuth: true
  });
}

export async function loginBySms(phone, smsCode, captchaId, captchaCode) {
  return request("/api/user/auth/login/sms", {
    method: "POST",
    body: JSON.stringify({ phone, smsCode, captchaId, captchaCode }),
    skipAuth: true
  });
}

export async function sendSmsCode(phone, scene) {
  return request("/api/user/auth/sms-code/send", {
    method: "POST",
    body: JSON.stringify({ phone, scene }),
    skipAuth: true
  });
}

export async function registerBySms(phone, smsCode, password, captchaId, captchaCode, nickname = "") {
  return request("/api/user/auth/register/sms", {
    method: "POST",
    body: JSON.stringify({ phone, smsCode, password, captchaId, captchaCode, nickname }),
    skipAuth: true
  });
}

export async function me() {
  return request("/api/user/auth/me", { method: "GET" });
}

export async function sessionStatus() {
  return request("/api/user/auth/session/status", { method: "GET" });
}

export async function pingProduct() {
  return request("/api/product/ping", { method: "GET" });
}

export async function listCategories() {
  return request("/api/product/categories", { method: "GET" });
}

export async function pageProducts(params = {}) {
  return request(`/api/product/spus${toQueryString(params)}`, { method: "GET" });
}

export async function recommendProducts(params = {}) {
  return request(`/api/product/spus/recommend${toQueryString(params)}`, { method: "GET" });
}

export async function getProductDetail(id) {
  return request(`/api/product/spus/${id}`, { method: "GET" });
}

export async function getSku(id) {
  return request(`/api/product/skus/${id}`, { method: "GET" });
}

export async function listCartItems() {
  return request("/api/cart/items", { method: "GET" });
}

export async function addCartItem(skuId, quantity = 1) {
  return request("/api/cart/items", {
    method: "POST",
    body: JSON.stringify({ skuId, quantity })
  });
}

export async function updateCartItem(id, payload) {
  return request(`/api/cart/items/${id}`, {
    method: "PUT",
    body: JSON.stringify(payload)
  });
}

export async function removeCartItem(id) {
  return request(`/api/cart/items/${id}`, { method: "DELETE" });
}

export async function removeCheckedCartItems() {
  return request("/api/cart/items/checked", { method: "DELETE" });
}

export async function createOrder(payload) {
  return request("/api/order/orders", {
    method: "POST",
    body: JSON.stringify(payload)
  });
}

export async function listOrders() {
  return request("/api/order/orders", { method: "GET" });
}

export async function getOrderDetail(orderNo) {
  return request(`/api/order/orders/${orderNo}`, { method: "GET" });
}

export async function cancelOrder(orderNo) {
  return request(`/api/order/orders/${orderNo}/cancel`, { method: "POST" });
}

export async function refundOrder(orderNo) {
  return request(`/api/order/orders/${orderNo}/refund`, { method: "POST" });
}

export async function createPayment(orderNo, amount, payChannel = 1) {
  return request("/api/payment/payments", {
    method: "POST",
    body: JSON.stringify({ orderNo, amount, payChannel })
  });
}

export async function mockPaySuccess(paymentNo) {
  return request(`/api/payment/payments/${paymentNo}/mock-success`, {
    method: "POST"
  });
}

export async function listCurrentSeckillActivities() {
  return request("/api/seckill/activities/current", { method: "GET" });
}

export async function attemptSeckill(activityId, activitySkuId) {
  return request(`/api/seckill/activities/${activityId}/skus/${activitySkuId}/attempt`, {
    method: "POST"
  });
}

export async function getSeckillRecord(recordId) {
  return request(`/api/seckill/records/${recordId}`, { method: "GET" });
}
