<script setup>
import { computed, onMounted, ref } from "vue";
import { RouterLink, useRouter } from "vue-router";
import { getToken, listCurrentSeckillActivities, recommendProducts } from "@shared";
import searchStagePoster from "@shared/assets/search-stage-poster.svg";

const router = useRouter();
const query = ref("");
const diagnosisInput = ref("");
const diagnosisMessages = ref([
  {
    role: "assistant",
    text: "你好，我是灵医问药助手。你可以描述症状，也可以直接搜索药品进入详情页。"
  }
]);
const recommendItems = ref([]);
const flashSaleItems = ref([]);
const loading = ref(false);
const error = ref("");

const isAuthed = computed(() => Boolean(getToken()));
const searchPosterStyle = computed(() => ({
  backgroundImage: `linear-gradient(180deg, rgba(241, 250, 244, 0.12), rgba(223, 239, 228, 0.08)), url(${searchStagePoster})`
}));

onMounted(async () => {
  loading.value = true;
  error.value = "";
  try {
    const [recommendPage, seckillActivities] = await Promise.all([
      recommendProducts({ pageNo: 1, pageSize: 8 }),
      listCurrentSeckillActivities()
    ]);

    recommendItems.value = (recommendPage?.records || []).map(normalizeProductCard);
    flashSaleItems.value = (seckillActivities || [])
      .flatMap((activity) => (activity.skus || []).map((sku) => normalizeSeckillCard(activity, sku)))
      .slice(0, 4);
  } catch (e) {
    error.value = e.message || "首页数据加载失败";
  } finally {
    loading.value = false;
  }
});

function normalizeProductCard(item) {
  return {
    id: item.id,
    name: item.name,
    category: item.categoryName || "常备药品",
    desc: item.subTitle || "适合家庭常备与日常复购",
    image: item.mainImage,
    price: Number(item.minPrice || 0),
    stockAvailable: item.stockAvailable || 0
  };
}

function normalizeSeckillCard(activity, sku) {
  return {
    activityId: activity.id,
    activitySkuId: sku.activitySkuId,
    id: sku.spuId,
    skuId: sku.skuId,
    name: sku.skuTitle || sku.spuName,
    category: sku.spuName || activity.name,
    image: sku.mainImage,
    price: Number(sku.seckillPrice || 0),
    originPrice: Number(sku.originPrice || 0),
    stockAvailable: sku.stockAvailable || 0
  };
}

function createReply(text) {
  if (text.includes("发热") || text.includes("咳") || text.includes("喉咙")) {
    return "如果伴随发热、咳嗽或咽痛加重，建议先记录持续时间和体温变化；症状明显时尽快线下就医。";
  }
  if (text.includes("胃") || text.includes("反酸") || text.includes("胃痛")) {
    return "如果是饭后反酸、胃胀或轻度胃痛，可以先看肠胃类常备药；若持续加重，建议尽快线下就医。";
  }
  return "我可以先帮你做基础判断：补充持续时间、频率，以及你想了解的药品或症状，我再继续细化建议。";
}

function goToLoginForAi() {
  if (!isAuthed.value) {
    router.push({ path: "/login", query: { redirect: "/" } });
  }
}

function submitSearch() {
  const target = router.resolve({
    path: "/products",
    query: query.value.trim() ? { q: query.value.trim(), page: "1" } : { page: "1" }
  }).fullPath;

  if (!isAuthed.value) {
    router.push({ path: "/login", query: { redirect: target } });
    return;
  }

  router.push(target);
}

function sendDiagnosis() {
  if (!isAuthed.value) {
    router.push({ path: "/login", query: { redirect: "/" } });
    return;
  }

  const content = diagnosisInput.value.trim();
  if (!content) {
    return;
  }

  diagnosisMessages.value.push({ role: "user", text: content });
  diagnosisMessages.value.push({ role: "assistant", text: createReply(content) });
  diagnosisInput.value = "";
}

function handleSeckill(item) {
  if (!isAuthed.value) {
    router.push({ path: "/login", query: { redirect: "/" } });
    return;
  }
  router.push({
    path: `/products/${item.id}`,
    query: {
      skuId: String(item.skuId),
      seckill: "1",
      seckillActivityId: String(item.activityId),
      seckillActivitySkuId: String(item.activitySkuId),
      seckillPrice: item.price.toFixed(2),
      originPrice: item.originPrice.toFixed(2)
    }
  });
}
</script>

