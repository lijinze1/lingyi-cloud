<script setup>
import { computed, onBeforeUnmount, onMounted, ref } from "vue";
import { RouterLink, useRoute, useRouter } from "vue-router";
import {
  attemptSeckill,
  cancelOrder,
  createPayment,
  getSeckillRecord,
  listCurrentSeckillActivities,
  listOrders,
  mockPaySuccess
} from "@shared";

const route = useRoute();
const router = useRouter();
const loading = ref(false);
const acting = ref(false);
const error = ref("");
const success = ref("");
const record = ref(null);
const order = ref(null);

let timer = null;

const displayStatus = computed(() => {
  if (order.value) {
    if (Number(order.value.status) === 60) {
      return 60;
    }
    if (Number(order.value.payStatus) === 1) {
      return 3;
    }
    if (Number(order.value.status) === 50) {
      return 4;
    }
    if (Number(order.value.status) === 10) {
      return 1;
    }
  }
  return Number(record.value?.status ?? -1);
});

const statusText = computed(() => {
  switch (displayStatus.value) {
    case 0:
      return "排队处理中";
    case 1:
      return "待支付";
    case 2:
      return "已失败";
    case 3:
      return "已支付";
    case 4:
      return "已取消";
    case 60:
      return "已退款";
    default:
      return record.value?.statusText || "处理中";
  }
});

const canPay = computed(() =>
  Boolean(order.value?.orderNo) && Number(order.value?.status) === 10 && Number(order.value?.payStatus) !== 1
);
const canRetry = computed(() => [2, 4].includes(displayStatus.value));

onMounted(() => {
  loadPage();
});

onBeforeUnmount(() => {
  stopPolling();
});

async function loadPage() {
  loading.value = true;
  error.value = "";
  try {
    const currentRecord = await getSeckillRecord(route.params.recordId);
    record.value = currentRecord;
    await syncOrder(currentRecord);
    if (displayStatus.value === 0) {
      startPolling();
    } else {
      stopPolling();
    }
  } catch (e) {
    error.value = e.message || "秒杀订单加载失败";
  } finally {
    loading.value = false;
  }
}

async function syncOrder(currentRecord) {
  if (!currentRecord?.recordId) {
    order.value = null;
    return;
  }
  const expectedRemark = `SECKILL:${currentRecord.activityId}:`;
  const orders = await listOrders();
  order.value = (orders || [])
    .filter((item) => Number(item.orderType) === 1)
    .filter((item) => typeof item.remark === "string" && item.remark.startsWith(expectedRemark))
    .filter((item) => typeof item.remark === "string" && item.remark.endsWith(`:${currentRecord.recordId}`))
    .sort((left, right) => String(right.createdAt || "").localeCompare(String(left.createdAt || "")))[0] || null;
}

function startPolling() {
  if (timer) {
    return;
  }
  timer = window.setInterval(async () => {
    try {
      const latest = await getSeckillRecord(route.params.recordId);
      record.value = latest;
      await syncOrder(latest);
      if (displayStatus.value !== 0) {
        stopPolling();
      }
    } catch (e) {
      stopPolling();
      error.value = e.message || "秒杀状态刷新失败";
    }
  }, 2000);
}

function stopPolling() {
  if (timer) {
    window.clearInterval(timer);
    timer = null;
  }
}

async function handlePay() {
  if (!order.value?.orderNo) {
    error.value = "当前秒杀订单还没有可支付的订单号";
    return;
  }
  acting.value = true;
  error.value = "";
  success.value = "";
  try {
    const payment = await createPayment(order.value.orderNo, order.value.payAmount || order.value.totalAmount, 1);
    await mockPaySuccess(payment.paymentNo);
    await loadPage();
    success.value = "秒杀订单支付成功";
  } catch (e) {
    error.value = e.message || "秒杀订单支付失败";
  } finally {
    acting.value = false;
  }
}

async function handleCancel() {
  if (!order.value?.orderNo) {
    error.value = "当前秒杀订单无法取消";
    return;
  }
  acting.value = true;
  error.value = "";
  success.value = "";
  try {
    await cancelOrder(order.value.orderNo);
    await loadPage();
    success.value = "秒杀订单已取消，可以重新发起秒杀";
  } catch (e) {
    error.value = e.message || "取消秒杀订单失败";
  } finally {
    acting.value = false;
  }
}

async function handleRetry() {
  if (!record.value?.activityId || !record.value?.skuId) {
    router.push("/");
    return;
  }
  acting.value = true;
  error.value = "";
  success.value = "";
  try {
    const activities = await listCurrentSeckillActivities();
    const matched = (activities || [])
      .flatMap((activity) => (activity.skus || []).map((sku) => ({ activityId: activity.id, ...sku })))
      .find(
        (item) =>
          String(item.activityId) === String(record.value.activityId) &&
          String(item.skuId) === String(record.value.skuId)
      );
    if (!matched) {
      throw new Error("当前秒杀活动已结束，无法重新发起");
    }
    const next = await attemptSeckill(matched.activityId, matched.activitySkuId);
    if (next?.recordId) {
      router.replace(`/seckill/${next.recordId}`);
      return;
    }
    error.value = next?.message || "重新秒杀失败";
  } catch (e) {
    error.value = e.message || "重新秒杀失败";
  } finally {
    acting.value = false;
  }
}

function formatAmount(value) {
  return Number(value || 0).toFixed(2);
}
</script>

