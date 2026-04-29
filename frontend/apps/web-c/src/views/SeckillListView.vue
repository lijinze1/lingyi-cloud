<script setup>
import { onMounted, ref } from "vue";
import { useRouter } from "vue-router";
import {
  fetchSeckillList,
  formatDateTime,
  getSeckillListButtonText,
  getSeckillStatusText,
  SeckillActivityStatus
} from "@shared";

const router = useRouter();
const loading = ref(false);
const error = ref("");
const seckillItems = ref([]);

onMounted(loadSeckillList);

async function loadSeckillList() {
  loading.value = true;
  error.value = "";
  try {
    seckillItems.value = await fetchSeckillList();
  } catch (e) {
    error.value = e.message || "秒杀列表加载失败";
  } finally {
    loading.value = false;
  }
}

function openDetail(item) {
  if (item.activityStatus !== SeckillActivityStatus.ACTIVE) {
    return;
  }
  router.push(`/seckill/${item.activityId}/${item.activitySkuId}`);
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
</script>

<template>
  <section class="seckill-list-shell">
    <header class="seckill-list-head">
      <div class="seckill-list-title">
        <p>限时秒杀</p>
        <h1>当前可参与的秒杀场次</h1>
      </div>
      <button type="button" class="ghost-btn" @click="loadSeckillList">刷新活动</button>
    </header>

    <p v-if="loading" class="ly-muted">秒杀活动加载中...</p>
    <p v-else-if="error" class="ly-error">{{ error }}</p>

    <div v-else-if="seckillItems.length" class="seckill-list-grid">
      <article v-for="item in seckillItems" :key="item.activitySkuId" class="seckill-card">
        <div class="seckill-card-image-shell">
          <img :src="item.image" :alt="item.name" class="seckill-card-image" />
          <span class="seckill-card-status" :class="statusClass(item.activityStatus)">
            {{ getSeckillStatusText(item.activityStatus) }}
          </span>
        </div>

        <div class="seckill-card-copy">
          <h2>{{ item.name }}</h2>
          <p>{{ item.title }}</p>
        </div>

        <div class="seckill-card-price">
          <div class="highlight">
            <span>秒杀价</span>
            <strong>￥{{ item.seckillPrice.toFixed(2) }}</strong>
          </div>
          <div>
            <span>原价</span>
            <b>￥{{ item.originPrice.toFixed(2) }}</b>
          </div>
        </div>

        <dl class="seckill-card-meta">
          <div>
            <dt>秒杀库存</dt>
            <dd>{{ item.stockAvailable }}</dd>
          </div>
          <div>
            <dt>每人限购</dt>
            <dd>{{ item.limitPerUser }}</dd>
          </div>
          <div>
            <dt>开始时间</dt>
            <dd>{{ formatDateTime(item.startTime) }}</dd>
          </div>
          <div>
            <dt>结束时间</dt>
            <dd>{{ formatDateTime(item.endTime) }}</dd>
          </div>
        </dl>

        <button
          type="button"
          class="seckill-card-action"
          :disabled="item.activityStatus !== SeckillActivityStatus.ACTIVE"
          @click="openDetail(item)"
        >
          {{ getSeckillListButtonText(item.activityStatus) }}
        </button>
      </article>
    </div>

    <section v-else class="seckill-empty">
      <h2>当前还没有可参与的秒杀活动</h2>
      <p>稍后刷新看看，新的活动上架后会直接出现在这里。</p>
    </section>
  </section>
</template>

<style scoped>
.seckill-list-shell {
  display: grid;
  gap: 18px;
}

.seckill-list-head,
.seckill-card,
.seckill-empty {
  padding: 28px;
  border-radius: 32px;
  background: rgba(255, 252, 248, 0.94);
  box-shadow: 0 20px 40px rgba(71, 44, 25, 0.08);
}

.seckill-list-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 18px;
}

.seckill-list-title {
  display: grid;
  gap: 6px;
}

.seckill-list-head p,
.seckill-card-copy p,
.seckill-card-price span,
.seckill-card-meta dt,
.seckill-empty p {
  margin: 0;
  color: rgba(36, 25, 18, 0.62);
}

.seckill-list-head h1,
.seckill-card h2,
.seckill-card-price strong,
.seckill-card-price b,
.seckill-card-meta dd,
.seckill-empty h2 {
  margin: 0;
  color: #241912;
}

.seckill-list-head h1 {
  font-size: 38px;
}

.ghost-btn,
.seckill-card-action {
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

.seckill-list-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 16px;
}

.seckill-card {
  display: grid;
  gap: 16px;
}

.seckill-card-image-shell {
  position: relative;
  height: 240px;
  border-radius: 24px;
  overflow: hidden;
  background: linear-gradient(180deg, #fff8f0 0%, #f7eadf 100%);
}

.seckill-card-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}

.seckill-card-status {
  position: absolute;
  left: 16px;
  top: 16px;
  min-height: 34px;
  padding: 0 12px;
  border-radius: 999px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-size: 13px;
  font-weight: 700;
}

.seckill-card-status.active {
  background: rgba(111, 168, 95, 0.16);
  color: #49733f;
}

.seckill-card-status.sold-out,
.seckill-card-status.ended {
  background: rgba(196, 81, 70, 0.12);
  color: #b34d43;
}

.seckill-card-status.upcoming {
  background: rgba(255, 109, 77, 0.12);
  color: #c46229;
}

.seckill-card-copy h2 {
  font-size: 26px;
}

.seckill-card-copy p {
  margin-top: 8px;
  line-height: 1.7;
}

.seckill-card-price {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
}

.seckill-card-price div {
  padding: 16px;
  border-radius: 22px;
  background: rgba(255, 255, 255, 0.8);
  box-shadow: inset 0 0 0 1px rgba(90, 56, 35, 0.06);
}

.seckill-card-price .highlight {
  background: linear-gradient(135deg, rgba(255, 109, 77, 0.12), rgba(255, 176, 120, 0.16));
}

.seckill-card-price strong {
  display: block;
  margin-top: 8px;
  font-size: 30px;
  color: #c44f35;
}

.seckill-card-price b {
  display: block;
  margin-top: 8px;
  font-size: 22px;
  text-decoration: line-through;
  color: rgba(36, 25, 18, 0.46);
}

.seckill-card-meta {
  margin: 0;
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
}

.seckill-card-meta div {
  padding: 16px;
  border-radius: 22px;
  background: rgba(255, 109, 77, 0.06);
}

.seckill-card-meta dt {
  font-size: 13px;
}

.seckill-card-meta dd {
  margin-top: 10px;
  font-size: 16px;
  line-height: 1.5;
}

.seckill-card-action {
  background: linear-gradient(135deg, #ff6d4d, #ff8d57);
  color: #fff9f4;
}

.seckill-card-action:disabled {
  cursor: not-allowed;
  opacity: 0.52;
  box-shadow: none;
}

@media (max-width: 1120px) {
  .seckill-list-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 820px) {
  .seckill-list-head {
    display: grid;
    grid-template-columns: 1fr;
  }

  .seckill-list-grid,
  .seckill-card-price,
  .seckill-card-meta {
    grid-template-columns: 1fr;
  }
}
</style>
