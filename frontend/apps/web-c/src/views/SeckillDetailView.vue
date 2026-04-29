<script setup>
import { computed, onBeforeUnmount, onMounted, ref, watch } from "vue";
import { useRoute, useRouter } from "vue-router";
import {
  fetchCurrentSeckillRequest,
  fetchSeckillDetail,
  fetchSeckillRequestRecord,
  formatDateTime,
  resolveSeckillActivityStatus,
  resolveSeckillRequestStatus,
  SeckillActivityStatus,
  SeckillRequestStatus,
  submitSeckillRequest
} from "@shared";

const route = useRoute();
const router = useRouter();

const detail = ref(null);
const loading = ref(false);
const actionLoading = ref(false);
const error = ref("");
const requestHint = ref("");
const requestRecord = ref(null);
const now = ref(Date.now());

let countdownTimer = null;
let pollingTimer = null;
let pollingCount = 0;

const activityStatus = computed(() =>
  detail.value ? resolveSeckillActivityStatus(detail.value, now.value) : SeckillActivityStatus.ENDED
);

const requestStatus = computed(() =>
  requestRecord.value ? resolveSeckillRequestStatus(requestRecord.value.status) : SeckillRequestStatus.IDLE
);

const currentButtonText = computed(() => {
  if (actionLoading.value) {
    return "提交中...";
  }
  if (requestStatus.value === SeckillRequestStatus.QUEUEING) {
    return "排队中，请等待结果";
  }
  if (requestStatus.value === SeckillRequestStatus.SUCCESS) {
    return "秒杀成功，去个人中心查看";
  }
  if (requestStatus.value === SeckillRequestStatus.FAILED) {
    return "秒杀失败，重新尝试";
  }

  switch (activityStatus.value) {
    case SeckillActivityStatus.UPCOMING:
      return "即将开始";
    case SeckillActivityStatus.SOLD_OUT:
      return "已抢光";
    case SeckillActivityStatus.ENDED:
      return "活动已结束";
    default:
      return "立即秒杀";
  }
});

const buttonDisabled = computed(() => {
  if (actionLoading.value || requestStatus.value === SeckillRequestStatus.QUEUEING) {
    return true;
  }
  if (requestStatus.value === SeckillRequestStatus.SUCCESS || requestStatus.value === SeckillRequestStatus.FAILED) {
    return false;
  }
  return activityStatus.value !== SeckillActivityStatus.ACTIVE;
});

const countdownLabel = computed(() => {
  if (activityStatus.value === SeckillActivityStatus.UPCOMING) {
    return "距离开始";
  }
  if (activityStatus.value === SeckillActivityStatus.ACTIVE) {
    return "距离结束";
  }
  return "倒计时";
});

const countdownText = computed(() => {
  if (!detail.value) {
    return "--";
  }

  const targetTime =
    activityStatus.value === SeckillActivityStatus.UPCOMING
      ? new Date(detail.value.startTime).getTime()
      : activityStatus.value === SeckillActivityStatus.ACTIVE
        ? new Date(detail.value.endTime).getTime()
        : 0;

  if (!targetTime || Number.isNaN(targetTime)) {
    return "--";
  }

  const diff = Math.max(targetTime - now.value, 0);
  const totalSeconds = Math.floor(diff / 1000);
  const days = Math.floor(totalSeconds / 86400);
  const hours = Math.floor((totalSeconds % 86400) / 3600);
  const minutes = Math.floor((totalSeconds % 3600) / 60);
  const seconds = totalSeconds % 60;

  if (days + hours + minutes + seconds === 0) {
    return activityStatus.value === SeckillActivityStatus.UPCOMING ? "即将开始" : "活动已结束";
  }

  const dayText = days > 0 ? `${days}天 ` : "";
  return `${dayText}${String(hours).padStart(2, "0")}:${String(minutes).padStart(2, "0")}:${String(seconds).padStart(2, "0")}`;
});

watch(
  () => route.fullPath,
  () => {
    loadDetail();
  },
  { immediate: true }
);

onMounted(() => {
  countdownTimer = window.setInterval(() => {
    now.value = Date.now();
  }, 1000);
});

onBeforeUnmount(() => {
  stopPolling();
  if (countdownTimer) {
    window.clearInterval(countdownTimer);
    countdownTimer = null;
  }
});

