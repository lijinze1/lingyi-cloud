<script setup>
import { computed, ref, watch } from "vue";
import { useRoute, useRouter } from "vue-router";
import { addCartItem, attemptSeckill, getProductDetail } from "@shared";

const route = useRoute();
const router = useRouter();
const product = ref(null);
const selectedSkuId = ref(null);
const loading = ref(false);
const saving = ref(false);
const seckillSaving = ref(false);
const error = ref("");
const actionBusy = computed(() => loading.value || saving.value || seckillSaving.value);

const isSeckillEntry = computed(() => route.query.seckill === "1");
const seckillActivityId = computed(() => route.query.seckillActivityId || "");
const seckillActivitySkuId = computed(() => route.query.seckillActivitySkuId || "");
const seckillPrice = computed(() => Number(route.query.seckillPrice || 0));
const selectedSku = computed(() => {
  return (product.value?.skus || []).find((item) => String(item.id) === String(selectedSkuId.value)) || null;
});

watch(
  () => route.fullPath,
  () => {
    fetchDetail();
  },
  { immediate: true }
);

async function fetchDetail() {
  loading.value = true;
  error.value = "";
  try {
    const detail = await getProductDetail(route.params.id);
    product.value = detail;
    selectedSkuId.value = route.query.skuId || detail?.skus?.[0]?.id || null;
  } catch (e) {
    error.value = e.message || "商品详情加载失败";
    product.value = null;
  } finally {
    loading.value = false;
  }
}

function formatAttrs(attrsJson) {
  if (!attrsJson) {
    return "默认规格";
  }
  try {
    const json = JSON.parse(attrsJson);
    if (Array.isArray(json)) {
      return json.map((item) => `${item.name || item.key}:${item.value || item.label || ""}`).join(" / ");
    }
    if (typeof json === "object") {
      return Object.entries(json).map(([key, value]) => `${key}:${value}`).join(" / ");
    }
  } catch {
    return attrsJson;
  }
  return String(attrsJson);
}

async function handleAddCart() {
  if (actionBusy.value) {
    return;
  }
  if (!selectedSku.value) {
    error.value = "当前商品没有可加入购物车的规格";
    return;
  }
  saving.value = true;
  error.value = "";
  try {
    await addCartItem(selectedSku.value.id, 1);
    router.push("/cart");
  } catch (e) {
    error.value = e.message || "加入购物车失败";
  } finally {
    saving.value = false;
  }
}

function handleDirectBuy() {
  if (actionBusy.value) {
    return;
  }
  if (!selectedSku.value || !product.value) {
    error.value = "当前商品没有可立即购买的规格";
    return;
  }
  router.push({
    path: "/checkout",
    query: {
      spuId: product.value.id,
      skuId: selectedSku.value.id
    }
  });
}

async function handleSeckillBuy() {
  if (actionBusy.value) {
    return;
  }
  if (!isSeckillEntry.value || !seckillActivityId.value || !seckillActivitySkuId.value) {
    error.value = "当前秒杀活动信息不完整";
    return;
  }
  seckillSaving.value = true;
  error.value = "";
  try {
    const result = await attemptSeckill(seckillActivityId.value, seckillActivitySkuId.value);
    if (!result?.recordId) {
      throw new Error(result?.message || "秒杀请求失败");
    }
    router.push(`/seckill/${result.recordId}`);
  } catch (e) {
    error.value = e.message || "秒杀请求失败";
  } finally {
    seckillSaving.value = false;
  }
}
</script>

<template>
  <section v-if="product" class="detail-shell">
    <div class="detail-visual">
      <div class="detail-image-shell">
        <img :src="product.mainImage" :alt="product.name" />
      </div>
    </div>

    <div class="detail-copy">
      <p>{{ product.categoryName || "常备药品" }}</p>
      <h1>{{ product.name }}</h1>
      <span>{{ product.subTitle || product.detail || "适合家庭常备与日常复购" }}</span>

      <div class="detail-price-row" v-if="selectedSku">
        <strong>￥{{ Number(selectedSku.price || 0).toFixed(2) }}</strong>
        <small>库存 {{ selectedSku.stockAvailable || 0 }}</small>
      </div>

      <div v-if="isSeckillEntry" class="detail-seckill-banner">
        <span>限时秒杀</span>
        <div>
          <strong>￥{{ seckillPrice.toFixed(2) }}</strong>
          <small>原价 ￥{{ Number(selectedSku?.price || 0).toFixed(2) }}</small>
        </div>
      </div>

      <div class="detail-benefit">
        <span>店铺优惠</span>
        <b>{{ Number(selectedSku?.price || 0) >= 30 ? "满30减5" : Number(selectedSku?.price || 0) >= 20 ? "满20减3" : "下单立减1元" }}</b>
      </div>

      <div class="detail-tags">
        <button
          v-for="sku in product.skus"
          :key="sku.id"
          type="button"
          class="detail-sku-chip"
          :class="{ active: String(selectedSkuId) === String(sku.id) }"
          :disabled="actionBusy"
          @click="selectedSkuId = sku.id"
        >
          {{ formatAttrs(sku.attrsJson) }}
        </button>
      </div>

      <div v-if="selectedSku" class="detail-desc-block">
        <strong>规格说明</strong>
        <span>{{ formatAttrs(selectedSku.attrsJson) }}</span>
      </div>

      <div class="detail-actions" :class="{ busy: actionBusy }">
        <button v-if="isSeckillEntry" type="button" class="ghost" @click="handleDirectBuy">普通购买</button>
        <button v-else type="button" class="ghost" @click="handleDirectBuy">领券购买</button>
        <button v-if="isSeckillEntry" type="button" :disabled="seckillSaving" @click="handleSeckillBuy">
          {{ seckillSaving ? "秒杀提交中..." : "立即秒杀" }}
        </button>
        <button type="button" :disabled="saving" @click="handleAddCart">
          {{ saving ? "加入中..." : "加入购物车" }}
        </button>
      </div>
    </div>
  </section>

  <section v-else-if="!loading" class="detail-empty">
    <h1>没有找到该药品</h1>
    <p>可以返回搜索页重新检索关键词。</p>
  </section>

  <p v-if="error" class="ly-error">{{ error }}</p>
