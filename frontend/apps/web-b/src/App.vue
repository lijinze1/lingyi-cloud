<template>
  <div class="shell">
    <header>
      <h1>Lingyi B端管理后台</h1>
      <nav>
        <router-link to="/dashboard">控制台</router-link>
        <router-link to="/users">用户管理</router-link>
        <router-link to="/products">商品管理</router-link>
        <router-link to="/login">登录</router-link>
      </nav>
    </header>
    <main>
      <router-view />
    </main>
  </div>
</template>

<style>
body {
  margin: 0;
  font-family: "Microsoft YaHei", sans-serif;
}
.shell {
  max-width: 1100px;
  margin: 0 auto;
  padding: 20px;
}
header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
nav {
  display: flex;
  gap: 16px;
}
a {
  color: #0052cc;
  text-decoration: none;
}
main {
  margin-top: 20px;
}
.card {
  border: 1px solid #ddd;
  border-radius: 8px;
  padding: 12px;
  margin: 8px 0;
}
input, button {
  padding: 8px;
  margin: 4px 0;
}
</style>