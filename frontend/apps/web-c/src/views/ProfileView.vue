<script setup>
import { ref, onMounted } from "vue";
import { clearAuth, getUser, me, setUser } from "@shared";
import { useRouter } from "vue-router";

const router = useRouter();
const user = ref(getUser());
const error = ref("");

onMounted(async () => {
  try {
    const current = await me();
    setUser(current);
    user.value = current;
  } catch (e) {
    error.value = e.message;
  }
});

function logout() {
  clearAuth();
  router.push("/login");
}
</script>

<template>
  <div>
    <h2>个人中心</h2>
    <div class="card" v-if="user">
      <p>用户ID：{{ user.userId }}</p>
      <p>用户名：{{ user.username }}</p>
      <p>昵称：{{ user.nickname }}</p>
      <p>角色：{{ (user.roles || []).join(', ') || '无' }}</p>
      <button @click="logout">退出登录</button>
    </div>
    <p v-if="error" style="color: red">{{ error }}</p>
  </div>
</template>