</template>

<style scoped>
.detail-shell {
  min-height: 420px;
  display: grid;
  grid-template-columns: 0.9fr 1.1fr;
  gap: 18px;
  padding: 28px;
  border-radius: 32px;
  background: rgba(255, 252, 248, 0.92);
  box-shadow: 0 20px 40px rgba(71, 44, 25, 0.08);
}

.detail-visual,
.detail-copy,
.detail-empty {
  border-radius: 28px;
}

.detail-visual {
  padding: 18px;
  background: linear-gradient(180deg, #fff8f0 0%, #f7eadf 100%);
}

.detail-image-shell {
  height: 100%;
  min-height: 360px;
  border-radius: 22px;
  overflow: hidden;
  background: rgba(255, 255, 255, 0.7);
}

.detail-image-shell img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}

.detail-copy {
  display: grid;
  align-content: center;
  gap: 14px;
}

.detail-copy p {
  margin: 0;
  color: rgba(36, 25, 18, 0.62);
}

.detail-copy h1 {
  margin: 0;
  font-size: 46px;
  color: #241912;
}

.detail-copy span {
  color: rgba(36, 25, 18, 0.72);
  line-height: 1.85;
}

.detail-price-row,
.detail-benefit {
  display: flex;
  align-items: center;
  gap: 12px;
}

.detail-price-row strong {
  font-size: 34px;
  color: #241912;
}

.detail-price-row small,
.detail-benefit span {
  color: rgba(36, 25, 18, 0.52);
}

.detail-benefit {
  width: fit-content;
  padding: 10px 14px;
  border-radius: 18px;
  background: rgba(255, 109, 77, 0.08);
}

.detail-seckill-banner {
  width: fit-content;
  display: grid;
  gap: 6px;
  padding: 14px 18px;
  border-radius: 22px;
  background: linear-gradient(135deg, #ff6d4d, #ff8d57);
  color: #fff9f4;
}

.detail-seckill-banner span,
.detail-seckill-banner small,
.detail-seckill-banner strong {
  color: inherit;
}

.detail-seckill-banner strong {
  font-size: 34px;
}

.detail-benefit b,
.detail-desc-block strong {
  color: #a4562d;
}

.detail-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.detail-sku-chip {
  min-height: 38px;
  padding: 0 14px;
  border-radius: 999px;
  border: 1px solid rgba(255, 109, 77, 0.16);
  background: rgba(255, 109, 77, 0.06);
  color: #a4562d;
  cursor: pointer;
}

.detail-sku-chip.active {
  background: linear-gradient(135deg, #ff6d4d, #ff8d57);
  color: #fff9f4;
  border-color: transparent;
}

.detail-sku-chip:disabled {
  cursor: not-allowed;
  opacity: 0.6;
}

.detail-desc-block {
  display: grid;
  gap: 8px;
}

.detail-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}

.detail-actions.busy {
  pointer-events: none;
  opacity: 0.72;
}

.detail-actions button {
  min-height: 50px;
  padding: 0 22px;
  border: 0;
  border-radius: 999px;
  cursor: pointer;
  font-weight: 700;
}

.detail-actions button:disabled {
  cursor: not-allowed;
  opacity: 0.6;
}

.detail-actions button:last-child {
  background: linear-gradient(135deg, #ff6d4d, #ff8d57);
  color: #fff9f4;
}

.detail-actions .ghost {
  background: rgba(255, 109, 77, 0.1);
  color: #a4562d;
}

.detail-empty {
  min-height: 300px;
  padding: 28px;
  background: rgba(255, 252, 248, 0.92);
  box-shadow: 0 20px 40px rgba(71, 44, 25, 0.08);
}

@media (max-width: 900px) {
  .detail-shell {
    grid-template-columns: 1fr;
  }
}
</style>
