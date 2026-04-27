<script setup>
const products = [
  { name: "智能血压计", state: "在售", stock: 218, note: "首页重点露出" },
  { name: "便携雾化器", state: "待审核", stock: 58, note: "等待补充详情图" },
  { name: "维生素收纳套组", state: "在售", stock: 164, note: "适合做组合推荐" },
  { name: "医用冷敷贴", state: "低库存", stock: 24, note: "建议补货提醒" }
];
</script>

<template>
  <section class="admin-products-page">
    <header class="admin-products-hero">
      <div>
        <p>商品与库存</p>
        <h2>把商品审核、库存状态和推荐位信息做成更像运营后台的工作视图</h2>
      </div>
      <button type="button">新增商品</button>
    </header>

    <section class="admin-products-grid">
      <article class="admin-products-panel admin-products-panel-wide">
        <div class="admin-products-head">
          <div>
            <p>商品列表</p>
            <h3>当前重点商品</h3>
          </div>
          <span>{{ products.length }} 个商品</span>
        </div>

        <div class="admin-products-table">
          <div v-for="item in products" :key="item.name" class="admin-products-row">
            <div>
              <strong>{{ item.name }}</strong>
              <span>{{ item.note }}</span>
            </div>
            <b :class="item.state">{{ item.state }}</b>
            <span>库存 {{ item.stock }}</span>
            <button type="button">查看</button>
          </div>
        </div>
      </article>

      <article class="admin-products-panel">
        <div class="admin-products-head">
          <div>
            <p>本日建议</p>
            <h3>运营提醒</h3>
          </div>
        </div>

        <ul class="admin-products-notices">
          <li>优先处理“待审核”商品，减少首页空档位。</li>
          <li>低库存商品建议联动前台推荐权重下调。</li>
          <li>首页主视觉可持续放大 AI 问诊与商品联动入口。</li>
        </ul>
      </article>
    </section>
  </section>
</template>

<style scoped>
.admin-products-page {
  display: grid;
  gap: 18px;
}

.admin-products-hero,
.admin-products-panel {
  padding: 24px 26px;
  border-radius: 28px;
  background: rgba(255, 252, 248, 0.92);
  box-shadow: 0 18px 36px rgba(71, 44, 25, 0.08);
}

.admin-products-hero {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  gap: 18px;
}

.admin-products-hero p,
.admin-products-head p,
.admin-products-row span,
.admin-products-notices li {
  margin: 0 0 8px;
  color: rgba(36, 25, 18, 0.58);
}

.admin-products-hero h2,
.admin-products-head h3 {
  margin: 0;
  color: #241912;
}

.admin-products-hero h2 {
  font-size: 34px;
  line-height: 1.12;
  max-width: 760px;
}

.admin-products-hero button,
.admin-products-row button {
  min-height: 42px;
  padding: 0 18px;
  border: 0;
  border-radius: 999px;
  background: linear-gradient(135deg, #ff7d57, #ffae61);
  color: #fff9f3;
  cursor: pointer;
}

.admin-products-grid {
  display: grid;
  grid-template-columns: minmax(0, 1.15fr) minmax(320px, 0.85fr);
  gap: 16px;
}

.admin-products-head {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  gap: 12px;
  margin-bottom: 18px;
}

.admin-products-head span {
  color: rgba(36, 25, 18, 0.62);
}

.admin-products-table {
  display: grid;
}

.admin-products-row {
  display: grid;
  grid-template-columns: minmax(220px, 1fr) 110px 110px auto;
  gap: 14px;
  align-items: center;
  padding: 18px 0;
  border-bottom: 1px solid rgba(90, 56, 35, 0.08);
}

.admin-products-row:last-child {
  border-bottom: 0;
}

.admin-products-row strong {
  display: block;
  font-size: 22px;
  color: #241912;
}

.admin-products-row b {
  font-weight: 700;
}

.admin-products-row b.在售 {
  color: #c95f2a;
}

.admin-products-row b.待审核 {
  color: #8e6a35;
}

.admin-products-row b.低库存 {
  color: #cb4b3f;
}

.admin-products-notices {
  list-style: none;
  padding: 0;
  margin: 0;
  display: grid;
  gap: 12px;
}

@media (max-width: 1100px) {
  .admin-products-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 860px) {
  .admin-products-hero {
    align-items: flex-start;
  }

  .admin-products-row {
    grid-template-columns: 1fr;
  }
}
</style>
