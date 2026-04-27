<script setup>
import { computed, onMounted, onUnmounted, ref, watch } from "vue";
import { RouterLink, useRoute, useRouter } from "vue-router";
import authPoster from "@shared/assets/auth-poster.svg";
import {
  clearAuth,
  getCaptcha,
  loginByPassword,
  loginBySms,
  sendSmsCode,
  setToken,
  setUser
} from "@shared";

const route = useRoute();
const router = useRouter();

const mode = ref("password");
const phone = ref("13800000000");
const password = ref("123456");
const smsCode = ref("");
const captchaId = ref("");
const captchaCode = ref("");
const captchaImage = ref("");
const loading = ref(false);
const captchaLoading = ref(false);
const smsLoading = ref(false);
const smsCountdown = ref(0);
const smsHint = ref("开发环境短信验证码固定为 123456");
const error = ref("");

let countdownTimer = null;

const hasCaptcha = computed(() => Boolean(captchaImage.value));
const pageStyle = computed(() => ({
  backgroundImage: `linear-gradient(180deg, rgba(18, 24, 40, 0.24), rgba(31, 23, 16, 0.16)), url(${authPoster})`
}));
const posterStyle = computed(() => ({
  backgroundImage: `linear-gradient(180deg, rgba(22, 24, 38, 0.08), rgba(22, 18, 20, 0.02)), url(${authPoster})`
}));
const smsButtonText = computed(() => {
  if (smsLoading.value) return "发送中...";
  if (smsCountdown.value > 0) return `${smsCountdown.value}s 后重发`;
  return "获取验证码";
});

watch(mode, (nextMode) => {
  error.value = "";
  captchaCode.value = "";
  if (nextMode === "password") {
    smsCode.value = "";
  }
});

function normalizeRedirect(target) {
  return typeof target === "string" && target.startsWith("/") ? target : "/";
}

function buildUserSnapshot(data) {
  return {
    userId: data.userId,
    username: data.username,
    nickname: data.nickname,
    roles: data.roles || [],
    permissions: data.permissions || { menus: [], buttons: [], apis: [] }
  };
}

function resolveAuthError(e, currentMode, action = "submit") {
  const code = e?.code;
  if (code === "A0401") {
    return action === "sendSms" ? "请求已失效，请刷新页面后重试" : "登录状态已过期，请重新登录";
  }
  if (code === "A0410") return "手机号或密码不正确";
  if (code === "A0411") return "图形验证码不正确";
  if (code === "A0412") return "图形验证码已过期，请重新获取";
  if (code === "A0413") return "请输入正确的 11 位手机号";
  if (code === "A0414") return currentMode === "sms" ? "短信验证码不正确" : "请检查登录信息";
  if (code === "A0415") return currentMode === "sms" ? "请先获取验证码" : "请检查登录信息";
  if (code === "A0416") return "发送太频繁了，请稍后再试";
  if (code === "A0417") return "验证码场景错误，请刷新后重试";
  if (code === "A0418") return "该手机号已注册，请直接登录";
  if (code === "A0419") return "该手机号未注册，请先注册";
  return e?.message || "提交失败，请稍后再试";
}

function isValidPhone(value) {
  return /^1\d{10}$/.test(String(value || "").trim());
}

function startSmsCountdown(seconds = 60) {
  smsCountdown.value = Number(seconds) || 60;
  if (countdownTimer) {
    clearInterval(countdownTimer);
  }
  countdownTimer = window.setInterval(() => {
    if (smsCountdown.value <= 1) {
      smsCountdown.value = 0;
      clearInterval(countdownTimer);
      countdownTimer = null;
      return;
    }
    smsCountdown.value -= 1;
  }, 1000);
}

async function loadCaptcha(clearError = true) {
  captchaLoading.value = true;
  if (clearError) {
    error.value = "";
  }
  try {
    const data = await getCaptcha();
    captchaId.value = data.captchaId;
    captchaImage.value = data.imageBase64 || "";
    captchaCode.value = "";
  } catch {
    captchaId.value = "";
    captchaImage.value = "";
    error.value = "验证码加载失败，请重新获取";
  } finally {
    captchaLoading.value = false;
  }
}

function onCaptchaImageError() {
  captchaImage.value = "";
  error.value = "验证码图片加载失败，请重新获取";
}