async function loadDetail() {
  stopPolling();
  loading.value = true;
  error.value = "";
  requestHint.value = "";
  requestRecord.value = null;

  try {
    const [detailData, currentRecord] = await Promise.all([
      fetchSeckillDetail(route.params.activityId, route.params.activitySkuId),
      fetchCurrentSeckillRequest(route.params.activityId, route.params.activitySkuId)
    ]);

    detail.value = detailData;
    requestRecord.value = currentRecord || null;
    syncRequestHint();

    if (requestRecord.value && requestStatus.value === SeckillRequestStatus.QUEUEING) {
      startPolling(requestRecord.value.recordId, true);
    }
  } catch (e) {
    detail.value = null;
    error.value = e.message || "秒杀详情加载失败";
  } finally {
    loading.value = false;
  }
}

async function handlePrimaryAction() {
  if (requestStatus.value === SeckillRequestStatus.SUCCESS) {
    router.push("/profile");
    return;
  }

  if (buttonDisabled.value && requestStatus.value !== SeckillRequestStatus.FAILED) {
    return;
  }

  await submitRequest();
}

async function submitRequest() {
  if (!detail.value) {
    return;
  }

  actionLoading.value = true;
  error.value = "";

  try {
    const result = await submitSeckillRequest(detail.value.activityId, detail.value.activitySkuId);
    requestRecord.value = {
      recordId: result?.recordId || null,
      status: result?.status ?? 2,
      statusText: result?.message || ""
    };
    syncRequestHint();

    if (requestRecord.value.recordId && requestStatus.value === SeckillRequestStatus.QUEUEING) {
      startPolling(requestRecord.value.recordId);
    }
  } catch (e) {
    error.value = e.message || "秒杀请求失败";
  } finally {
    actionLoading.value = false;
  }
}

function startPolling(recordId, resume = false) {
  if (!recordId) {
    return;
  }

  stopPolling();
  pollingCount = 0;
  if (resume) {
    requestHint.value = "当前商品已有秒杀请求，正在继续查询处理结果。";
  }

  pollingTimer = window.setInterval(async () => {
    pollingCount += 1;
    try {
      const latest = await fetchSeckillRequestRecord(recordId);
      requestRecord.value = latest;
      syncRequestHint();

      if (requestStatus.value === SeckillRequestStatus.SUCCESS || requestStatus.value === SeckillRequestStatus.FAILED) {
        stopPolling();
        return;
      }

      if (pollingCount >= 30) {
        requestHint.value = "排队结果暂未返回，请稍后前往个人中心查看。";
        stopPolling();
      }
    } catch (e) {
      error.value = e.message || "秒杀状态查询失败";
      stopPolling();
    }
  }, 2000);
}

function stopPolling() {
  if (pollingTimer) {
    window.clearInterval(pollingTimer);
    pollingTimer = null;
  }
  pollingCount = 0;
}

function syncRequestHint() {
  if (requestStatus.value === SeckillRequestStatus.QUEUEING) {
    requestHint.value = "已进入秒杀排队，请等待系统返回处理结果。";
    return;
  }
  if (requestStatus.value === SeckillRequestStatus.SUCCESS) {
    requestHint.value = "秒杀成功，请前往个人中心查看订单并完成后续支付。";
    return;
  }
  if (requestStatus.value === SeckillRequestStatus.FAILED) {
    requestHint.value = requestRecord.value?.statusText || "本次秒杀未成功，可以重新尝试。";
    return;
  }

  switch (activityStatus.value) {
    case SeckillActivityStatus.UPCOMING:
      requestHint.value = "活动尚未开始，开始后按钮会自动可点击。";
      break;
    case SeckillActivityStatus.SOLD_OUT:
      requestHint.value = "当前场次库存已抢完，请关注后续活动。";
      break;
    case SeckillActivityStatus.ENDED:
      requestHint.value = "活动已经结束，请返回秒杀列表查看其他场次。";
      break;
    default:
      requestHint.value = "库存充足时可直接发起秒杀，系统会自动进入排队处理。";
      break;
  }
}

function statusClass(status) {
  switch (status) {
    case SeckillActivityStatus.SOLD_OUT:
      return "sold-out";
    case SeckillActivityStatus.ENDED:
      return "ended";
    case SeckillActivityStatus.UPCOMING:
      return "upcoming";
    default:
      return "active";
  }
}

function heroStatusText(status) {
  switch (status) {
    case SeckillActivityStatus.UPCOMING:
      return "未开始";
    case SeckillActivityStatus.SOLD_OUT:
      return "已抢光";
    case SeckillActivityStatus.ENDED:
      return "已结束";
    default:
      return "进行中";
  }
}
</script>