<template>
  <section class="seckill-shell">
    <div class="seckill-head">
      <div>
        <p>秒杀订单</p>
        <h1>专属抢购流程</h1>
      </div>
      <RouterLink to="/">返回首页</RouterLink>
    </div>

    <div v-if="record" class="seckill-layout">
      <article class="seckill-card">
        <small>当前状态</small>
        <strong>{{ statusText }}</strong>
        <p v-if="displayStatus === 0">你已抢到购买资格，系统正在生成订单，页面会自动刷新。</p>
        <p v-else-if="displayStatus === 1">秒杀订单已创建，先去支付；支付后仍然可以在个人中心查看并申请退款。</p>
        <p v-else-if="displayStatus === 3">秒杀订单已支付完成，售后与退款入口在订单详情页。</p>
        <p v-else-if="displayStatus === 4">这笔秒杀订单已取消，你可以重新发起秒杀。</p>
        <p v-else-if="displayStatus === 60">这笔秒杀订单已经退款完成，详情可在订单页继续查看。</p>
        <p v-else>本次秒杀没有成功，可以重新尝试。</p>

        <div class="seckill-meta">
          <span>记录 ID</span>
          <b>{{ record.recordId }}</b>
        </div>
        <div class="seckill-meta">
          <span>活动 ID</span>
          <b>{{ record.activityId }}</b>
        </div>
        <div class="seckill-meta">
          <span>SKU ID</span>
          <b>{{ record.skuId }}</b>
        </div>
      </article>

      <article class="seckill-card">
        <small>订单信息</small>
        <template v-if="order">
          <strong>{{ order.orderNo }}</strong>
          <div class="seckill-total">
            <span>订单状态</span>
            <b>{{ statusText }}</b>
          </div>
          <ul class="seckill-items">
            <li v-for="item in order.items" :key="`${order.orderNo}-${item.skuId}`">
              <span>{{ item.skuTitle }}</span>
              <span>x{{ item.quantity }}</span>
            </li>
          </ul>
          <div class="seckill-total">
            <span>支付金额</span>
            <b>￥{{ formatAmount(order.payAmount || order.totalAmount) }}</b>
          </div>

          <div class="seckill-actions">
            <button v-if="canPay" type="button" :disabled="acting" @click="handlePay">
              {{ acting ? "处理中..." : "立即支付" }}
            </button>
            <button v-if="canPay" type="button" class="ghost" :disabled="acting" @click="handleCancel">
              {{ acting ? "处理中..." : "取消订单" }}
            </button>
            <RouterLink v-if="order.orderNo" :to="`/orders/${order.orderNo}`" class="ghost link-btn">
              查看订单详情
            </RouterLink>
            <button v-if="canRetry" type="button" class="ghost" :disabled="acting" @click="handleRetry">
              {{ acting ? "处理中..." : "重新秒杀" }}
            </button>
          </div>
        </template>
        <template v-else>
          <strong>{{ displayStatus === 0 ? "订单生成中" : "暂无关联订单" }}</strong>
          <p>
            {{
              displayStatus === 0
                ? "系统正在为你创建待支付订单，创建完成后这里会直接出现支付和查看详情入口。"
                : "这次秒杀没有生成可支付订单，你可以返回首页重新发起。"
            }}
          </p>
          <div class="seckill-actions">
            <button v-if="canRetry" type="button" class="ghost" :disabled="acting" @click="handleRetry">
              {{ acting ? "处理中..." : "重新秒杀" }}
            </button>
          </div>
        </template>
      </article>
    </div>

    <p v-if="loading" class="ly-muted">秒杀订单加载中...</p>
    <p v-if="success" class="ly-success">{{ success }}</p>
    <p v-if="error" class="ly-error">{{ error }}</p>
  </section>
</template>

<style scoped>
.seckill-shell {
  display: grid;
  gap: 18px;
}

.seckill-head,
.seckill-card {
  padding: 28px;
  border-radius: 32px;
  background: rgba(255, 252, 248, 0.94);
  box-shadow: 0 20px 40px rgba(71, 44, 25, 0.08);
}

.seckill-head {
  display: flex;
  align-items: end;
  justify-content: space-between;
}

.seckill-head p,
.seckill-card small,
.seckill-card p,
.seckill-meta span,
.seckill-items span,
.seckill-total span {
  color: rgba(36, 25, 18, 0.62);
}

.seckill-head h1,
.seckill-card strong,
.seckill-total b {
  margin: 0;
  color: #241912;
}

.seckill-head a,
.seckill-actions button,
.link-btn {
  min-height: 42px;
  padding: 0 16px;
  border: 0;
  border-radius: 999px;
  cursor: pointer;
  text-decoration: none;
}

.seckill-head a,
.seckill-actions .ghost,
.link-btn {
  background: rgba(255, 109, 77, 0.1);
  color: #a4562d;
}

.seckill-actions button:not(.ghost) {
  background: linear-gradient(135deg, #ff6d4d, #ff8d57);
  color: #fff9f4;
}

.seckill-layout {
  display: grid;
  grid-template-columns: 0.9fr 1.1fr;
  gap: 18px;
}

.seckill-card {
  display: grid;
  gap: 16px;
}

.seckill-card strong {
  font-size: 28px;
}

.seckill-meta,
.seckill-total {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.seckill-items {
  display: grid;
  gap: 10px;
  padding: 0;
  margin: 0;
  list-style: none;
}

.seckill-items li {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.seckill-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

@media (max-width: 960px) {
  .seckill-layout {
    grid-template-columns: 1fr;
  }
}
</style>