async function sendLoginSmsCode() {
  error.value = "";
  if (!isValidPhone(phone.value)) {
    error.value = "请输入正确的 11 位手机号";
    return;
  }
  smsLoading.value = true;
  try {
    const data = await sendSmsCode(phone.value.trim(), "login");
    smsHint.value = `开发环境短信验证码固定为 ${data.mockCode || "123456"}`;
    startSmsCountdown(data.cooldownSeconds || 60);
  } catch (e) {
    error.value = resolveAuthError(e, "sms", "sendSms");
  } finally {
    smsLoading.value = false;
  }
}

async function submit() {
  error.value = "";
  loading.value = true;
  try {
    clearAuth();
    const payload = mode.value === "password"
      ? await loginByPassword(phone.value.trim(), password.value, captchaId.value, captchaCode.value.trim())
      : await loginBySms(phone.value.trim(), smsCode.value.trim(), captchaId.value, captchaCode.value.trim());
    setToken(payload.token);
    setUser(buildUserSnapshot(payload));
    router.push(normalizeRedirect(route.query.redirect));
  } catch (e) {
    clearAuth();
    error.value = resolveAuthError(e, mode.value);
    await loadCaptcha(false);
  } finally {
    loading.value = false;
  }
}

onMounted(async () => {
  clearAuth();
  await loadCaptcha();
});
onUnmounted(() => {
  if (countdownTimer) {
    clearInterval(countdownTimer);
  }
});
</script>

<template>
  <div class="auth-page" :style="pageStyle">
    <div class="auth-backdrop"></div>
    <main class="auth-stage">
      <section class="auth-poster" :style="posterStyle">
        <div class="auth-brand">
          <span class="auth-brand-mark">灵</span>
          <div class="auth-brand-copy">
            <strong>灵医商城</strong>
            <span>常备药与健康服务入口</span>
          </div>
        </div>

        <div class="auth-poster-body">
          <p class="auth-kicker">统一登录</p>
          <h1>备药、复购、问药，一个账号就够了</h1>
          <p class="auth-description">常备药补货更快，订单和问药记录也能接着用。</p>
        </div>

        <div class="auth-poster-footer">
          <span>开发体验</span>
          <strong>13800000000 / 123456</strong>
          <small>短信验证码固定为 123456</small>
        </div>
      </section>

      <section class="auth-panel">
        <div class="auth-panel-head">
          <span class="auth-mini">用户登录</span>
          <h2>欢迎回来</h2>
          <p>补药、查单、问药建议，登录后都能接着用。</p>
        </div>

        <div class="auth-tabs">
          <button :class="['auth-tab', { active: mode === 'password' }]" type="button" @click="mode = 'password'">
            密码登录
          </button>
          <button :class="['auth-tab', { active: mode === 'sms' }]" type="button" @click="mode = 'sms'">
            短信登录
          </button>
        </div>

        <form class="auth-form" autocomplete="off" @submit.prevent="submit">
          <label class="auth-field">
            <span>手机号</span>
            <input
              v-model="phone"
              type="text"
              name="phone"
              inputmode="numeric"
              maxlength="11"
              autocomplete="tel"
              placeholder="请输入手机号"
            />
          </label>

          <label v-if="mode === 'password'" class="auth-field">
            <span>密码</span>
            <input
              v-model="password"
              type="password"
              name="password"
              autocomplete="current-password"
              placeholder="请输入密码"
            />
          </label>

          <div v-else class="auth-field">
            <span>短信验证码</span>
            <div class="auth-captcha-row auth-sms-row">
              <input
                v-model="smsCode"
                type="text"
                name="smsCode"
                inputmode="numeric"
                maxlength="6"
                autocomplete="one-time-code"
                placeholder="请输入验证码"
              />
              <button class="auth-secondary" :disabled="smsLoading || smsCountdown > 0" type="button" @click="sendLoginSmsCode">
                {{ smsButtonText }}
              </button>
            </div>
            <p class="auth-hint">{{ smsHint }}</p>
          </div>

          <div class="auth-field">
            <span>图形验证码</span>
            <div class="auth-captcha-row">
              <input
                v-model="captchaCode"
                type="text"
                name="captchaCode"
                autocomplete="off"
                autocapitalize="off"
                spellcheck="false"
                placeholder="请输入图形验证码"
              />
              <button class="auth-captcha" type="button" @click="loadCaptcha()">
                <img v-if="hasCaptcha" :src="captchaImage" alt="captcha" @error="onCaptchaImageError" />
                <span v-else>{{ captchaLoading ? '加载中...' : '重新获取' }}</span>
              </button>
            </div>
          </div>

          <div class="auth-error-slot">
            <p v-if="error" class="auth-error">{{ error }}</p>
          </div>

          <button class="auth-submit" :disabled="loading" type="submit">
            {{ loading ? '登录中...' : '进入用户端' }}
          </button>

          <div class="auth-actions">
            <p>还没有账号？<RouterLink to="/register">去注册</RouterLink></p>
            <RouterLink to="/">返回首页</RouterLink>
          </div>
        </form>
      </section>
    </main>

    <footer class="auth-footer">Copyright © 2014-2026 灵医信息科技（上海）有限公司</footer>
  </div>
