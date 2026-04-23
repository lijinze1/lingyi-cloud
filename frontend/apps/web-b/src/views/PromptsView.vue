<script setup>
const prompts = [
  { name: "首页问药助手", version: "v1.4", scene: "用户端首页", status: "生效中", note: "优先引导用户补充症状、持续时间和已有用药。" },
  { name: "商品详情问答", version: "v1.2", scene: "药品详情页", status: "待评审", note: "回答适用症状、禁忌提醒和搭配建议。" },
  { name: "后台审核辅助", version: "v0.9", scene: "运营后台", status: "测试中", note: "帮助审核同义词、类目归属和推荐话术。" }
];

const notices = [
  "高频提示词建议按‘首页 / 搜索 / 详情页’拆分管理。",
  "问药类提示词优先控制风险词和绝对化回答。",
  "新版本上线前建议先做 20 条典型问题回归。"
];
</script>

<template>
  <section class="admin-module-page">
    <header class="admin-module-hero">
      <div>
        <p>提示词管理</p>
        <h2>集中管理问药话术、场景版本和上线状态</h2>
      </div>
      <button type="button">新建提示词</button>
    </header>

    <section class="admin-module-grid">
      <article class="admin-module-panel admin-module-panel-wide">
        <div class="admin-module-head">
          <div>
            <p>版本列表</p>
            <h3>当前提示词配置</h3>
          </div>
          <span>{{ prompts.length }} 个版本</span>
        </div>

        <div class="admin-module-table">
          <div v-for="item in prompts" :key="item.name" class="admin-module-row">
            <div>
              <strong>{{ item.name }}</strong>
              <span>{{ item.note }}</span>
            </div>
            <span>{{ item.scene }}</span>
            <span>{{ item.version }}</span>
            <b>{{ item.status }}</b>
            <button type="button">查看</button>
          </div>
        </div>
      </article>

      <article class="admin-module-panel">
        <div class="admin-module-head">
          <div>
            <p>维护建议</p>
            <h3>本周提醒</h3>
          </div>
        </div>

        <ul class="admin-module-notices">
          <li v-for="notice in notices" :key="notice">{{ notice }}</li>
        </ul>
      </article>
    </section>
  </section>
</template>

<style scoped>
.admin-module-page {
  display: grid;
  gap: 18px;
}

.admin-module-hero,
.admin-module-panel {
  padding: 24px 26px;
  border-radius: 28px;
  background: rgba(255, 252, 248, 0.92);
  box-shadow: 0 18px 36px rgba(71, 44, 25, 0.08);
}

.admin-module-hero {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  gap: 18px;
}

.admin-module-hero p,
.admin-module-head p,
.admin-module-row span,
.admin-module-notices li {
  margin: 0 0 8px;
  color: rgba(36, 25, 18, 0.58);
}

.admin-module-hero h2,
.admin-module-head h3 {
  margin: 0;
  color: #241912;
}

.admin-module-hero h2 {
  font-size: 34px;
  line-height: 1.12;
  max-width: 760px;
}

.admin-module-hero button,
.admin-module-row button {
  min-height: 42px;
  padding: 0 18px;
  border: 0;
  border-radius: 999px;
  background: linear-gradient(135deg, #ff7d57, #ffae61);
  color: #fff9f3;
  cursor: pointer;
}

.admin-module-grid {
  display: grid;
  grid-template-columns: minmax(0, 1.2fr) minmax(320px, 0.8fr);
  gap: 16px;
}

.admin-module-head {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  gap: 12px;
  margin-bottom: 18px;
}

.admin-module-head span {
  color: rgba(36, 25, 18, 0.62);
}

.admin-module-table {
  display: grid;
}

.admin-module-row {
  display: grid;
  grid-template-columns: minmax(220px, 1.1fr) 170px 90px 90px auto;
  gap: 14px;
  align-items: center;
  padding: 18px 0;
  border-bottom: 1px solid rgba(90, 56, 35, 0.08);
}

.admin-module-row:last-child {
  border-bottom: 0;
}

.admin-module-row strong {
  display: block;
  font-size: 22px;
  color: #241912;
}

.admin-module-row b {
  color: #c95f2a;
}

.admin-module-notices {
  list-style: none;
  padding: 0;
  margin: 0;
  display: grid;
  gap: 12px;
}

@media (max-width: 1100px) {
  .admin-module-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 860px) {
  .admin-module-hero {
    align-items: flex-start;
  }

  .admin-module-row {
    grid-template-columns: 1fr;
  }
}
</style>
