<script setup>
import { onMounted, ref } from "vue";
import { me } from "@shared";

const user = ref(null);

onMounted(async () => {
  user.value = await me();
});

const metrics = [
  { label: "今日新增用户", value: "128", note: "较昨日 +12%" },
  { label: "在线会话", value: "36", note: "问诊助手活跃中" },
  { label: "待审核商品", value: "09", note: "建议优先处理" },
  { label: "提示词版本", value: "14", note: "本周已更新" }
];

const tasks = [
  { title: "用户权限申请", status: "待处理", owner: "权限组", time: "09:30" },
  { title: "商品审核队列", status: "处理中", owner: "商品组", time: "10:15" },
  { title: "知识库同步", status: "已完成", owner: "内容组", time: "11:20" }
];

const notices = [
  "新商品提审后 2 小时内完成首轮审核。",
  "问诊演示模块本周优先接真实回复能力。",
  "后台账号已固定为 admin / 123456，便于联调。"
];
</script>

<template>
  <section class="dashboard-hero">
    <div>
      <p>工作台概览</p>
      <h2>把待办、数据和当前账号集中在一个视图里</h2>
      <span>减少空白和弱卡片感，让后台第一屏更像真正的运营工作面板。</span>
    </div>
    <div class="dashboard-account">
      <small>当前账号</small>
      <strong>{{ user?.nickname || user?.username || "管理员" }}</strong>
      <span>{{ (user?.roles || []).join(" / ") || "ROLE_ADMIN" }}</span>
    </div>
  </section>

  <section class="dashboard-metrics">
    <article v-for="item in metrics" :key="item.label" class="dashboard-metric-card">
      <small>{{ item.label }}</small>
      <strong>{{ item.value }}</strong>
      <span>{{ item.note }}</span>
    </article>
  </section>

  <section class="dashboard-grid">
    <article class="dashboard-panel dashboard-panel-wide">
      <div class="dashboard-panel-head">
        <div>
          <p>待处理事项</p>
          <h3>今日工作队列</h3>
        </div>
        <button type="button">查看全部</button>
      </div>

      <div class="dashboard-task-list">
        <div v-for="task in tasks" :key="task.title" class="dashboard-task-row">
          <div>
            <strong>{{ task.title }}</strong>
            <span>{{ task.owner }}</span>
          </div>
          <b>{{ task.status }}</b>
          <span>{{ task.time }}</span>
        </div>
      </div>
    </article>

    <article class="dashboard-panel">
      <div class="dashboard-panel-head">
        <div>
          <p>系统提示</p>
          <h3>运营提醒</h3>
        </div>
      </div>

      <ul class="dashboard-notice-list">
        <li v-for="notice in notices" :key="notice">{{ notice }}</li>
      </ul>

      <div class="dashboard-status-card">
        <small>登录状态</small>
        <strong>已登录</strong>
      </div>
    </article>
  </section>
</template>

<style scoped>
.dashboard-hero {
  padding: 28px 30px;
  border-radius: 30px;
  display: grid;
  grid-template-columns: minmax(0, 1fr) 280px;
  gap: 18px;
  background: linear-gradient(135deg, rgba(255, 246, 234, 0.96), rgba(255, 255, 255, 0.82));
  box-shadow: 0 20px 40px rgba(71, 44, 25, 0.08);
}

.dashboard-hero p,
.dashboard-panel-head p,
.dashboard-account small,
.dashboard-metric-card small,
.dashboard-status-card small {
  margin: 0 0 8px;
  color: rgba(36, 25, 18, 0.58);
}

.dashboard-hero h2,
.dashboard-panel-head h3 {
  margin: 0;
  font-size: 36px;
  line-height: 1.1;
  color: #241912;
}

.dashboard-hero span,
.dashboard-account span,
.dashboard-metric-card span,
.dashboard-task-row span,
.dashboard-notice-list li {
  color: rgba(36, 25, 18, 0.68);
  line-height: 1.7;
}

.dashboard-account {
  padding: 20px;
  border-radius: 24px;
  background: linear-gradient(135deg, #ff7a54, #ffaf66);
  color: #fffaf4;
  display: grid;
  align-content: center;
}

.dashboard-account strong {
  font-size: 30px;
}

.dashboard-account span,
.dashboard-account small {
  color: rgba(255, 250, 244, 0.88);
}

.dashboard-metrics {
  margin-top: 18px;
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 14px;
}

.dashboard-metric-card,
.dashboard-panel,
.dashboard-status-card {
  padding: 22px;
  border-radius: 26px;
  background: rgba(255, 252, 248, 0.92);
  box-shadow: 0 18px 36px rgba(71, 44, 25, 0.08);
}

.dashboard-metric-card strong,
.dashboard-status-card strong {
  display: block;
  font-size: 38px;
  color: #241912;
}

.dashboard-grid {
  margin-top: 18px;
  display: grid;
  grid-template-columns: minmax(0, 1.1fr) minmax(320px, 0.9fr);
  gap: 16px;
}

.dashboard-panel-wide {
  min-width: 0;
}

.dashboard-panel-head {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  align-items: flex-end;
  margin-bottom: 18px;
}

.dashboard-panel-head button {
  min-height: 40px;
  padding: 0 16px;
  border: 0;
  border-radius: 999px;
  background: rgba(255, 109, 77, 0.1);
  color: #241912;
  cursor: pointer;
}

.dashboard-task-list,
.dashboard-notice-list {
  display: grid;
  gap: 12px;
}

.dashboard-task-row {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 92px 64px;
  gap: 12px;
  align-items: center;
  padding: 16px 0;
  border-bottom: 1px solid rgba(90, 56, 35, 0.08);
}

.dashboard-task-row:last-child {
  border-bottom: 0;
}

.dashboard-task-row strong {
  display: block;
  font-size: 20px;
  color: #241912;
}

.dashboard-task-row b {
  font-weight: 600;
  color: #c95f2a;
}

.dashboard-notice-list {
  list-style: none;
  padding: 0;
  margin: 0 0 18px;
}

.dashboard-status-card {
  background: rgba(255, 109, 77, 0.08);
}

@media (max-width: 1100px) {
  .dashboard-metrics {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .dashboard-grid,
  .dashboard-hero {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 720px) {
  .dashboard-metrics {
    grid-template-columns: 1fr;
  }

  .dashboard-task-row {
    grid-template-columns: 1fr;
  }
}
</style>
