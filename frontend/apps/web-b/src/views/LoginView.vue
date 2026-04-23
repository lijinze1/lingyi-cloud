<script setup>
import { ref } from "vue";
import { useRoute, useRouter } from "vue-router";
import { login, me, setToken, setUser } from "@shared";

const route = useRoute();
const router = useRouter();
const username = ref("admin");
const password = ref("admin123456");
const error = ref("");

async function submit() {
  error.value = "";
  try {
    const data = await login(username.value, password.value);
    setToken(data.token);
    const current = await me();
    setUser(current);
    const redirect = route.query.redirect || "/dashboard";
    router.push(String(redirect));
  } catch (e) {
    error.value = e.message;
  }
}
</script>

<template>
  <div>
    <h2>B端登录</h2>
    <div class="card">
      <div><input v-model="username" placeholder="管理员账号" /></div>
      <div><input v-model="password" type="password" placeholder="密码" /></div>
      <button @click="submit">登录管理后台</button>
      <p v-if="error" style="color: red">{{ error }}</p>
    </div>
  </div>
</template>