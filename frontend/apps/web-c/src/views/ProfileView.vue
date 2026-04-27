<script setup>
import { computed, onMounted, ref } from "vue";
import { RouterLink, useRoute, useRouter } from "vue-router";
import {
  cancelOrder,
  clearAuth,
  createPayment,
  getUser,
  listOrders,
  me,
  mockPaySuccess,
  refundOrder,
  setUser
} from "@shared";

const router = useRouter();
const route = useRoute();
const user = ref(getUser());
const orders = ref([]);
const error = ref("");
const success = ref("");
const actionLoading = ref("");
const activeTab = ref(route.query.tab || "all");

const tabs = [
  { key: "all", label: "全部订单" },
  { key: "unpaid", label: "待付款" },
  { key: "paid", label: "已支付" },
  { key: "refunded", label: "已退款" },
  { key: "cancelled", label: "已取消" }
];

const filteredOrders = computed(() => {
  return (orders.value || []).filter((order) => {
    if (activeTab.value === "unpaid") {
      return Number(order.payStatus) !== 1 && Number(order.status) === 10;
    }
    if (activeTab.value === "paid") {
      return Number(order.payStatus) === 1 && Number(order.status) === 20;
    }
    if (activeTab.value === "refunded") {
      return Number(order.status) === 60;
    }
    if (activeTab.value === "cancelled") {
      return Number(order.status) === 50;
    }
    return true;
  });
});

onMounted(loadPage);

async function loadPage() {
  error.value = "";
  try {
    const [current, orderList] = await Promise.all([me(), listOrders()]);
    setUser(current);
    user.value = current;
    orders.value = orderList || [];
  } catch (e) {
    error.value = e.message || "获取用户信息失败";
  }
}

function logout() {
  clearAuth();
  router.push("/login");
}

function selectTab(tab) {
  activeTab.value = tab;
  router.replace({ path: "/profile", query: tab === "all" ? {} : { tab } });
}

async function handleCancel(order) {
  actionLoading.value = `cancel-${order.orderNo}`;
  error.value = "";
  success.value = "";
  try {
    await cancelOrder(order.orderNo);
    await loadPage();
    success.value = "订单已取消";
  } catch (e) {
    error.value = e.message || "取消订单失败";
  } finally {
    actionLoading.value = "";
  }
}

async function handlePay(order) {
  actionLoading.value = `pay-${order.orderNo}`;
  error.value = "";
  success.value = "";
  try {
    const payment = await createPayment(order.orderNo, order.payAmount || order.totalAmount, 1);
    await mockPaySuccess(payment.paymentNo);
    await loadPage();
    success.value = "订单已支付完成";
  } catch (e) {
    error.value = e.message || "支付订单失败";
  } finally {
    actionLoading.value = "";
  }
}

async function handleRefund(order) {
  actionLoading.value = `refund-${order.orderNo}`;
  error.value = "";
  success.value = "";
  try {
    await refundOrder(order.orderNo);
    await loadPage();
    success.value = "退款已完成";
  } catch (e) {
    error.value = e.message || "退款失败";
  } finally {
    actionLoading.value = "";
  }
}

function formatAmount(value) {
  return Number(value || 0).toFixed(2);
}

function orderStatusText(order) {
  if (Number(order.status) === 60) {
    return "已退款";
  }
  if (Number(order.payStatus) === 1) {
    return "已支付";
  }
  if (Number(order.status) === 50) {
    return "已取消";
  }
  return "待支付";
}
</script>

<template>
  <section v-if="user" class="profile-shell">
    <div class="profile-card profile-summary">
      <div>
        <p>个人中心</p>
        <h1>{{ user.nickname || user.username }}</h1>
        <span>{{ user.phone || user.username }}</span>
      </div>
      <button type="button" @click="logout">退出登录</button>
    </div>

    <div class="profile-grid">
      <article class="profile-card">
        <p>账号信息</p>
        <ul>
          <li>
            <strong>手机号</strong>
            <span>{{ user.phone || user.username }}</span>
          </li>
          <li>
            <strong>用户 ID</strong>
            <span>{{ user.userId }}</span>
          </li>
          <li>
            <strong>角色</strong>
            <span>{{ (user.roles || []).join(" / ") || "ROLE_USER" }}</span>
          </li>
        </ul>
      </article>

      <article class="profile-card">
        <p>当前状态</p>
        <div class="profile-status">
          <strong>可继续支付 / 取消 / 退款</strong>
          <span>未支付订单会留在这里，支付成功后也可以从订单详情页继续查看和发起退款。</span>
        </div>
      </article>
    </div>

    <section class="profile-card">
      <div class="profile-orders-head">
        <div>
          <p>我的订单</p>
          <h2>订单中心</h2>
        </div>
        <span>{{ filteredOrders.length }} 笔订单</span>
      </div>

      <div class="profile-tabs">
        <button
          v-for="tab in tabs"
          :key="tab.key"
          type="button"
          :class="{ active: activeTab === tab.key }"
          @click="selectTab(tab.key)"
        >
          {{ tab.label }}
        </button>
      </div>

      <div v-if="filteredOrders.length" class="profile-order-list">
        <article v-for="order in filteredOrders" :key="order.orderNo" class="profile-order-card">
          <div class="profile-order-top">
            <div>
              <strong>{{ order.orderNo }}</strong>
              <span>{{ orderStatusText(order) }}</span>
            </div>
            <b>¥{{ formatAmount(order.payAmount || order.totalAmount) }}</b>
          </div>

          <ul>
            <li v-for="item in order.items" :key="`${order.orderNo}-${item.skuId}`">
              <span>{{ item.skuTitle }}</span>
              <span>x{{ item.quantity }}</span>
            </li>
          </ul>

          <div class="profile-order-actions">
            <RouterLink :to="`/orders/${order.orderNo}`">查看详情</RouterLink>
            <button
              v-if="Number(order.payStatus) !== 1 && Number(order.status) === 10"
              type="button"
              @click="handlePay(order)"
              :disabled="actionLoading === `pay-${order.orderNo}`"
            >
              {{ actionLoading === `pay-${order.orderNo}` ? "支付中..." : "立即支付" }}
            </button>
            <button
              v-if="Number(order.payStatus) !== 1 && Number(order.status) === 10"
              type="button"
              class="ghost"
              @click="handleCancel(order)"
              :disabled="actionLoading === `cancel-${order.orderNo}`"
            >
              {{ actionLoading === `cancel-${order.orderNo}` ? "处理中..." : "取消订单" }}
            </button>
            <button
              v-if="Number(order.payStatus) === 1 && Number(order.status) === 20"
              type="button"
              class="ghost"
              @click="handleRefund(order)"
              :disabled="actionLoading === `refund-${order.orderNo}`"
            >
              {{ actionLoading === `refund-${order.orderNo}` ? "处理中..." : "申请退款" }}
            </button>
          </div>
        </article>
      </div>

      <p v-else class="ly-muted">当前筛选下还没有订单记录。</p>
    </section>
  </section>

  <p v-if="success" class="ly-success">{{ success }}</p>
  <p v-if="error" class="ly-error">{{ error }}</p>
