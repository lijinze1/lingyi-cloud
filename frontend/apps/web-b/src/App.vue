<script setup>
import { computed, onBeforeUnmount, onMounted } from "vue";
import { RouterLink, RouterView, useRoute, useRouter } from "vue-router";
import { clearAuth, getUser } from "@shared";

const route = useRoute();
const router = useRouter();

const isAuthPage = computed(() => Boolean(route.meta?.authPage));
const currentUser = computed(() => {
  route.fullPath;
  return getUser();
});
const roleText = computed(() => (currentUser.value?.roles || []).join(" / ") || "ROLE_ADMIN");

function logout() {
  clearAuth();
  router.push("/login");
}

function handleAuthExpired(event) {
  const redirect = event?.detail?.redirect || route.fullPath || "/dashboard";
  router.push({ path: "/login", query: { redirect } });
}

onMounted(() => {
  if (typeof window !== "undefined") {
    window.addEventListener("lingyi-auth-expired", handleAuthExpired);
  }
});

onBeforeUnmount(() => {
  if (typeof window !== "undefined") {
    window.removeEventListener("lingyi-auth-expired", handleAuthExpired);
  }
});
</script>

<template>
  <RouterView v-if="isAuthPage" />

  <div v-else class="admin-shell">
    <aside class="admin-sidebar">
      <div class="admin-brand">
        <span class="admin-brand-mark">灵</span>
        <div class="admin-brand-copy">
          <strong>灵医后台</strong>
          <span>运营管理工作台</span>
        </div>
      </div>

      <nav class="admin-nav">
        <RouterLink class="admin-nav-link" to="/dashboard">
          <strong>工作台</strong>
          <span>概览与待办</span>
        </RouterLink>
        <RouterLink class="admin-nav-link" to="/users">
          <strong>用户</strong>
          <span>账号与角色</span>
        </RouterLink>
        <RouterLink class="admin-nav-link" to="/products">
          <strong>商品</strong>
          <span>库存与审核</span>
        </RouterLink>
        <RouterLink class="admin-nav-link" to="/prompts">
          <strong>提示词</strong>
          <span>版本与场景</span>
        </RouterLink>
        <RouterLink class="admin-nav-link" to="/knowledge">
          <strong>知识库</strong>
          <span>文档与同步</span>
        </RouterLink>
      </nav>

      <div class="admin-sidebar-foot">
        <span>当前角色</span>
        <strong>{{ roleText }}</strong>
      </div>
    </aside>

    <main class="admin-main">
      <header class="admin-topbar">
        <div>
          <p>今日工作台</p>
          <h1>{{ currentUser?.nickname || currentUser?.username || "管理员" }}</h1>
        </div>
        <button class="admin-logout" type="button" @click="logout">退出登录</button>
      </header>

      <section class="admin-content">
        <RouterView />
      </section>
    </main>
  </div>
</template>

<style scoped>
.admin-shell {
  min-height: 100vh;
  display: grid;
  grid-template-columns: 280px 1fr;
  background:
    radial-gradient(circle at 18% 12%, rgba(255, 186, 122, 0.12), transparent 20%),
    linear-gradient(180deg, #fff9f2 0%, #f8efe3 100%);
}

.admin-sidebar {
  position: sticky;
  top: 0;
  height: 100vh;
  overflow-y: auto;
  padding: 24px 18px;
  background: linear-gradient(180deg, #2f2a38 0%, #26212c 100%);
  color: #fff6eb;
  display: flex;
  flex-direction: column;
  gap: 28px;
}

.admin-brand {
  display: flex;
  align-items: center;
  gap: 14px;
}

.admin-brand-mark {
  width: 48px;
  height: 48px;
  border-radius: 16px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #ff7d57, #ffb15f);
  color: #fff8ef;
  font-size: 24px;
  font-weight: 700;
}

.admin-brand-copy {
  display: grid;
  gap: 2px;
}

.admin-brand-copy strong {
  font-size: 24px;
}

.admin-brand-copy span,
.admin-sidebar-foot span {
  color: rgba(255, 246, 235, 0.66);
  font-size: 13px;
}

.admin-nav {
  display: grid;
  gap: 10px;
}

.admin-nav-link {
  padding: 14px 16px;
  border-radius: 20px;
  text-decoration: none;
  color: rgba(255, 246, 235, 0.78);
  background: rgba(255, 255, 255, 0.02);
  transition: transform 0.2s ease, background-color 0.2s ease;
}

.admin-nav-link strong {
  display: block;
  font-size: 18px;
}

.admin-nav-link span {
  display: block;
  margin-top: 6px;
  font-size: 13px;
  color: rgba(255, 246, 235, 0.58);
}

.admin-nav-link:hover,
.admin-nav-link.router-link-active {
  transform: translateX(2px);
  background: rgba(255, 255, 255, 0.08);
}

.admin-sidebar-foot {
  margin-top: auto;
  padding: 18px;
  border-radius: 22px;
  background: rgba(255, 255, 255, 0.05);
}

.admin-sidebar-foot strong {
  display: block;
  margin-top: 8px;
  font-size: 18px;
}

.admin-main {
  padding: 26px;
}

.admin-topbar {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: 18px;
  margin-bottom: 22px;
}

.admin-topbar p {
  margin: 0 0 8px;
  color: rgba(36, 25, 18, 0.58);
}

.admin-topbar h1 {
  margin: 0;
  font-size: 44px;
  color: #241912;
}

.admin-logout {
  min-height: 46px;
  padding: 0 18px;
  border: 0;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.8);
  color: #241912;
  cursor: pointer;
  box-shadow: inset 0 0 0 1px rgba(95, 59, 39, 0.08);
}

.admin-content {
  display: grid;
  gap: 18px;
}

@media (max-width: 980px) {
  .admin-shell {
    grid-template-columns: 1fr;
  }

  .admin-sidebar {
    position: static;
    height: auto;
    overflow: visible;
    gap: 18px;
  }
}
</style>
