<script setup>
import { computed, ref, watch } from "vue";
import { RouterLink, RouterView, useRoute, useRouter } from "vue-router";
import { clearAuth, getToken, getUser } from "@shared";

const route = useRoute();
const router = useRouter();
const menuOpen = ref(false);

const isAuthPage = computed(() => Boolean(route.meta?.authPage));
const authSnapshot = computed(() => {
  route.fullPath;
  return {
    token: getToken(),
    user: getUser()
  };
});
const isAuthed = computed(() => Boolean(authSnapshot.value.token));
const currentUser = computed(() => authSnapshot.value.user);
const userLabel = computed(() => currentUser.value?.nickname || currentUser.value?.username || "用户");
const userInitial = computed(() => userLabel.value.slice(0, 1));

watch(
  () => route.fullPath,
  () => {
    menuOpen.value = false;
  }
);

function toggleMenu() {
  menuOpen.value = !menuOpen.value;
}

function logout() {
  clearAuth();
  menuOpen.value = false;
  router.push("/login");
}

function isNavActive(target) {
  if (target === "/") {
    return route.path === "/";
  }
  return route.path === target || route.path.startsWith(`${target}/`);
}
</script>

<template>
  <RouterView v-if="isAuthPage" />

  <template v-else>
    <header class="store-topbar">
      <div class="store-topbar-inner">
        <RouterLink class="store-brand" to="/">
          <span class="store-brand-mark">灵</span>
          <div class="store-brand-copy">
            <strong>灵医商城</strong>
            <span>常备药品与 AI 问诊</span>
          </div>
        </RouterLink>

        <nav class="store-nav" aria-label="主导航">
          <RouterLink :class="['store-nav-link', { active: isNavActive('/') }]" to="/">首页</RouterLink>
          <RouterLink :class="['store-nav-link', { active: isNavActive('/products') }]" to="/products">商品</RouterLink>
          <RouterLink :class="['store-nav-link', { active: isNavActive('/seckill') }]" to="/seckill">秒杀</RouterLink>
          <RouterLink :class="['store-nav-link', { active: isNavActive('/cart') }]" to="/cart">购物车</RouterLink>
        </nav>

        <div class="store-actions">
          <template v-if="!isAuthed">
            <RouterLink class="store-action-link" to="/login">登录</RouterLink>
            <RouterLink class="store-action-primary" to="/register">注册</RouterLink>
          </template>

          <div v-else class="store-user-menu">
            <button class="store-user-trigger" type="button" @click="toggleMenu">
              <span class="store-user-avatar">{{ userInitial }}</span>
              <span class="store-user-name">{{ userLabel }}</span>
            </button>

            <div v-if="menuOpen" class="store-user-dropdown">
              <RouterLink class="store-user-item" to="/profile">个人中心</RouterLink>
              <RouterLink class="store-user-item" to="/cart">购物车</RouterLink>
              <button class="store-user-item store-user-item-button" type="button" @click="logout">退出登录</button>
            </div>
          </div>
        </div>
      </div>
    </header>

    <main class="store-main">
      <div class="store-shell">
        <RouterView />
      </div>
    </main>
  </template>
</template>

<style scoped>
.store-topbar {
  position: sticky;
  top: 0;
  z-index: 30;
  backdrop-filter: blur(18px);
  background: rgba(255, 251, 246, 0.86);
  border-bottom: 1px solid rgba(95, 59, 39, 0.08);
}

.store-topbar-inner {
  width: min(1280px, calc(100% - 40px));
  margin: 0 auto;
  min-height: 86px;
  display: grid;
  grid-template-columns: auto 1fr auto;
  align-items: center;
  gap: 24px;
}

.store-brand {
  display: inline-flex;
  align-items: center;
  gap: 14px;
  text-decoration: none;
  color: #241912;
}

