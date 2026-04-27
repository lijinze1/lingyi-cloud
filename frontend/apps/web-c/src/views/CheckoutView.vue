<script setup>
import { computed, onMounted, ref } from "vue";
import { useRoute, useRouter } from "vue-router";
import { createOrder, getProductDetail, listCartItems, removeCheckedCartItems } from "@shared";

const route = useRoute();
const router = useRouter();
const loading = ref(false);
const submitting = ref(false);
const error = ref("");
const sourceType = ref("direct");
const orderItems = ref([]);

const receiver = ref({
  receiverName: "灵医用户",
  receiverPhone: "13800000000",
  receiverAddress: "上海市浦东新区灵医路 99 号",
  remark: ""
});

const totalAmount = computed(() =>
  orderItems.value.reduce((sum, item) => sum + Number(item.price || 0) * Number(item.quantity || 0), 0)
);

const couponDiscount = computed(() => {
  if (totalAmount.value >= 30) {
    return 5;
  }
  if (totalAmount.value >= 20) {
    return 3;
  }
  return 1;
});

const payableAmount = computed(() => Math.max(totalAmount.value - couponDiscount.value, 0));

onMounted(loadCheckout);

async function loadCheckout() {
  loading.value = true;
  error.value = "";
  try {
    if (route.query.fromCart === "1") {
      sourceType.value = "cart";
      const items = await listCartItems();
      orderItems.value = (items || [])
        .filter((item) => Number(item.checked) === 1)
        .map((item) => ({
          skuId: item.skuId,
          title: item.title,
          attrsJson: item.attrsJson,
          quantity: Number(item.quantity || 1),
          price: Number(item.price || 0),
          mainImage: item.mainImage,
          stockAvailable: item.stockAvailable
        }));
      if (!orderItems.value.length) {
        router.replace("/cart");
      }
      return;
    }

    sourceType.value = "direct";
    const spuId = route.query.spuId || route.query.id;
    const skuId = route.query.skuId;
    if (!spuId) {
      router.replace("/products");
      return;
    }
    const detail = await getProductDetail(spuId);
    const matchedSku = (detail?.skus || []).find((item) => String(item.id) === String(skuId)) || detail?.skus?.[0];
    if (!matchedSku) {
      throw new Error("当前商品没有可下单规格");
    }
    orderItems.value = [
      {
        skuId: matchedSku.id,
        title: detail.name,
        attrsJson: matchedSku.attrsJson,
        quantity: 1,
        price: Number(matchedSku.price || 0),
        mainImage: detail.mainImage,
        stockAvailable: matchedSku.stockAvailable
      }
    ];
  } catch (e) {
    error.value = e.message || "确认订单信息加载失败";
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

function changeQuantity(index, delta) {
  const target = orderItems.value[index];
  if (!target) {
    return;
  }
  target.quantity = Math.max(1, Number(target.quantity || 1) + delta);
}

async function submitOrder() {
  if (!orderItems.value.length) {
    error.value = "当前没有可提交的商品";
    return;
  }
  submitting.value = true;
  error.value = "";
  try {
    const order = await createOrder({
      items: orderItems.value.map((item) => ({ skuId: item.skuId, quantity: item.quantity })),
      receiverName: receiver.value.receiverName,
      receiverPhone: receiver.value.receiverPhone,
      receiverAddress: receiver.value.receiverAddress,
      remark: receiver.value.remark || (sourceType.value === "cart" ? "购物车结算" : "领券购买")
    });
    if (sourceType.value === "cart") {
      await removeCheckedCartItems();
    }
    router.replace(`/orders/${order.orderNo}`);
  } catch (e) {
    error.value = e.message || "提交订单失败";
  } finally {
    submitting.value = false;
  }
}
</script>

<template>
  <section class="checkout-shell" v-if="orderItems.length">
    <header class="checkout-head">
      <div>
        <p>确认订单</p>
        <h1>{{ sourceType === "cart" ? "购物车结算" : "领券购买" }}</h1>
      </div>
      <button type="button" class="ghost-link" @click="$router.back()">返回上一步</button>
    </header>

    <div class="checkout-layout">
      <section class="checkout-main">
        <section class="checkout-card">
          <h2>收货信息</h2>
          <div class="address-grid">
            <input v-model="receiver.receiverName" type="text" placeholder="收货人" />
            <input v-model="receiver.receiverPhone" type="text" placeholder="手机号" />
            <textarea v-model="receiver.receiverAddress" rows="3" placeholder="详细地址" />
            <textarea v-model="receiver.remark" rows="3" placeholder="订单备注，付款后商家可见" />
          </div>
        </section>

        <section class="checkout-card">
          <h2>确认商品</h2>
          <article v-for="(item, index) in orderItems" :key="`${item.skuId}-${index}`" class="checkout-product">
            <img :src="item.mainImage" :alt="item.title" />
            <div class="checkout-copy">
              <strong>{{ item.title }}</strong>
              <span>{{ formatAttrs(item.attrsJson) }}</span>
              <small>库存 {{ item.stockAvailable || 0 }}</small>
            </div>
            <div class="checkout-qty">
              <button type="button" @click="changeQuantity(index, -1)">-</button>
              <span>{{ item.quantity }}</span>
              <button type="button" @click="changeQuantity(index, 1)">+</button>
            </div>
            <b>￥{{ (Number(item.price || 0) * Number(item.quantity || 0)).toFixed(2) }}</b>
          </article>
        </section>
      </section>

      <aside class="checkout-side">
        <section class="checkout-card">
          <h2>付款详情</h2>
          <div class="summary-line">
            <span>商品总价</span>
            <b>￥{{ totalAmount.toFixed(2) }}</b>
          </div>
          <div class="summary-line accent">
            <span>优惠减免</span>
            <b>-￥{{ couponDiscount.toFixed(2) }}</b>
          </div>
          <div class="summary-line total">
            <span>应付金额</span>
            <strong>￥{{ payableAmount.toFixed(2) }}</strong>
          </div>
          <button type="button" :disabled="submitting" @click="submitOrder">
            {{ submitting ? "提交中..." : `提交订单 ￥${payableAmount.toFixed(2)}` }}
          </button>
          <p class="checkout-tip">提交后即生成待付款订单，你可以立即支付，也可以稍后去个人中心继续处理。</p>
        </section>
      </aside>
    </div>
  </section>

  <p v-if="loading" class="ly-muted">确认订单信息加载中...</p>
  <p v-if="error" class="ly-error">{{ error }}</p>
</template>

<style scoped>
.checkout-shell {
  display: grid;
  gap: 18px;
}

.checkout-head,
.checkout-card {
  padding: 24px;
  border-radius: 28px;
  background: rgba(255, 252, 248, 0.94);
  box-shadow: 0 20px 40px rgba(71, 44, 25, 0.08);
}

.checkout-head {
  display: flex;
  align-items: end;
  justify-content: space-between;
}

.checkout-head p,
.checkout-copy span,
.checkout-copy small,
.checkout-tip,
.summary-line span {
  color: rgba(36, 25, 18, 0.62);
}

.checkout-head h1,
.checkout-card h2,
.checkout-copy strong,
.summary-line strong {
  margin: 0;
  color: #241912;
}

.ghost-link {
  min-height: 42px;
  padding: 0 16px;
  border: 0;
  border-radius: 999px;
  background: rgba(255, 109, 77, 0.1);
  color: #a4562d;
  cursor: pointer;
}

.checkout-layout {
  display: grid;
  grid-template-columns: 1.3fr 0.7fr;
  gap: 18px;
}

.checkout-main,
.checkout-card,
.checkout-side {
  display: grid;
  gap: 16px;
}

.address-grid {
  display: grid;
  gap: 12px;
}

.address-grid input,
.address-grid textarea {
  width: 100%;
  border: 1px solid rgba(90, 56, 35, 0.08);
  border-radius: 16px;
  padding: 12px 14px;
  font: inherit;
  resize: none;
}

.checkout-product {
  display: grid;
  grid-template-columns: 100px 1fr 120px 100px;
  gap: 16px;
  align-items: center;
}

.checkout-product img {
  width: 100px;
  height: 100px;
  object-fit: cover;
  border-radius: 18px;
  background: linear-gradient(180deg, #fff8f0 0%, #f7eadf 100%);
}

.checkout-copy {
  display: grid;
  gap: 6px;
}

.checkout-qty {
  display: flex;
  align-items: center;
  gap: 10px;
}

.checkout-qty button,
.checkout-side button {
  min-height: 42px;
  min-width: 42px;
  border: 0;
  border-radius: 999px;
  background: linear-gradient(135deg, #ff6d4d, #ff8d57);
  color: #fff9f4;
  cursor: pointer;
}

.summary-line {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.summary-line.accent b {
  color: #ff6d4d;
}

.summary-line.total strong {
  font-size: 34px;
}

.checkout-side button {
  min-width: 100%;
}

@media (max-width: 960px) {
  .checkout-layout,
  .checkout-product {
    grid-template-columns: 1fr;
  }
}
</style>
