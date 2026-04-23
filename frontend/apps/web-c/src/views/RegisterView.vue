<script setup>
import { ref } from "vue";
import { useRouter } from "vue-router";
import { register, setToken, setUser, me } from "@shared";

const router = useRouter();
const username = ref("");
const nickname = ref("");
const password = ref("");
const error = ref("");

async function submit() {
  error.value = "";
  try {
    const data = await register(username.value, password.value, nickname.value);
    setToken(data.token);
    const current = await me();
    setUser(current);
    router.push("/profile");
  } catch (e) {
    error.value = e.message;
  }
}
</script>

<template>
  <div>
    <h2>C端注册</h2>
    <div class="card">
      <div><input v-model="username" placeholder="用户名(4-32)" /></div>
      <div><input v-model="nickname" placeholder="昵称" /></div>
      <div><input v-model="password" placeholder="密码(6-64)" type="password" /></div>
      <button @click="submit">注册</button>
      <p v-if="error" style="color: red">{{ error }}</p>
    </div>
  </div>
</template>