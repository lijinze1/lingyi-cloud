<script setup>
import { computed } from "vue";
import { useRoute } from "vue-router";

const route = useRoute();
const productId = computed(() => route.params.id);
</script>

<template>
  <div>
    <h2>商品详情 #{{ productId }}</h2>
    <div class="card">
      <p>这里是商品图文详情占位，后续接商品详情接口。</p>
      <p>可扩展：评论区、AI客服入口、加入购物车按钮。</p>
    </div>
  </div>
</template>