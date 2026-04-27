<script setup>
const users = [
  { username: "admin", nickname: "后台管理员", role: "ROLE_ADMIN", status: "正常", scene: "后台运营 / 审核 / 配置" },
  { username: "test001", nickname: "体验用户", role: "ROLE_USER", status: "正常", scene: "用户端联调 / 登录验证" },
  { username: "doctor-demo", nickname: "问诊演示账号", role: "ROLE_USER", status: "观察中", scene: "AI 问诊演示" }
];
</script>

<template>
  <section class="admin-list-page">
    <header class="admin-list-hero">
      <div>
        <p>账号与角色</p>
        <h2>把后台账号、默认测试账号和权限状态放在同一个面板里</h2>
      </div>
      <button type="button">新增账号</button>
    </header>

    <section class="admin-list-grid">
      <article class="admin-list-panel admin-list-panel-wide">
        <div class="admin-list-head">
          <div>
            <p>用户列表</p>
            <h3>当前账号总览</h3>
          </div>
          <span>{{ users.length }} 个账号</span>
        </div>

        <div class="admin-user-table">
          <div v-for="item in users" :key="item.username" class="admin-user-row">
            <div>
              <strong>{{ item.nickname }}</strong>
              <span>{{ item.username }}</span>
            </div>
            <span>{{ item.role }}</span>
            <span>{{ item.scene }}</span>
            <b>{{ item.status }}</b>
            <button type="button">查看</button>
          </div>
        </div>
      </article>

      <article class="admin-list-panel">
        <div class="admin-list-head">
          <div>
            <p>联调说明</p>
            <h3>默认测试账号</h3>
          </div>
        </div>

        <div class="admin-account-card">
          <small>B 端默认</small>
          <strong>admin / 123456</strong>
        </div>

        <div class="admin-account-card soft">
          <small>用户端默认</small>
          <strong>test001 / 123456</strong>
        </div>
      </article>
    </section>
  </section>
</template>

<style scoped>
.admin-list-page {
  display: grid;
  gap: 18px;
}

.admin-list-hero,
.admin-list-panel,
.admin-account-card {
  padding: 24px 26px;
  border-radius: 28px;
  background: rgba(255, 252, 248, 0.92);
  box-shadow: 0 18px 36px rgba(71, 44, 25, 0.08);
}

.admin-list-hero {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  gap: 18px;
}

.admin-list-hero p,
.admin-list-head p,
.admin-account-card small,
.admin-user-row span {
  margin: 0 0 8px;
  color: rgba(36, 25, 18, 0.58);
}

.admin-list-hero h2,
.admin-list-head h3 {
  margin: 0;
  color: #241912;
}

.admin-list-hero h2 {
  font-size: 34px;
  line-height: 1.12;
  max-width: 760px;
}

.admin-list-hero button,
.admin-user-row button {
  min-height: 42px;
  padding: 0 18px;
  border: 0;
  border-radius: 999px;
  background: linear-gradient(135deg, #ff7d57, #ffae61);
  color: #fff9f3;
  cursor: pointer;
}

.admin-list-grid {
  display: grid;
  grid-template-columns: minmax(0, 1.2fr) minmax(320px, 0.8fr);
  gap: 16px;
}

.admin-list-head {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  gap: 12px;
  margin-bottom: 18px;
}

.admin-list-head span {
  color: rgba(36, 25, 18, 0.62);
}

.admin-user-table {
  display: grid;
}

.admin-user-row {
  display: grid;
  grid-template-columns: minmax(180px, 1.1fr) 180px 1fr 80px auto;
  gap: 14px;
  align-items: center;
  padding: 18px 0;
  border-bottom: 1px solid rgba(90, 56, 35, 0.08);
}

.admin-user-row:last-child {
  border-bottom: 0;
}

.admin-user-row strong {
  display: block;
  font-size: 20px;
  color: #241912;
}

.admin-user-row b {
  color: #c95f2a;
}

.admin-account-card {
  display: grid;
  gap: 8px;
}

.admin-account-card strong {
  font-size: 28px;
  color: #241912;
}

.admin-account-card.soft {
  margin-top: 14px;
  background: rgba(255, 109, 77, 0.08);
}

@media (max-width: 1100px) {
  .admin-list-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 860px) {
  .admin-list-hero {
    align-items: flex-start;
  }

  .admin-user-row {
    grid-template-columns: 1fr;
  }
}
</style>
