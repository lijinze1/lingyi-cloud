<script setup>
import { computed, onMounted, ref } from "vue";
import { useRoute, useRouter } from "vue-router";
import { cancelOrder, createPayment, getOrderDetail, mockPaySuccess, refundOrder } from "@shared";

const route = useRoute();
const router = useRouter();
const loading = ref(false);
const acting = ref(false);
const error = ref("");
const success = ref("");
const order = ref(null);

const canPay = computed(() => order.value && Number(order.value.payStatus) !== 1 && Number(order.value.status) === 10);
const canCancel = computed(() => order.value && Number(order.value.payStatus) !== 1 && Number(order.value.status) === 10);
const canRefund = computed(() => order.value && Number(order.value.payStatus) === 1 && Number(order.value.status) === 20);

onMounted(loadOrder);

async function loadOrder() {
  loading.value = true;
  error.value = "";
  try {
    order.value = await getOrderDetail(route.params.orderNo);
  } catch (e) {
    error.value = e.message || "订单详情加载失败";
  } finally {
    loading.value = false;
  }
}

function orderStatusText(current) {
  if (!current) {
    return "";
  }
  if (Number(current.status) === 60) {
    return "已退款";
  }
  if (Number(current.payStatus) === 1) {
    return "已支付";
  }
  if (Number(current.status) === 50) {
    return "已取消";
  }
  return "待支付";
}

async function handlePay() {
  if (!order.value) {
    return;
  }
  acting.value = true;
  error.value = "";
  success.value = "";
  try {
    const payment = await createPayment(order.value.orderNo, order.value.payAmount || order.value.totalAmount, 1);
    await mockPaySuccess(payment.paymentNo);
    await loadOrder();
    success.value = "订单支付成功";
  } catch (e) {
    error.value = e.message || "订单支付失败";
  } finally {
    acting.value = false;
  }
}

async function handleCancel() {
  if (!order.value) {
    return;
  }
  acting.value = true;
  error.value = "";
  success.value = "";
  try {
    await cancelOrder(order.value.orderNo);
    await loadOrder();
    success.value = "订单已取消";
  } catch (e) {
    error.value = e.message || "取消订单失败";
  } finally {
    acting.value = false;
  }
}

async function handleRefund() {
  if (!order.value) {
    return;
  }
  acting.value = true;
  error.value = "";
  success.value = "";
  try {
    await refundOrder(order.value.orderNo);
    await loadOrder();
    success.value = "退款已完成";
  } catch (e) {
    error.value = e.message || "退款失败";
  } finally {
    acting.value = false;
  }
}

function formatAmount(value) {
  return Number(value || 0).toFixed(2);
}
</script>

<template>
  <section v-if="order" class="order-shell">
    <header class="order-head">
      <div>
        <p>订单详情</p>
        <h1>{{ order.orderNo }}</h1>
      </div>
      <div class="order-head-actions">
        <span class="order-status">{{ orderStatusText(order) }}</span>
        <button type="button" class="ghost-link" @click="router.push('/profile')">返回个人中心</button>
      </div>
    </header>

    <div class="order-layout">
      <section class="order-card">
        <h2>商品信息</h2>
        <article v-for="item in order.items" :key="`${order.orderNo}-${item.skuId}`" class="order-item">
          <div>
            <strong>{{ item.skuTitle }}</strong>
            <span>{{ item.skuAttrsJson }}</span>
          </div>
          <span>x{{ item.quantity }}</span>
          <b>¥{{ formatAmount(item.amount) }}</b>
        </article>
      </section>

      <section class="order-card">
        <h2>收货与支付</h2>
        <div class="order-info-line">
          <span>收货人</span>
          <b>{{ order.receiverName || "秒杀直购用户" }}</b>
        </div>
        <div class="order-info-line">
          <span>手机号</span>
          <b>{{ order.receiverPhone || "13800000000" }}</b>
        </div>
        <div class="order-info-line">
          <span>地址</span>
          <b>{{ order.receiverAddress || "秒杀订单默认收货地址" }}</b>
        </div>
        <div class="order-info-line total">
          <span>应付金额</span>
          <strong>¥{{ formatAmount(order.payAmount || order.totalAmount) }}</strong>
        </div>

        <div class="order-actions">
          <button v-if="canPay" type="button" :disabled="acting" @click="handlePay">
            {{ acting ? "支付中..." : "立即支付" }}
          </button>
          <button v-if="canCancel" type="button" class="ghost" :disabled="acting" @click="handleCancel">
            {{ acting ? "处理中..." : "取消订单" }}
          </button>
          <button v-if="canRefund" type="button" class="ghost" :disabled="acting" @click="handleRefund">
            {{ acting ? "处理中..." : "申请退款" }}
          </button>
        </div>
      </section>
    </div>
  </section>

  <p v-if="loading" class="ly-muted">订单详情加载中...</p>
  <p v-if="success" class="ly-success">{{ success }}</p>
  <p v-if="error" class="ly-error">{{ error }}</p>
</template>

<style scoped>
.order-shell {
  display: grid;
  gap: 18px;
}

.order-head,
.order-card {
  padding: 24px;
  border-radius: 28px;
  background: rgba(255, 252, 248, 0.94);
  box-shadow: 0 20px 40px rgba(71, 44, 25, 0.08);
}

.order-head,
.order-head-actions,
.order-info-line,
.order-actions {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.order-head p,
.order-item span,
.order-status,
.order-info-line span {
  color: rgba(36, 25, 18, 0.62);
}

.ghost-link,
.order-actions button {
  min-height: 42px;
  padding: 0 16px;
  border: 0;
  border-radius: 999px;
  cursor: pointer;
}

.ghost-link,
.order-actions .ghost {
  background: rgba(255, 109, 77, 0.1);
  color: #a4562d;
}

.order-actions button:not(.ghost) {
  background: linear-gradient(135deg, #ff6d4d, #ff8d57);
  color: #fff9f4;
}

.order-layout {
  display: grid;
  grid-template-columns: 1.2fr 0.8fr;
  gap: 18px;
}

.order-card {
  display: grid;
  gap: 14px;
}

.order-item {
  display: grid;
  grid-template-columns: 1fr 60px 100px;
  gap: 12px;
  align-items: center;
  padding: 16px 0;
  border-bottom: 1px solid rgba(90, 56, 35, 0.08);
}

.order-item:last-child {
  border-bottom: 0;
}

.order-item strong,
.order-info-line strong {
  color: #241912;
}

.order-info-line.total strong {
  font-size: 34px;
}

.order-actions {
  justify-content: flex-start;
  flex-wrap: wrap;
}

@media (max-width: 900px) {
  .order-layout,
  .order-head {
    grid-template-columns: 1fr;
    display: grid;
  }

  .order-item {
    grid-template-columns: 1fr;
  }
}
</style>
