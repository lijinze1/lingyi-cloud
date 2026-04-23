<script setup>
const docs = [
  { name: "呼吸系统常见问答", source: "运营整理", updatedAt: "今天 10:20", status: "已同步", note: "覆盖感冒、咳嗽、咽喉不适等高频问法。" },
  { name: "肠胃类用药知识", source: "药师校对", updatedAt: "今天 11:45", status: "待复核", note: "补充饭后胃胀、反酸、腹泻等场景答案。" },
  { name: "过敏护理词条", source: "内容组", updatedAt: "昨天 18:10", status: "已同步", note: "支持季节性过敏与鼻炎场景推荐。" }
];

const syncTips = [
  "高频知识建议先保证标题、摘要、适用场景三段结构一致。",
  "待复核词条优先补齐来源和更新时间，避免上线后难追踪。",
  "同步完成后建议抽查 5 条问答链路，确认前台引用正常。"
];
</script>

<template>
  <section class="admin-knowledge-page">
    <header class="admin-knowledge-hero">
      <div>
        <p>知识库管理</p>
        <h2>统一管理知识文档、同步状态和前台引用关系</h2>
      </div>
      <button type="button">上传文档</button>
    </header>

    <section class="admin-knowledge-grid">
      <article class="admin-knowledge-panel admin-knowledge-panel-wide">
        <div class="admin-knowledge-head">
          <div>
            <p>文档列表</p>
            <h3>当前知识条目</h3>
          </div>
          <span>{{ docs.length }} 个条目</span>
        </div>

        <div class="admin-knowledge-table">
          <div v-for="item in docs" :key="item.name" class="admin-knowledge-row">
            <div>
              <strong>{{ item.name }}</strong>
              <span>{{ item.note }}</span>
            </div>
            <span>{{ item.source }}</span>
            <span>{{ item.updatedAt }}</span>
            <b>{{ item.status }}</b>
            <button type="button">查看</button>
          </div>
        </div>
      </article>

      <article class="admin-knowledge-panel">
        <div class="admin-knowledge-head">
          <div>
            <p>同步建议</p>
            <h3>本日提醒</h3>
          </div>
        </div>

        <ul class="admin-knowledge-notices">
          <li v-for="tip in syncTips" :key="tip">{{ tip }}</li>
        </ul>
      </article>
    </section>
  </section>
</template>

<style scoped>
.admin-knowledge-page {
  display: grid;
  gap: 18px;
}

.admin-knowledge-hero,
.admin-knowledge-panel {
  padding: 24px 26px;
  border-radius: 28px;
  background: rgba(255, 252, 248, 0.92);
  box-shadow: 0 18px 36px rgba(71, 44, 25, 0.08);
}

.admin-knowledge-hero {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  gap: 18px;
}

.admin-knowledge-hero p,
.admin-knowledge-head p,
.admin-knowledge-row span,
.admin-knowledge-notices li {
  margin: 0 0 8px;
  color: rgba(36, 25, 18, 0.58);
}

.admin-knowledge-hero h2,
.admin-knowledge-head h3 {
  margin: 0;
  color: #241912;
}

.admin-knowledge-hero h2 {
  font-size: 34px;
  line-height: 1.12;
  max-width: 760px;
}

.admin-knowledge-hero button,
.admin-knowledge-row button {
  min-height: 42px;
  padding: 0 18px;
  border: 0;
  border-radius: 999px;
  background: linear-gradient(135deg, #ff7d57, #ffae61);
  color: #fff9f3;
  cursor: pointer;
}

.admin-knowledge-grid {
  display: grid;
  grid-template-columns: minmax(0, 1.2fr) minmax(320px, 0.8fr);
  gap: 16px;
}

.admin-knowledge-head {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  gap: 12px;
  margin-bottom: 18px;
}

.admin-knowledge-head span {
  color: rgba(36, 25, 18, 0.62);
}

.admin-knowledge-table {
  display: grid;
}

.admin-knowledge-row {
  display: grid;
  grid-template-columns: minmax(220px, 1.1fr) 150px 120px 90px auto;
  gap: 14px;
  align-items: center;
  padding: 18px 0;
  border-bottom: 1px solid rgba(90, 56, 35, 0.08);
}

.admin-knowledge-row:last-child {
  border-bottom: 0;
}

.admin-knowledge-row strong {
  display: block;
  font-size: 22px;
  color: #241912;
}

.admin-knowledge-row b {
  color: #c95f2a;
}

.admin-knowledge-notices {
  list-style: none;
  padding: 0;
  margin: 0;
  display: grid;
  gap: 12px;
}

@media (max-width: 1100px) {
  .admin-knowledge-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 860px) {
  .admin-knowledge-hero {
    align-items: flex-start;
  }

  .admin-knowledge-row {
    grid-template-columns: 1fr;
  }
}
</style>
