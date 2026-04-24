import { createRouter, createWebHistory } from "vue-router";
import { getToken, getUser, hasRole, me, setUser, clearAuth } from "@shared";
import LoginView from "@/views/LoginView.vue";
import DashboardView from "@/views/DashboardView.vue";
import UsersView from "@/views/UsersView.vue";
import ProductsView from "@/views/ProductsView.vue";
import PromptsView from "@/views/PromptsView.vue";
import KnowledgeView from "@/views/KnowledgeView.vue";
import UnauthorizedView from "@/views/UnauthorizedView.vue";

const routes = [
  { path: "/", redirect: "/dashboard" },
  { path: "/login", component: LoginView, meta: { public: true } },
  { path: "/unauthorized", component: UnauthorizedView, meta: { public: true } },
  { path: "/dashboard", component: DashboardView },
  { path: "/users", component: UsersView },
  { path: "/products", component: ProductsView },
  { path: "/prompts", component: PromptsView },
  { path: "/knowledge", component: KnowledgeView }
];

const router = createRouter({
  history: createWebHistory(),
  routes
});

router.beforeEach(async (to) => {
  if (to.meta.public) return true;

  if (!getToken()) {
    return { path: "/login", query: { redirect: to.fullPath } };
  }

  let user = getUser();
  if (!user) {
    try {
      user = await me();
      setUser(user);
    } catch {
      clearAuth();
      return { path: "/login", query: { redirect: to.fullPath } };
    }
  }

  if (!hasRole("ROLE_ADMIN")) {
    return { path: "/unauthorized" };
  }

  return true;
});

export default router;
