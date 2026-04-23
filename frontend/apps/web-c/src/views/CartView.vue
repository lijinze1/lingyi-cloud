<script setup>
import productColdMedicine from "@shared/assets/product-cold-medicine.svg";
import productPainRelief from "@shared/assets/product-pain-relief.svg";

const cartItems = [
  {
    id: 1,
    name: "感冒灵颗粒",
    spec: "10 袋 / 盒",
    price: 26,
    qty: 2,
    image: productColdMedicine
  },
  {
    id: 2,
    name: "布洛芬缓释胶囊",
    spec: "24 粒 / 盒",
    price: 32,
    qty: 1,
    image: productPainRelief
  }
];

const total = cartItems.reduce((sum, item) => sum + item.price * item.qty, 0);
</script>

<template>
  <section class="cart-shell">
    <div class="cart-head">
      <div>
        <p>购物车</p>
        <h1>待结算药品</h1>
      </div>
      <span>{{ cartItems.length }} 件商品</span>
    </div>

    <div class="cart-layout">
      <section class="cart-list">
        <article v-for="item in cartItems" :key="item.id" class="cart-item">
          <div class="cart-item-image-shell">
            <img :src="item.image" :alt="item.name" class="cart-item-image" />
          </div>

          <div class="cart-item-copy">
            <strong>{{ item.name }}</strong>
            <span>{{ item.spec }}</span>
          </div>

          <div class="cart-item-meta">
            <span>数量 x{{ item.qty }}</span>
            <b>¥{{ item.price * item.qty }}</b>
          </div>
        </article>
      </section>

      <aside class="cart-summary">
        <small>结算金额</small>
        <strong>¥{{ total }}</strong>
        <button type="button">去结算</button>
      </aside>
    </div>
  </section>
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
.cart-item-meta span,
.cart-summary small,
.cart-head span {
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
  grid-template-columns: minmax(0, 1fr) 280px;
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
  grid-template-columns: 120px minmax(0, 1fr) 120px;
  gap: 14px;
  align-items: center;
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

.cart-item-meta {
  text-align: right;
}

.cart-item-meta b {
  display: block;
  font-size: 24px;
  color: #241912;
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
  border: 0;
  border-radius: 999px;
  background: linear-gradient(135deg, #ff6d4d, #ff8d57);
  color: #fff9f4;
  cursor: pointer;
}

@media (max-width: 900px) {
  .cart-layout,
  .cart-item {
    grid-template-columns: 1fr;
  }

  .cart-item-meta {
    text-align: left;
  }
}
</style>
