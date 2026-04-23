<script setup>
import { ref, onMounted } from "vue";
import { me } from "@shared";

const user = ref(null);

onMounted(async () => {
  user.value = await me();
});
</script>

<template>
  <div>
    <h2>控制台</h2>
    <div class="card" v-if="user">
      <p>当前用户：{{ user.username }}</p>
      <p>角色：{{ (user.roles || []).join(', ') }}</p>
      <p>说明：此页面已通过 ROLE_ADMIN 守卫。</p>
    </div>
  </div>
</template>