</template>

<style scoped>
.profile-shell {
  display: grid;
  gap: 18px;
}

.profile-card {
  padding: 26px;
  border-radius: 28px;
  background: rgba(255, 252, 248, 0.92);
  box-shadow: 0 20px 40px rgba(71, 44, 25, 0.08);
}

.profile-summary {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: 18px;
}

.profile-summary p,
.profile-card p,
.profile-summary span,
.profile-orders-head span {
  color: rgba(36, 25, 18, 0.62);
}

.profile-summary h1,
.profile-orders-head h2 {
  margin: 0;
  color: #241912;
}

.profile-summary h1 {
  font-size: 42px;
}

.profile-orders-head h2 {
  font-size: 32px;
}

.profile-summary button,
.profile-order-actions button,
.profile-order-actions a,
.profile-tabs button {
  min-height: 44px;
  padding: 0 18px;
  border: 0;
  border-radius: 999px;
  cursor: pointer;
}

.profile-summary button,
.profile-order-actions button:not(.ghost) {
  background: linear-gradient(135deg, #ff6d4d, #ff8d57);
  color: #fff9f4;
}

.profile-order-actions .ghost,
.profile-tabs button,
.profile-order-actions a {
  background: rgba(255, 109, 77, 0.1);
  color: #a4562d;
  text-decoration: none;
}

.profile-tabs button.active {
  background: linear-gradient(135deg, #ff6d4d, #ff8d57);
  color: #fff9f4;
}

.profile-grid {
  display: grid;
  grid-template-columns: 1.2fr 0.8fr;
  gap: 18px;
}

.profile-card ul {
  list-style: none;
  padding: 0;
  margin: 0;
  display: grid;
  gap: 12px;
}

.profile-card li {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  padding: 14px 0;
  border-bottom: 1px solid rgba(90, 56, 35, 0.08);
}

.profile-card li:last-child {
  border-bottom: 0;
}

.profile-status {
  padding: 18px;
  border-radius: 22px;
  background: rgba(255, 109, 77, 0.08);
}

.profile-status strong {
  display: block;
  font-size: 28px;
  margin-bottom: 8px;
}

.profile-status span {
  color: rgba(36, 25, 18, 0.68);
  line-height: 1.8;
}

.profile-orders-head,
.profile-order-top,
.profile-order-actions,
.profile-tabs {
  display: flex;
  align-items: center;
  gap: 12px;
}

.profile-orders-head {
  justify-content: space-between;
}

.profile-tabs {
  flex-wrap: wrap;
  margin: 18px 0;
}

.profile-order-list {
  display: grid;
  gap: 14px;
}

.profile-order-card {
  padding: 18px;
  border-radius: 22px;
  background: rgba(255, 255, 255, 0.82);
  box-shadow: inset 0 0 0 1px rgba(90, 56, 35, 0.06);
}

.profile-order-top {
  justify-content: space-between;
}

.profile-order-top strong,
.profile-order-top b {
  color: #241912;
}

.profile-order-top strong {
  display: block;
  margin-bottom: 6px;
}

.profile-order-top span {
  color: rgba(36, 25, 18, 0.62);
}

.profile-order-actions {
  justify-content: flex-end;
  flex-wrap: wrap;
}

@media (max-width: 900px) {
  .profile-summary,
  .profile-grid,
  .profile-orders-head,
  .profile-order-top {
    grid-template-columns: 1fr;
    display: grid;
  }

  .profile-order-actions {
    justify-content: flex-start;
  }
}
</style>
