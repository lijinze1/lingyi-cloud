<script setup>
import { ref, onMounted } from "vue";
import { pingProduct } from "@shared";

const status = ref("加载中...");
const list = ref([
  { id: 1, name: "智能血压计", price: 199 },
  { id: 2, name: "便携雾化器", price: 299 },
  { id: 3, name: "医用口罩50只", price: 39 }
]);

onMounted(async () => {
  try {
    const data = await pingProduct();
    status.value = `${data.service} ${data.status}`;
  } catch {
    status.value = "产品服务暂不可达";
  }
});
</script>

<template>
  <div>
    <h2>商品列表</h2>
    <p>服务状态：{{ status }}</p>
    <div class="card" v-for="item in list" :key="item.id">
      <h3>{{ item.name }}</h3>
      <p>价格：¥{{ item.price }}</p>
      <router-link :to="`/products/${item.id}`">查看详情</router-link>
    </div>
  </div>
</template>