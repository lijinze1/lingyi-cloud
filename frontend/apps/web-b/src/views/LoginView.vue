<script setup>
import { computed, onMounted, ref } from "vue";
import { useRoute, useRouter } from "vue-router";
import authPoster from "@shared/assets/auth-poster.svg";
import { getCaptcha, login, me, setToken, setUser } from "@shared";

const route = useRoute();
const router = useRouter();

const username = ref("admin");
const password = ref("123456");
const captchaId = ref("");
const captchaCode = ref("");
const captchaImage = ref("");
const loading = ref(false);
const captchaLoading = ref(false);
const error = ref("");

const hasCaptcha = computed(() => Boolean(captchaImage.value));
const pageStyle = computed(() => ({
  backgroundImage: `linear-gradient(180deg, rgba(18, 24, 40, 0.28), rgba(31, 23, 16, 0.18)), url(${authPoster})`
}));
const posterStyle = computed(() => ({
  backgroundImage: `linear-gradient(180deg, rgba(30, 25, 36, 0.12), rgba(22, 18, 20, 0.02)), url(${authPoster})`
}));

function resolveAuthError(e, fallback) {
  const map = {
    A0410: "账号或密码错误",
    A0411: "验证码错误",
    A0412: "验证码已过期，请重新获取",
    A0401: "登录已过期，请重新登录"
  };
  return map[e?.code] || e?.message || fallback;
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
  } catch (e) {
    captchaId.value = "";
    captchaImage.value = "";
    error.value = resolveAuthError(e, "验证码暂时加载失败，请重新获取。");
  } finally {
    captchaLoading.value = false;
  }
}

function onCaptchaImageError() {
  captchaImage.value = "";
  error.value = "验证码图片渲染失败，请重新获取。";
}

async function submit() {
  error.value = "";
  loading.value = true;
  try {
    const data = await login(
      username.value.trim(),
      password.value,
      captchaId.value,
      captchaCode.value.trim()
    );
    setToken(data.token);
    const current = await me();
    setUser(current);
    router.push(String(route.query.redirect || "/dashboard"));
  } catch (e) {
    error.value = resolveAuthError(e, "登录失败");
    await loadCaptcha(false);
  } finally {
    loading.value = false;
  }
}

onMounted(loadCaptcha);
</script>

<template>
  <div class="auth-page" :style="pageStyle">
    <div class="auth-backdrop"></div>
    <main class="auth-stage">
      <section class="auth-poster" :style="posterStyle">
        <div class="auth-brand">
          <span class="auth-brand-mark">灵</span>
          <div class="auth-brand-copy">
            <strong>灵医后台</strong>
            <span>运营与管理工作台</span>
          </div>
        </div>

        <div class="auth-poster-body">
          <p class="auth-kicker">统一管理入口</p>
          <h1>登录后台，处理用户、商品与内容运营</h1>
          <p class="auth-description">按你要的方向重做成海报式登录页，用中文品牌、整页背景和更集中的登录框来收住视觉。</p>
        </div>

        <div class="auth-poster-footer">
          <span>默认账号</span>
          <strong>admin / 123456</strong>
        </div>
      </section>

      <section class="auth-panel">
        <div class="auth-panel-head">
          <span class="auth-mini">管理员登录</span>
          <h2>欢迎回来</h2>
          <p>输入账号、密码和验证码进入后台。</p>
        </div>

        <form class="auth-form" @submit.prevent="submit">
          <label class="auth-field">
            <span>账号</span>
            <input v-model="username" type="text" placeholder="请输入管理员账号" />
          </label>

          <label class="auth-field">
            <span>密码</span>
            <input v-model="password" type="password" placeholder="请输入密码" />
          </label>

          <div class="auth-field">
            <span>图形验证码</span>
            <div class="auth-captcha-row">
              <input v-model="captchaCode" type="text" placeholder="请输入验证码" />
              <button class="auth-captcha" type="button" @click="loadCaptcha()">
                <img v-if="hasCaptcha" :src="captchaImage" alt="captcha" @error="onCaptchaImageError" />
                <span v-else>{{ captchaLoading ? "加载中" : "重新获取" }}</span>
              </button>
            </div>
          </div>

          <p v-if="error" class="auth-error">{{ error }}</p>

          <button class="auth-submit" :disabled="loading" type="submit">
            {{ loading ? "登录中..." : "进入后台" }}
          </button>
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
    radial-gradient(circle at 18% 22%, rgba(255, 232, 194, 0.24), transparent 22%),
    radial-gradient(circle at 86% 18%, rgba(255, 201, 142, 0.18), transparent 24%),
    linear-gradient(180deg, rgba(12, 16, 28, 0.2), rgba(45, 28, 14, 0.14));
}

.auth-stage {
  position: relative;
  z-index: 1;
  width: min(1180px, 100%);
  margin: 0 auto;
  min-height: calc(100vh - 130px);
  display: grid;
  grid-template-columns: minmax(0, 1.06fr) minmax(360px, 440px);
  border-radius: 34px;
  overflow: hidden;
  background: rgba(255, 250, 245, 0.9);
  box-shadow: 0 36px 90px rgba(32, 18, 10, 0.26);
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
  background: linear-gradient(135deg, #d97432, #f0af54);
  color: #fff7eb;
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
  max-width: 420px;
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
  font-size: clamp(42px, 5vw, 64px);
  line-height: 1.08;
  letter-spacing: -0.05em;
}

.auth-description {
  margin: 18px 0 0;
  max-width: 360px;
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

.auth-poster-footer span {
  font-size: 13px;
  opacity: 0.82;
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
  margin-bottom: 24px;
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

.auth-captcha {
  min-height: 54px;
  border-radius: 18px;
  border: 1px solid rgba(196, 111, 51, 0.16);
  background: rgba(255, 255, 255, 0.94);
  padding: 0;
  overflow: hidden;
  cursor: pointer;
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

  .auth-captcha-row {
    grid-template-columns: 1fr;
  }

  .auth-poster-footer strong {
    font-size: 20px;
  }
}
</style>
