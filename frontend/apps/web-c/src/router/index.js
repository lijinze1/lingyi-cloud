import { createRouter, createWebHistory } from "vue-router";
import { getToken } from "@shared/auth";
import LoginView from "@/views/LoginView.vue";
import RegisterView from "@/views/RegisterView.vue";
import ProductsView from "@/views/ProductsView.vue";
import ProductDetailView from "@/views/ProductDetailView.vue";
import ProfileView from "@/views/ProfileView.vue";

const routes = [
  { path: "/", redirect: "/products" },
  { path: "/login", component: LoginView, meta: { public: true } },
  { path: "/register", component: RegisterView, meta: { public: true } },
  { path: "/products", component: ProductsView, meta: { public: true } },
  { path: "/products/:id", component: ProductDetailView, meta: { public: true } },
  { path: "/profile", component: ProfileView }
];

const router = createRouter({
  history: createWebHistory(),
  routes
});

router.beforeEach((to) => {
  if (to.meta.public) return true;
  if (!getToken()) {
    return { path: "/login", query: { redirect: to.fullPath } };
  }
  return true;
});

export default router;