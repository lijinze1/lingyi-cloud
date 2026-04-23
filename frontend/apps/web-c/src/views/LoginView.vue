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
    const redirect = route.query.redirect || "/profile";
    router.push(String(redirect));
  } catch (e) {
    error.value = e.message;
  }
}
</script>

<template>
  <div>
    <h2>C端登录</h2>
    <div class="card">
      <div><input v-model="username" placeholder="用户名" /></div>
      <div><input v-model="password" placeholder="密码" type="password" /></div>
      <button @click="submit">登录</button>
      <p v-if="error" style="color: red">{{ error }}</p>
    </div>
  </div>
</template>