<template>
  <section v-if="detail" class="seckill-detail-shell">
    <header class="seckill-detail-head">
      <div>
        <p>秒杀详情</p>
        <h1>{{ detail.name }}</h1>
        <span>{{ detail.title }}</span>
      </div>
      <button type="button" class="ghost-btn" @click="router.push('/seckill')">返回秒杀列表</button>
    </header>

    <div class="seckill-detail-layout">
      <section class="seckill-detail-main">
        <article class="detail-card detail-hero">
          <div class="detail-hero-image-shell">
            <img :src="detail.image" :alt="detail.name" class="detail-hero-image" />
            <span class="detail-hero-status" :class="statusClass(activityStatus)">
              {{ heroStatusText(activityStatus) }}
            </span>
          </div>

          <div class="detail-hero-copy">
            <div class="detail-price-row">
              <div class="highlight">
                <span>秒杀价</span>
                <strong>￥{{ detail.seckillPrice.toFixed(2) }}</strong>
              </div>
              <div>
                <span>原价</span>
                <b>￥{{ detail.originPrice.toFixed(2) }}</b>
              </div>
            </div>

            <dl class="detail-meta-grid">
              <div>
                <dt>剩余库存</dt>
                <dd>{{ detail.stockAvailable }}</dd>
              </div>
              <div>
                <dt>每人限购</dt>
                <dd>{{ detail.limitPerUser }}</dd>
              </div>
              <div>
                <dt>活动开始</dt>
                <dd>{{ formatDateTime(detail.startTime) }}</dd>
              </div>
              <div>
                <dt>活动结束</dt>
                <dd>{{ formatDateTime(detail.endTime) }}</dd>
              </div>
            </dl>
          </div>
        </article>

        <article class="detail-card">
          <div class="detail-card-head">
            <div>
              <p>{{ countdownLabel }}</p>
              <h2>{{ countdownText }}</h2>
            </div>
            <button type="button" class="primary-btn" :disabled="buttonDisabled" @click="handlePrimaryAction">
              {{ currentButtonText }}
            </button>
          </div>

          <div class="detail-notice" :class="requestStatus">
            <strong>
              {{
                requestStatus === SeckillRequestStatus.SUCCESS
                  ? "秒杀成功"
                  : requestStatus === SeckillRequestStatus.FAILED
                    ? "秒杀失败"
                    : requestStatus === SeckillRequestStatus.QUEUEING
                      ? "排队处理中"
                      : "活动提示"
              }}
            </strong>
            <span>{{ requestHint }}</span>
          </div>
        </article>
      </section>

      <aside class="seckill-detail-side">
        <article class="detail-card">
          <p>秒杀规则</p>
          <ul class="rule-list">
            <li v-for="rule in detail.rules" :key="rule">{{ rule }}</li>
          </ul>
        </article>

        <article v-if="requestRecord?.recordId" class="detail-card">
          <p>当前请求</p>
          <div class="request-row">
            <span>请求编号</span>
            <b>{{ requestRecord.recordId }}</b>
          </div>
          <div class="request-row">
            <span>处理状态</span>
            <b>{{ requestRecord.statusText || "处理中" }}</b>
          </div>
          <button
            v-if="requestStatus === SeckillRequestStatus.SUCCESS"
            type="button"
            class="ghost-btn full-width"
            @click="router.push('/profile')"
          >
            去个人中心查看
          </button>
        </article>
      </aside>
    </div>
  </section>

  <p v-if="loading" class="ly-muted">秒杀详情加载中...</p>
  <p v-if="error" class="ly-error">{{ error }}</p>
</template>

<style scoped>
.seckill-detail-shell {
  display: grid;
  gap: 18px;
}

.seckill-detail-head,
.detail-card {
  padding: 28px;
  border-radius: 32px;
  background: rgba(255, 252, 248, 0.94);
  box-shadow: 0 20px 40px rgba(71, 44, 25, 0.08);
}

.seckill-detail-head {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  gap: 18px;
}

.seckill-detail-head p,
.seckill-detail-head span,
.detail-card p,
.detail-price-row span,
.detail-meta-grid dt,
.detail-notice span,
.rule-list li,
.request-row span {
  color: rgba(36, 25, 18, 0.62);
}

