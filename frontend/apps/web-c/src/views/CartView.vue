<script setup>
import { computed, onMounted, ref } from "vue";
import { useRouter } from "vue-router";
import { listCartItems, removeCartItem, updateCartItem } from "@shared";

const router = useRouter();
const cartItems = ref([]);
const loading = ref(false);
const error = ref("");

const checkedItems = computed(() => cartItems.value.filter((item) => Number(item.checked) === 1));
const total = computed(() =>
  checkedItems.value.reduce((sum, item) => sum + Number(item.price || 0) * Number(item.quantity || 0), 0)
);

onMounted(fetchCart);

async function fetchCart() {
  loading.value = true;
  error.value = "";
  try {
    cartItems.value = await listCartItems();
  } catch (e) {
    error.value = e.message || "购物车加载失败";
  } finally {
    loading.value = false;
  }
}

async function changeQuantity(item, delta) {
  const nextQuantity = Math.max(1, Number(item.quantity) + delta);
  try {
    await updateCartItem(item.id, { quantity: nextQuantity, checked: item.checked });
    item.quantity = nextQuantity;
  } catch (e) {
    error.value = e.message || "更新数量失败";
  }
}

async function toggleChecked(item) {
  const nextChecked = Number(item.checked) === 1 ? 0 : 1;
  try {
    await updateCartItem(item.id, { quantity: item.quantity, checked: nextChecked });
    item.checked = nextChecked;
  } catch (e) {
    error.value = e.message || "更新勾选状态失败";
  }
}

async function handleRemove(item) {
  try {
    await removeCartItem(item.id);
    cartItems.value = cartItems.value.filter((current) => current.id !== item.id);
  } catch (e) {
    error.value = e.message || "移除商品失败";
  }
}

function goCheckout() {
  if (!checkedItems.value.length) {
    error.value = "请先勾选要结算的商品";
    return;
  }
  router.push("/checkout?fromCart=1");
}
</script>

<template>
  <section class="cart-shell">
    <div class="cart-head">
      <div>
        <p>购物车</p>
        <h1>待结算商品</h1>
      </div>
      <span>{{ cartItems.length }} 件商品</span>
    </div>

    <div class="cart-layout">
      <section class="cart-list">
        <article v-for="item in cartItems" :key="item.id" class="cart-item">
          <label class="cart-check">
            <input type="checkbox" :checked="Number(item.checked) === 1" @change="toggleChecked(item)" />
          </label>

          <div class="cart-item-image-shell">
            <img :src="item.mainImage" :alt="item.title" class="cart-item-image" />
          </div>

          <div class="cart-item-copy">
            <strong>{{ item.title }}</strong>
            <span>{{ item.attrsJson || "默认规格" }}</span>
            <small>库存 {{ item.stockAvailable || 0 }}</small>
          </div>

          <div class="cart-item-stepper">
            <button type="button" @click="changeQuantity(item, -1)">-</button>
            <span>{{ item.quantity }}</span>
            <button type="button" @click="changeQuantity(item, 1)">+</button>
          </div>

          <div class="cart-item-meta">
            <span>单价 ￥{{ Number(item.price || 0).toFixed(2) }}</span>
            <b>￥{{ (Number(item.price || 0) * Number(item.quantity || 0)).toFixed(2) }}</b>
            <button type="button" class="cart-remove" @click="handleRemove(item)">删除</button>
          </div>
        </article>

        <p v-if="!cartItems.length && !loading" class="ly-muted">购物车还是空的，先去挑几款常备药吧。</p>
      </section>

      <aside class="cart-summary">
        <small>本次结算</small>
        <strong>￥{{ total.toFixed(2) }}</strong>
        <span>已选 {{ checkedItems.length }} 件商品</span>
        <button type="button" @click="goCheckout">去确认订单</button>
        <p class="cart-tip">下一步会进入确认订单页，提交后订单会出现在个人中心的待付款列表。</p>
      </aside>
    </div>
  </section>

  <p v-if="error" class="ly-error">{{ error }}</p>
</template>

<style scoped>
.cart-shell {
  padding: 28px;
  border-radius: 32px;
  background: rgba(255, 252, 248, 0.94);
  box-shadow: 0 20px 44px rgba(71, 44, 25, 0.08);
}

.cart-head {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 20px;
}

.cart-head p,
.cart-item-copy span,
.cart-item-copy small,
.cart-item-meta span,
.cart-summary small,
.cart-summary span,
.cart-head span,
.cart-tip {
  margin: 0 0 8px;
  color: rgba(36, 25, 18, 0.58);
}

.cart-head h1 {
  margin: 0;
  font-size: 36px;
  color: #241912;
}

.cart-layout {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 320px;
  gap: 18px;
}

.cart-list,
.cart-summary {
  padding: 20px;
  border-radius: 24px;
  background: rgba(255, 255, 255, 0.78);
  box-shadow: inset 0 0 0 1px rgba(90, 56, 35, 0.06);
}

.cart-list {
  display: grid;
  gap: 14px;
}

.cart-item {
  display: grid;
  grid-template-columns: 36px 120px minmax(0, 1fr) 120px 140px;
  gap: 14px;
  align-items: center;
}

.cart-check input {
  width: 18px;
  height: 18px;
}

.cart-item-image-shell {
  height: 100px;
  border-radius: 18px;
  overflow: hidden;
  background: linear-gradient(180deg, #fff8f0 0%, #f7eadf 100%);
}

.cart-item-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}

.cart-item-copy strong,
.cart-summary strong {
  display: block;
  color: #241912;
}

.cart-item-copy strong {
  font-size: 22px;
  margin-bottom: 8px;
}

.cart-item-stepper {
  display: flex;
  align-items: center;
  gap: 10px;
}

.cart-item-stepper button,
.cart-remove,
.cart-summary button {
  min-height: 42px;
  border: 0;
  border-radius: 999px;
  background: linear-gradient(135deg, #ff6d4d, #ff8d57);
  color: #fff9f4;
  cursor: pointer;
}

.cart-item-stepper button {
  min-width: 42px;
}

.cart-item-meta {
  text-align: right;
}

.cart-item-meta b {
  display: block;
  font-size: 24px;
  color: #241912;
}

.cart-remove {
  margin-top: 8px;
  min-height: 34px;
  padding: 0 12px;
}

.cart-summary {
  display: grid;
  align-content: start;
  gap: 10px;
}

.cart-summary strong {
  font-size: 42px;
}

.cart-summary button {
  min-height: 46px;
}

@media (max-width: 960px) {
  .cart-layout,
  .cart-item {
    grid-template-columns: 1fr;
  }
}
</style>
