<script setup>
import { computed, ref, watch } from "vue";
import { RouterLink, useRoute, useRouter } from "vue-router";
import { pageProducts } from "@shared";

const route = useRoute();
const router = useRouter();
const pageSize = 10;
const searchText = ref(String(route.query.q || ""));
const loading = ref(false);
const error = ref("");
const productPage = ref({ pageNo: 1, pageSize, total: 0, records: [] });

const currentPage = computed(() => {
  const page = Number(route.query.page || 1);
  return Number.isFinite(page) && page > 0 ? page : 1;
});

const totalPages = computed(() => Math.max(1, Math.ceil((productPage.value.total || 0) / pageSize)));
const visiblePages = computed(() => {
  const total = totalPages.value;
  const current = currentPage.value;
  const start = Math.max(1, current - 2);
  const end = Math.min(total, start + 4);
  const fixedStart = Math.max(1, end - 4);
  return Array.from({ length: end - fixedStart + 1 }, (_, index) => fixedStart + index);
});

watch(
  () => route.fullPath,
  () => {
    searchText.value = String(route.query.q || "");
    fetchProducts();
  },
  { immediate: true }
);

async function fetchProducts() {
  loading.value = true;
  error.value = "";
  try {
    const page = await pageProducts({
      q: String(route.query.q || "").trim(),
      pageNo: currentPage.value,
      pageSize
    });
    productPage.value = page || { pageNo: 1, pageSize, total: 0, records: [] };
  } catch (e) {
    error.value = e.message || "商品列表加载失败";
  } finally {
    loading.value = false;
  }
}

function submitSearch() {
  router.push({
    path: "/products",
    query: {
      q: searchText.value.trim() || undefined,
      page: "1"
    }
  });
}

function changePage(page) {
  router.push({
    path: "/products",
    query: {
      q: searchText.value.trim() || undefined,
      page: String(page)
    }
  });
}
</script>

<template>
  <section class="search-page-shell">
    <div class="search-page-head">
      <div>
        <h1>{{ route.query.q ? `“${route.query.q}” 的搜索结果` : "全部药品" }}</h1>
      </div>
      <span>{{ productPage.total || 0 }} 个结果</span>
    </div>

    <div class="search-topbar">
      <div class="search-input-shell">
        <input
          v-model="searchText"
          type="text"
          placeholder="搜索药品、分类、适用症状"
          @keyup.enter="submitSearch"
        />
        <button type="button" @click="submitSearch">搜索</button>
      </div>
    </div>

    <div v-if="productPage.records?.length" class="search-grid">
      <article v-for="item in productPage.records" :key="item.id" class="search-card">
        <div class="search-card-image-shell">
          <img :src="item.mainImage" :alt="item.name" class="search-card-image" />
        </div>
        <div class="search-card-meta-top">
          <span>{{ item.categoryName || '常备药品' }}</span>
          <span>{{ item.stockAvailable > 0 ? '现货' : '缺货' }}</span>
        </div>
        <h3>{{ item.name }}</h3>
        <p>{{ item.subTitle || '适合家庭常备与日常复购' }}</p>
        <div class="search-card-meta-bottom">
          <strong>¥{{ Number(item.minPrice || 0).toFixed(2) }}</strong>
        </div>
        <RouterLink class="search-card-link" :to="`/products/${item.id}`">查看详情</RouterLink>
      </article>
    </div>

    <p v-else-if="!loading" class="ly-muted">没有找到匹配的药品，可以换个关键词再试试。</p>

    <div class="search-pagination">
      <button type="button" :disabled="currentPage <= 1" @click="changePage(currentPage - 1)">上一页</button>
      <button
        v-for="page in visiblePages"
        :key="page"
        type="button"
        :class="{ active: page === currentPage }"
        @click="changePage(page)"
      >
        {{ page }}
      </button>
      <button type="button" :disabled="currentPage >= totalPages" @click="changePage(currentPage + 1)">下一页</button>
    </div>
  </section>

  <p v-if="error" class="ly-error">{{ error }}</p>
</template>