.store-brand-mark {
  width: 46px;
  height: 46px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  border-radius: 16px;
  background: linear-gradient(135deg, #ff6d4d, #ff9a52);
  color: #fffaf4;
  font-size: 22px;
  font-weight: 700;
  box-shadow: 0 14px 24px rgba(255, 109, 77, 0.22);
}

.store-brand-copy {
  display: grid;
  gap: 2px;
}

.store-brand-copy strong {
  font-size: 22px;
}

.store-brand-copy span {
  color: rgba(36, 25, 18, 0.62);
  font-size: 12px;
}

.store-nav {
  display: flex;
  justify-content: center;
  gap: 8px;
}

.store-nav-link {
  padding: 12px 16px;
  border-radius: 999px;
  color: rgba(36, 25, 18, 0.76);
  text-decoration: none;
  transition: background-color 0.2s ease, color 0.2s ease, transform 0.2s ease;
}

.store-nav-link:hover,
.store-nav-link.active {
  background: rgba(255, 109, 77, 0.1);
  color: #241912;
  transform: translateY(-1px);
}

.store-actions {
  display: flex;
  align-items: center;
  gap: 10px;
}

.store-action-link,
.store-action-primary {
  min-height: 42px;
  padding: 0 18px;
  border-radius: 999px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  text-decoration: none;
  transition: transform 0.2s ease;
}

.store-action-link {
  color: rgba(36, 25, 18, 0.78);
}

.store-action-primary {
  background: linear-gradient(135deg, #ff6d4d, #ff8757);
  color: #fff8f2;
  box-shadow: 0 14px 22px rgba(255, 109, 77, 0.22);
}

.store-action-link:hover,
.store-action-primary:hover,
.store-user-trigger:hover {
  transform: translateY(-1px);
}

.store-user-menu {
  position: relative;
}

.store-user-trigger {
  border: 0;
  background: rgba(255, 255, 255, 0.8);
  border-radius: 999px;
  padding: 7px 10px 7px 8px;
  display: inline-flex;
  align-items: center;
  gap: 10px;
  cursor: pointer;
  box-shadow: 0 12px 22px rgba(86, 54, 32, 0.08);
}

.store-user-avatar {
  width: 34px;
  height: 34px;
  border-radius: 50%;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #ff8b5e, #ffb05f);
  color: #fff9f0;
  font-weight: 700;
}

.store-user-name {
  color: #241912;
  font-weight: 600;
}

.store-user-dropdown {
  position: absolute;
  right: 0;
  top: calc(100% + 10px);
  min-width: 170px;
  padding: 8px;
  border-radius: 18px;
  background: rgba(255, 252, 248, 0.98);
  box-shadow: 0 22px 42px rgba(54, 34, 20, 0.16);
  border: 1px solid rgba(95, 59, 39, 0.08);
  display: grid;
  gap: 4px;
}

.store-user-item {
  min-height: 42px;
  padding: 0 14px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  text-decoration: none;
  color: #241912;
}

.store-user-item:hover {
  background: rgba(255, 109, 77, 0.08);
}

.store-user-item-button {
  border: 0;
  background: transparent;
  font: inherit;
  cursor: pointer;
}

.store-main {
  padding: 24px 0 46px;
}

.store-shell {
  width: min(1280px, calc(100% - 40px));
  margin: 0 auto;
}

@media (max-width: 900px) {
  .store-topbar-inner {
    grid-template-columns: 1fr;
    justify-items: start;
    padding: 16px 0;
    gap: 14px;
  }

  .store-nav {
    justify-content: flex-start;
    flex-wrap: wrap;
  }

  .store-actions {
    width: 100%;
    justify-content: flex-start;
  }
}

@media (max-width: 640px) {
  .store-topbar-inner,
  .store-shell {
    width: min(1280px, calc(100% - 24px));
  }

  .store-brand-copy strong {
    font-size: 20px;
  }

  .store-user-name {
    display: none;
  }
}
</style>