</template>

<style scoped>
.auth-page {
  position: relative;
  min-height: 100vh;
  padding: 48px 24px 28px;
  background-position: center;
  background-repeat: no-repeat;
  background-size: cover;
  overflow: hidden;
}

.auth-backdrop {
  position: absolute;
  inset: 0;
  background:
    radial-gradient(circle at 20% 20%, rgba(255, 232, 194, 0.2), transparent 22%),
    radial-gradient(circle at 80% 18%, rgba(255, 195, 136, 0.16), transparent 24%),
    linear-gradient(180deg, rgba(14, 18, 29, 0.18), rgba(38, 24, 16, 0.12));
}

.auth-stage {
  position: relative;
  z-index: 1;
  width: min(1180px, 100%);
  margin: 0 auto;
  min-height: calc(100vh - 130px);
  display: grid;
  grid-template-columns: minmax(0, 1.04fr) minmax(360px, 440px);
  border-radius: 34px;
  overflow: hidden;
  background: rgba(255, 250, 245, 0.9);
  box-shadow: 0 36px 90px rgba(32, 18, 10, 0.24);
  backdrop-filter: blur(14px);
}

.auth-poster {
  position: relative;
  min-height: 720px;
  padding: 30px 34px 34px;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  background-position: center;
  background-repeat: no-repeat;
  background-size: cover;
}

.auth-poster::after {
  content: "";
  position: absolute;
  inset: 0;
  background: linear-gradient(180deg, rgba(255, 247, 236, 0.04), rgba(52, 27, 18, 0.08));
}

.auth-brand,
.auth-poster-body,
.auth-poster-footer,
.auth-panel {
  position: relative;
  z-index: 1;
}

.auth-brand {
  display: inline-flex;
  align-items: center;
  gap: 14px;
  width: fit-content;
  padding: 12px 16px 12px 12px;
  border-radius: 999px;
  background: rgba(255, 251, 246, 0.9);
  box-shadow: 0 12px 26px rgba(45, 24, 16, 0.12);
}