.seckill-detail-head h1,
.detail-card h2,
.detail-price-row strong,
.detail-price-row b,
.detail-meta-grid dd,
.detail-notice strong,
.request-row b {
  margin: 0;
  color: #241912;
}

.seckill-detail-head h1 {
  font-size: 42px;
}

.ghost-btn,
.primary-btn {
  min-height: 46px;
  padding: 0 20px;
  border: 0;
  border-radius: 999px;
  cursor: pointer;
  font: inherit;
  font-weight: 700;
}

.ghost-btn {
  background: rgba(255, 109, 77, 0.1);
  color: #a4562d;
}

.primary-btn {
  background: linear-gradient(135deg, #ff6d4d, #ff8d57);
  color: #fff9f4;
}

.primary-btn:disabled {
  cursor: not-allowed;
  opacity: 0.55;
}

.full-width {
  width: 100%;
}

.seckill-detail-layout {
  display: grid;
  grid-template-columns: minmax(0, 1.1fr) 360px;
  gap: 18px;
}

.seckill-detail-main,
.seckill-detail-side {
  display: grid;
  gap: 18px;
}

.detail-hero {
  display: grid;
  grid-template-columns: minmax(280px, 0.9fr) minmax(0, 1.1fr);
  gap: 18px;
}

.detail-hero-image-shell {
  position: relative;
  min-height: 360px;
  border-radius: 26px;
  overflow: hidden;
  background: linear-gradient(180deg, #fff8f0 0%, #f7eadf 100%);
}

.detail-hero-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}

.detail-hero-status {
  position: absolute;
  left: 18px;
  top: 18px;
  min-height: 38px;
  padding: 0 14px;
  border-radius: 999px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-size: 13px;
  font-weight: 700;
}

.detail-hero-status.active {
  background: rgba(111, 168, 95, 0.16);
  color: #49733f;
}

.detail-hero-status.sold-out,
.detail-hero-status.ended {
  background: rgba(196, 81, 70, 0.12);
  color: #b34d43;
}

.detail-hero-status.upcoming {
  background: rgba(255, 109, 77, 0.12);
  color: #c46229;
}

.detail-hero-copy {
  display: grid;
  align-content: start;
  gap: 18px;
}

.detail-price-row,
.detail-card-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
}

.detail-price-row {
  align-items: stretch;
}

.detail-price-row div {
  flex: 1;
  padding: 18px;
  border-radius: 24px;
  background: rgba(255, 255, 255, 0.82);
  box-shadow: inset 0 0 0 1px rgba(90, 56, 35, 0.06);
}

.detail-price-row .highlight {
  background: linear-gradient(135deg, rgba(255, 109, 77, 0.12), rgba(255, 176, 120, 0.16));
}

.detail-price-row strong {
  display: block;
  margin-top: 10px;
  font-size: 34px;
  color: #c44f35;
}

.detail-price-row b {
  display: block;
  margin-top: 10px;
  font-size: 24px;
  text-decoration: line-through;
  color: rgba(36, 25, 18, 0.46);
}

.detail-meta-grid {
  margin: 0;
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
}

.detail-meta-grid div {
  padding: 18px;
  border-radius: 24px;
  background: rgba(255, 109, 77, 0.06);
}

.detail-meta-grid dd {
  margin: 10px 0 0;
  font-size: 18px;
  line-height: 1.5;
}

.detail-card-head h2 {
  font-size: 36px;
}

.detail-notice {
  margin-top: 18px;
  padding: 18px;
  border-radius: 24px;
  background: rgba(255, 255, 255, 0.82);
  display: grid;
  gap: 8px;
}

.detail-notice.queueing {
  background: rgba(255, 109, 77, 0.08);
}

.detail-notice.success {
  background: rgba(111, 168, 95, 0.12);
}

.detail-notice.failed {
  background: rgba(196, 81, 70, 0.1);
}

.rule-list {
  margin: 0;
  padding-left: 18px;
  display: grid;
  gap: 10px;
}

.request-row {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  padding: 12px 0;
  border-bottom: 1px solid rgba(90, 56, 35, 0.08);
}

.request-row:last-of-type {
  border-bottom: 0;
}

@media (max-width: 1080px) {
  .seckill-detail-layout,
  .detail-hero {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 760px) {
  .seckill-detail-head,
  .detail-card-head,
  .detail-price-row,
  .detail-meta-grid {
    display: grid;
    grid-template-columns: 1fr;
  }
}
</style>
