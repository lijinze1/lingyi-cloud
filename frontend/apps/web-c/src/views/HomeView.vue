<template>
  <section class="ly-card home-hero">
    <div>
      <h2 class="ly-title">欢迎来到灵医商城</h2>
      <p class="ly-kv">
        游客可直接浏览商品和详情。登录后可使用购物车、下单、个人中心和 AI 智能客服。
      </p>
      <div class="home-actions">
        <RouterLink class="ly-button" to="/products">开始浏览商品</RouterLink>
        <RouterLink class="home-link" to="/login">用户登录</RouterLink>
      </div>
    </div>
    <div class="home-tags">
      <span>医疗器械</span>
      <span>健康管理</span>
      <span>在线咨询</span>
      <span>极速配送</span>
    </div>
  </section>
</template>

<style scoped>
.home-hero {
  display: grid;
  grid-template-columns: 1.2fr 0.8fr;
  gap: 20px;
  align-items: center;
}

.home-actions {
  margin-top: 18px;
  display: flex;
  align-items: center;
  gap: 12px;
}

.home-link {
  color: var(--ly-primary);
  font-weight: 600;
  text-decoration: none;
}

.home-tags {
  display: grid;
  gap: 10px;
}

.home-tags span {
  display: inline-flex;
  width: fit-content;
  padding: 8px 12px;
  border-radius: 999px;
  background: #edf4ff;
  color: #23437d;
  font-size: 13px;
}

@media (max-width: 768px) {
  .home-hero {
    grid-template-columns: 1fr;
  }
}
</style>
