import { createRouter, createWebHistory } from "vue-router";
import { clearAuth, getToken, hasRole, me, setUser } from "@shared";
import LoginView from "@/views/LoginView.vue";
import DashboardView from "@/views/DashboardView.vue";
import UsersView from "@/views/UsersView.vue";
import ProductsView from "@/views/ProductsView.vue";
import PromptsView from "@/views/PromptsView.vue";
import KnowledgeView from "@/views/KnowledgeView.vue";
import UnauthorizedView from "@/views/UnauthorizedView.vue";

const routes = [
  { path: "/", redirect: "/dashboard" },
  { path: "/login", component: LoginView, meta: { public: true, authPage: true } },
  { path: "/unauthorized", component: UnauthorizedView, meta: { public: true } },
  { path: "/dashboard", component: DashboardView },
  { path: "/users", component: UsersView },
  { path: "/products", component: ProductsView },
  { path: "/prompts", component: PromptsView },
  { path: "/knowledge", component: KnowledgeView },
  { path: "/:pathMatch(.*)*", redirect: "/dashboard" }
];

const router = createRouter({
  history: createWebHistory(),
  routes
});

router.beforeEach(async (to) => {
  if (to.meta.authPage && getToken()) {
    return { path: "/dashboard" };
  }

  if (to.meta.public && to.path === "/login") {
    return true;
  }

  if (!getToken()) {
    return { path: "/login", query: { redirect: to.fullPath } };
  }

  let user;
  try {
    user = await me();
    setUser(user);
  } catch {
    clearAuth();
    return { path: "/login", query: { redirect: to.fullPath } };
  }

  if (!hasRole("ROLE_ADMIN")) {
    if (to.path === "/unauthorized") {
      return true;
    }
    return { path: "/unauthorized" };
  }

  return true;
});

export default router;