<template>
  <section class="home-search-stage" :style="searchPosterStyle">
    <div class="home-search-bar">
      <input
        v-model="query"
        type="text"
        placeholder="搜索药品、症状或分类，例如 感冒药 / 退热药 / 肠胃药"
        @keyup.enter="submitSearch"
      />
      <button type="button" @click="submitSearch">搜索</button>
    </div>
  </section>

  <section class="home-ai-panel" @click="goToLoginForAi">
    <div class="home-section-head">
      <div>
        <span>AI 用药助手</span>
        <h2>问症状，也能问药品</h2>
      </div>
      <strong>{{ isAuthed ? "在线问药" : "登录后可用" }}</strong>
    </div>

    <div class="home-ai-list">
      <div
        v-for="(message, index) in diagnosisMessages"
        :key="index"
        class="home-chat-bubble"
        :class="message.role"
      >
        {{ message.text }}
      </div>
    </div>

    <div class="home-ai-form">
      <input
        v-model="diagnosisInput"
        type="text"
        :placeholder="isAuthed ? '输入症状或药品问题' : '登录后使用 AI 问药'"
        @keyup.enter="sendDiagnosis"
        @focus="goToLoginForAi"
      />
      <button type="button" @click.stop="sendDiagnosis">发送</button>
    </div>
  </section>

  <section class="home-flash-panel">
    <div class="home-section-head home-section-head-compact">
      <div>
        <span>限时活动</span>
        <h2>今晚秒杀</h2>
      </div>
      <strong>{{ flashSaleItems.length ? "先看详情再秒杀" : "活动准备中" }}</strong>
    </div>

    <div v-if="flashSaleItems.length" class="home-flash-grid">
      <article v-for="item in flashSaleItems" :key="item.activitySkuId" class="home-flash-card">
        <img :src="item.image" :alt="item.name" />
        <div>
          <span>{{ item.category }}</span>
          <h3>{{ item.name }}</h3>
          <p>秒杀价 ￥{{ item.price.toFixed(2) }}</p>
          <small>原价 ￥{{ item.originPrice.toFixed(2) }}</small>
        </div>
        <button type="button" @click="handleSeckill(item)">查看秒杀详情</button>
      </article>
    </div>
    <p v-else class="ly-muted">当前还没有可用的秒杀药品。</p>
  </section>

  <section class="home-products-panel">
    <div class="home-section-head home-section-head-compact">
      <div>
        <span>推荐药品</span>
        <h2>家庭常备清单</h2>
      </div>
      <RouterLink to="/products">查看全部</RouterLink>
    </div>

    <div v-if="recommendItems.length" class="home-product-grid">
      <article v-for="item in recommendItems" :key="item.id" class="home-product-card">
        <div class="home-product-image-shell">
          <img :src="item.image" :alt="item.name" class="home-product-image" />
        </div>
        <div class="home-product-meta-top">
          <span>{{ item.category }}</span>
          <span>{{ item.stockAvailable > 0 ? "现货" : "缺货" }}</span>
        </div>
        <h3>{{ item.name }}</h3>
        <p>{{ item.desc }}</p>
        <div class="home-product-meta-bottom">
          <strong>￥{{ item.price.toFixed(2) }}</strong>
          <RouterLink :to="`/products/${item.id}`">详情</RouterLink>
        </div>
      </article>
    </div>
    <p v-else-if="!loading" class="ly-muted">当前还没有推荐药品。</p>
  </section>

  <p v-if="error" class="ly-error">{{ error }}</p>
</template>

<style scoped>
.home-search-stage,
.home-ai-panel,
.home-flash-panel,
.home-products-panel,
.home-product-card {
  border-radius: 32px;
  background: rgba(255, 252, 248, 0.95);
  box-shadow: 0 20px 44px rgba(71, 44, 25, 0.08);
}

.home-search-stage {
  min-height: 168px;
  padding: 34px;
  background-position: center;
  background-repeat: no-repeat;
  background-size: cover;
  display: flex;
  align-items: center;
}

.home-search-bar,
.home-ai-form {
  display: grid;
  grid-template-columns: 1fr auto;
  gap: 10px;
  padding: 10px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.94);
  box-shadow:
    0 18px 36px rgba(44, 89, 70, 0.1),
    inset 0 0 0 1px rgba(69, 116, 95, 0.08);
}

 .home-search-bar {
  width: 100%;
}

.home-section-head span {
  color: #d07232;
  letter-spacing: 0.12em;
  font-size: 13px;
}

.home-section-head h2 {
  margin: 10px 0 0;
  color: #241912;
  letter-spacing: -0.05em;
}