.auth-brand-mark {
  width: 44px;
  height: 44px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  border-radius: 16px;
  background: linear-gradient(135deg, #df7d34, #f0b253);
  color: #fff8ef;
  font-size: 22px;
  font-weight: 700;
}

.auth-brand-copy {
  display: grid;
  gap: 3px;
}

.auth-brand-copy strong {
  font-size: 22px;
  color: #2f241d;
}

.auth-brand-copy span {
  font-size: 13px;
  color: rgba(47, 36, 29, 0.72);
}

.auth-poster-body {
  max-width: 430px;
  padding-top: 100px;
}

.auth-kicker {
  margin: 0 0 14px;
  color: #fff2d2;
  font-size: 14px;
  letter-spacing: 0.18em;
}

.auth-poster-body h1 {
  margin: 0;
  color: #fff9f2;
  font-size: clamp(42px, 5vw, 62px);
  line-height: 1.08;
  letter-spacing: -0.05em;
}

.auth-description {
  margin: 18px 0 0;
  max-width: 380px;
  color: rgba(255, 247, 236, 0.88);
  line-height: 1.8;
  font-size: 16px;
}

.auth-poster-footer {
  display: inline-flex;
  flex-direction: column;
  gap: 6px;
  width: fit-content;
  padding: 16px 18px;
  border-radius: 22px;
  background: rgba(54, 34, 25, 0.24);
  color: #fff4e3;
  backdrop-filter: blur(12px);
}

.auth-poster-footer span,
.auth-poster-footer small {
  font-size: 13px;
  opacity: 0.86;
}

.auth-poster-footer strong {
  font-size: 22px;
}

.auth-panel {
  display: flex;
  flex-direction: column;
  justify-content: center;
  padding: 42px 38px;
  background: linear-gradient(180deg, rgba(255, 252, 248, 0.98), rgba(255, 247, 239, 0.96));
}

.auth-panel-head {
  margin-bottom: 22px;
}

.auth-mini {
  display: inline-block;
  margin-bottom: 10px;
  color: #c46f33;
  font-size: 13px;
  letter-spacing: 0.14em;
}

.auth-panel-head h2 {
  margin: 0;
  font-size: 38px;
  color: #291d16;
  letter-spacing: -0.05em;
}

.auth-panel-head p {
  margin: 10px 0 0;
  color: rgba(41, 29, 22, 0.68);
  line-height: 1.7;
}

.auth-tabs {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 10px;
  margin-bottom: 18px;
  padding: 6px;
  border-radius: 999px;
  background: rgba(216, 115, 50, 0.08);
}

.auth-tab {
  min-height: 42px;
  border: 0;
  border-radius: 999px;
  background: transparent;
  color: rgba(41, 29, 22, 0.7);
  font: inherit;
  font-weight: 600;
  cursor: pointer;
}

.auth-tab.active {
  background: #fff7f0;
  color: #b95f26;
  box-shadow: 0 12px 24px rgba(185, 95, 38, 0.14);
}

.auth-form {
  display: grid;
  gap: 16px;
}

.auth-field {
  display: grid;
  gap: 8px;
}

.auth-field span {
  color: rgba(41, 29, 22, 0.72);
  font-size: 13px;
}

.auth-field input {
  width: 100%;
  min-height: 54px;
  border-radius: 18px;
  border: 1px solid rgba(196, 111, 51, 0.16);
  background: rgba(255, 255, 255, 0.92);
  padding: 0 16px;
  font: inherit;
  color: #291d16;
}

.auth-field input:focus {
  outline: none;
  border-color: rgba(196, 111, 51, 0.42);
  box-shadow: 0 0 0 4px rgba(217, 116, 50, 0.12);
}

.auth-captcha-row {
  display: grid;
  grid-template-columns: 1fr 142px;
  gap: 10px;
}

.auth-secondary,
.auth-captcha {
  min-height: 54px;
  border-radius: 18px;
  border: 1px solid rgba(196, 111, 51, 0.16);
  background: rgba(255, 255, 255, 0.94);
  cursor: pointer;
}

.auth-secondary {
  font: inherit;
  color: #c85f25;
  font-weight: 600;
}

.auth-secondary:disabled {
  cursor: not-allowed;
  color: rgba(41, 29, 22, 0.5);
}

.auth-captcha {
  padding: 0;
  overflow: hidden;
}

.auth-captcha img {
  display: block;
  width: 100%;
  height: 100%;
  object-fit: contain;
}

.auth-captcha span {
  display: flex;
  width: 100%;
  height: 100%;
  align-items: center;
  justify-content: center;
  color: rgba(41, 29, 22, 0.72);
}

.auth-hint {
  margin: 0;
  color: rgba(41, 29, 22, 0.56);
  font-size: 12px;
}

.auth-error-slot {
  min-height: 56px;
}

.auth-error {
  margin: 0;
  padding: 13px 15px;
  border-radius: 18px;
  border: 1px solid rgba(202, 92, 67, 0.22);
  background: rgba(202, 92, 67, 0.1);
  color: #c45146;
}

.auth-submit {
  min-height: 56px;
  border: 0;
  border-radius: 999px;
  background: linear-gradient(135deg, #d87332, #c85f25);
  color: #fff8ef;
  font: inherit;
  font-weight: 700;
  cursor: pointer;
  box-shadow: 0 18px 32px rgba(200, 95, 37, 0.24);
}

.auth-submit:disabled {
  opacity: 0.72;
  cursor: not-allowed;
  box-shadow: none;
}

.auth-actions {
  display: flex;
  justify-content: space-between;
  gap: 14px;
  color: rgba(41, 29, 22, 0.68);
}

.auth-actions p {
  margin: 0;
}

.auth-actions a {
  color: #c85f25;
  text-decoration: none;
  font-weight: 600;
}

.auth-footer {
  position: relative;
  z-index: 1;
  width: min(1180px, 100%);
  margin: 18px auto 0;
  text-align: center;
  color: rgba(255, 246, 235, 0.84);
  font-size: 13px;
}

@media (max-width: 1024px) {
  .auth-stage {
    grid-template-columns: 1fr;
  }

  .auth-poster {
    min-height: 430px;
  }
}

@media (max-width: 768px) {
  .auth-page {
    padding: 18px 12px 22px;
  }

  .auth-stage {
    min-height: auto;
    border-radius: 28px;
  }

  .auth-poster,
  .auth-panel {
    padding: 24px 20px;
  }

  .auth-poster-body {
    padding-top: 48px;
  }

  .auth-tabs,
  .auth-captcha-row,
  .auth-actions {
    grid-template-columns: 1fr;
    display: grid;
  }

  .auth-poster-footer strong {
    font-size: 20px;
  }
}
</style>