<style scoped>
.search-page-shell {
  padding: 28px;
  border-radius: 32px;
  background: rgba(255, 252, 248, 0.94);
  box-shadow: 0 20px 40px rgba(71, 44, 25, 0.08);
}

.search-page-head,
.search-card-meta-top,
.search-card-meta-bottom,
.search-pagination {
  display: flex;
  align-items: center;
  gap: 12px;
}

.search-page-head {
  justify-content: space-between;
  margin-bottom: 20px;
}

.search-page-head h1 {
  margin: 0;
  font-size: 36px;
  color: #241912;
}

.search-page-head span,
.search-card-meta-top span,
.search-card p {
  color: rgba(36, 25, 18, 0.68);
}

.search-topbar {
  display: grid;
  gap: 14px;
  margin-bottom: 22px;
}

.search-input-shell {
  display: grid;
  grid-template-columns: 1fr auto;
  gap: 10px;
  padding: 10px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.96);
  box-shadow:
    0 18px 36px rgba(44, 89, 70, 0.08),
    inset 0 0 0 1px rgba(69, 116, 95, 0.08);
}

.search-input-shell input {
  border: 0;
  background: transparent;
  padding: 0 16px;
  color: #241912;
  font: inherit;
}

.search-input-shell input:focus {
  outline: none;
}

.search-input-shell button {
  min-height: 50px;
  padding: 0 24px;
  border: 0;
  border-radius: 999px;
  background: linear-gradient(135deg, #ff6d4d, #ff8d57);
  color: #fff9f4;
  font: inherit;
  font-weight: 700;
  cursor: pointer;
}

.search-grid {
  display: grid;
  grid-template-columns: repeat(5, minmax(0, 1fr));
  gap: 14px;
}

.search-card {
  padding: 14px;
  border-radius: 22px;
  background: rgba(255, 255, 255, 0.82);
  box-shadow: inset 0 0 0 1px rgba(90, 56, 35, 0.06);
}

.search-card-image-shell {
  height: 186px;
  margin-bottom: 12px;
  border-radius: 18px;
  overflow: hidden;
  background: linear-gradient(180deg, #fff8f0 0%, #f7eadf 100%);
}

.search-card-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}

.search-card h3 {
  margin: 10px 0 6px;
  font-size: 18px;
  color: #241912;
}

.search-card p {
  min-height: 68px;
  margin: 0;
  font-size: 14px;
  line-height: 1.65;
}

.search-card-meta-bottom {
  margin-top: 12px;
}

.search-card-meta-bottom strong {
  font-size: 24px;
  color: #241912;
}

.search-card-link {
  margin-top: 12px;
  min-height: 40px;
  border-radius: 999px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 100%;
  text-decoration: none;
  color: #241912;
  box-shadow: inset 0 0 0 1px rgba(90, 56, 35, 0.08);
}

.search-pagination {
  justify-content: center;
  margin-top: 24px;
  flex-wrap: wrap;
}

.search-pagination button {
  min-width: 42px;
  min-height: 42px;
  padding: 0 14px;
  border-radius: 999px;
  border: 1px solid rgba(95, 59, 39, 0.08);
  background: rgba(255, 255, 255, 0.88);
  color: #241912;
  cursor: pointer;
}

.search-pagination button.active {
  background: linear-gradient(135deg, #ff6d4d, #ff8d57);
  color: #fff9f4;
  border-color: transparent;
}

.search-pagination button:disabled {
  opacity: 0.42;
  cursor: not-allowed;
}

@media (max-width: 1280px) {
  .search-grid {
    grid-template-columns: repeat(4, minmax(0, 1fr));
  }
}

@media (max-width: 1024px) {
  .search-grid {
    grid-template-columns: repeat(3, minmax(0, 1fr));
  }
}

@media (max-width: 760px) {
  .search-page-shell {
    padding: 20px;
  }

  .search-page-head {
    display: grid;
    grid-template-columns: 1fr;
  }

  .search-input-shell {
    grid-template-columns: 1fr;
  }

  .search-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 560px) {
  .search-grid {
    grid-template-columns: 1fr;
  }
}
</style>