.home-search-bar input,
.home-ai-form input {
  border: 0;
  background: transparent;
  padding: 0 16px;
  color: #241912;
  font: inherit;
}

.home-search-bar input:focus,
.home-ai-form input:focus {
  outline: none;
}

.home-search-bar button,
.home-ai-form button,
.home-flash-card button {
  min-height: 52px;
  padding: 0 24px;
  border: 0;
  border-radius: 999px;
  background: linear-gradient(135deg, #ff6d4d, #ff8d57);
  color: #fff9f4;
  font: inherit;
  font-weight: 700;
  cursor: pointer;
  box-shadow: 0 14px 24px rgba(255, 109, 77, 0.2);
}

.home-ai-panel,
.home-flash-panel,
.home-products-panel {
  margin-top: 20px;
  padding: 28px;
}

.home-ai-panel {
  display: grid;
  grid-template-rows: auto minmax(240px, 1fr) auto;
  gap: 20px;
  min-height: 440px;
}

.home-section-head,
.home-product-meta-top,
.home-product-meta-bottom {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.home-section-head h2 {
  font-size: 34px;
}

.home-section-head strong,
.home-product-meta-top span,
.home-product-card p,
.home-flash-card span,
.home-flash-card small {
  color: rgba(36, 25, 18, 0.62);
}

.home-ai-list {
  display: grid;
  align-content: start;
  gap: 14px;
  min-height: 240px;
  padding-top: 6px;
}

.home-chat-bubble {
  display: flex;
  align-items: flex-start;
  max-width: 78%;
  min-height: 96px;
  padding: 20px 22px;
  border-radius: 26px;
  line-height: 1.8;
}

.home-chat-bubble.assistant {
  background: rgba(255, 109, 77, 0.08);
  color: #6a3d26;
}

.home-chat-bubble.user {
  margin-left: auto;
  background: linear-gradient(135deg, #ff6d4d, #ff8d57);
  color: #fff9f4;
}

.home-ai-form {
  grid-template-columns: minmax(0, 1fr) auto;
  align-items: center;
  gap: 14px;
  padding: 8px 8px 8px 18px;
}

.home-ai-form input {
  min-width: 0;
  min-height: 56px;
  padding: 0 8px;
  font-size: 17px;
}

.home-ai-form button {
  min-width: 84px;
  min-height: 56px;
  padding: 0 26px;
}

.home-flash-grid,
.home-product-grid {
  display: grid;
  gap: 14px;
}

.home-flash-grid {
  grid-template-columns: repeat(4, minmax(0, 1fr));
}

.home-flash-card {
  padding: 18px;
  border-radius: 24px;
  background: rgba(255, 255, 255, 0.76);
  display: grid;
  gap: 14px;
}

.home-flash-card img,
.home-product-image-shell {
  border-radius: 20px;
  overflow: hidden;
  background: linear-gradient(180deg, #fff8f0 0%, #f7eadf 100%);
}

.home-flash-card img {
  width: 100%;
  height: 180px;
  object-fit: cover;
}

.home-flash-card h3,
.home-product-card h3 {
  margin: 8px 0 6px;
  color: #241912;
}

.home-flash-card p {
  margin: 0;
  font-size: 28px;
  font-weight: 700;
  color: #241912;
}

.home-product-grid {
  grid-template-columns: repeat(4, minmax(0, 1fr));
}

.home-product-card {
  padding: 18px;
}

.home-product-image-shell {
  height: 210px;
  margin-bottom: 14px;
}

.home-product-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}

.home-product-card p {
  min-height: 62px;
  line-height: 1.7;
}

.home-product-meta-bottom strong {
  font-size: 28px;
  color: #241912;
}

.home-product-meta-bottom a,
.home-section-head-compact a {
  color: #a4562d;
  text-decoration: none;
}

@media (max-width: 1080px) {
  .home-flash-grid,
  .home-product-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 760px) {
  .home-flash-grid,
  .home-product-grid {
    grid-template-columns: 1fr;
  }

  .home-ai-panel {
    min-height: auto;
    grid-template-rows: auto auto auto;
  }

  .home-ai-list {
    min-height: 0;
  }

  .home-chat-bubble {
    max-width: 100%;
    min-height: 0;
  }

  .home-ai-form {
    grid-template-columns: 1fr;
    border-radius: 28px;
  }

  .home-ai-form button {
    width: 100%;
  }

  .home-section-head,
  .home-product-meta-top,
  .home-product-meta-bottom {
    display: grid;
  }
}
</style>
