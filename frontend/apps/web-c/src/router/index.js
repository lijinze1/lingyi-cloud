import { createRouter, createWebHistory } from "vue-router";
import { getToken } from "@shared/auth";
import HomeView from "@/views/HomeView.vue";
import LoginView from "@/views/LoginView.vue";
import RegisterView from "@/views/RegisterView.vue";
import ProductsView from "@/views/ProductsView.vue";
import ProductDetailView from "@/views/ProductDetailView.vue";
import ProfileView from "@/views/ProfileView.vue";
import CartView from "@/views/CartView.vue";
import SeckillOrderView from "@/views/SeckillOrderView.vue";
import CheckoutView from "@/views/CheckoutView.vue";
import OrderDetailView from "@/views/OrderDetailView.vue";

const routes = [
  { path: "/", component: HomeView, meta: { public: true } },
  { path: "/login", component: LoginView, meta: { public: true, authPage: true } },
  { path: "/register", component: RegisterView, meta: { public: true, authPage: true } },
  { path: "/products", component: ProductsView },
  { path: "/products/:id", component: ProductDetailView },
  { path: "/checkout", component: CheckoutView },
  { path: "/cart", component: CartView },
  { path: "/profile", component: ProfileView },
  { path: "/orders/:orderNo", component: OrderDetailView },
  { path: "/seckill/:recordId", component: SeckillOrderView },
  { path: "/:pathMatch(.*)*", redirect: "/" }
];

const router = createRouter({
  history: createWebHistory(),
  routes
});

function normalizeRedirect(target) {
  return typeof target === "string" && target.startsWith("/") ? target : "/";
}

router.beforeEach((to) => {
  const token = getToken();

  if (to.meta.authPage && token) {
    return { path: normalizeRedirect(to.query.redirect) };
  }

  if (to.meta.public) {
    return true;
  }

  if (!token) {
    return { path: "/login", query: { redirect: to.fullPath } };
  }

  return true;
});

export default router;
