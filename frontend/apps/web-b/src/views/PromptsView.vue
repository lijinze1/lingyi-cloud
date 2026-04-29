<script setup>
import { computed, onMounted, reactive, ref } from "vue";
import {
  createPrompt,
  createPromptCategory,
  createPromptVersion,
  deletePrompt,
  deletePromptCategory,
  deletePromptVersion,
  flattenTree,
  formatDateTime,
  getPromptDetail,
  listPromptCategories,
  pagePrompts,
  publishPromptVersion,
  updatePrompt,
  updatePromptCategory
} from "@shared";
import { ElMessage, ElMessageBox } from "element-plus";
import {
  EditPen,
  FolderAdd,
  Plus,
  RefreshRight,
  Remove,
  Search
} from "@element-plus/icons-vue";

const loading = ref(false);
const categoryLoading = ref(false);
const detailLoading = ref(false);

const categories = ref([]);
const selectedCategoryId = ref();
const selectedPromptId = ref();
const promptPage = ref({
  records: [],
  total: 0,
  current: 1,
  size: 10
});
const promptDetail = ref(null);

const filters = reactive({
  keyword: ""
});

const categoryDialogVisible = ref(false);
const categoryDialogMode = ref("create");
const categoryFormRef = ref();
const categorySaving = ref(false);
const categoryForm = reactive({
  id: null,
  parentId: 0,
  categoryCode: "",
  categoryName: "",
  sortNo: 0,
  status: 1
});

const promptDialogVisible = ref(false);
const promptDialogMode = ref("create");
const promptFormRef = ref();
const promptSaving = ref(false);
const promptForm = reactive({
  id: null,
  categoryId: null,
  promptCode: "",
  name: "",
  bizScene: "",
  description: "",
  status: 1
});

const versionDialogVisible = ref(false);
const versionDialogMode = ref("create");
const versionSourceVersion = ref(null);
const versionFormRef = ref();
const versionSaving = ref(false);
const versionForm = reactive({
  content: "",
  variablesJson: "{\n  \n}",
  modelConfig: {
    model: "",
    temperature: 0.2,
    topP: 0.8,
    topK: 40,
    maxTokens: 2000,
    presencePenalty: 0,
    frequencyPenalty: 0
  }
});

function normalizeId(value) {
  if (value === undefined || value === null || value === "") {
    return null;
  }
  return String(value);
}

function matchId(left, right) {
  return normalizeId(left) === normalizeId(right);
}

function normalizeCategoryNodes(nodes = []) {
  return nodes.map((node) => ({
    ...node,
    id: normalizeId(node.id),
    parentId: normalizeId(node.parentId) || "0",
    children: normalizeCategoryNodes(node.children || [])
  }));
}

function normalizePromptRow(row) {
  return {
    ...row,
    id: normalizeId(row.id),
    categoryId: normalizeId(row.categoryId),
    publishedVersionId: normalizeId(row.publishedVersionId)
  };
}

function normalizePromptDetail(detail) {
  if (!detail) {
    return null;
  }
  return {
    ...detail,
    id: normalizeId(detail.id),
    categoryId: normalizeId(detail.categoryId),
    publishedVersionId: normalizeId(detail.publishedVersionId),
    versions: (detail.versions || [])
      .map((version) => ({
        ...version,
        id: normalizeId(version.id),
        promptId: normalizeId(version.promptId)
      }))
      .sort((left, right) => {
        if (left.status !== right.status) {
          return right.status - left.status;
        }
        return (right.versionNo || 0) - (left.versionNo || 0);
      })
  };
}

const categoryOptions = computed(() => flattenTree(categories.value).map((item) => ({
  label: `${"　".repeat(item.level)}${item.categoryName}`,
  value: item.id
})));

const currentCategory = computed(() =>
  flattenTree(categories.value).find((item) => matchId(item.id, selectedCategoryId.value)) || null
);

const hasCategoryFilter = computed(() => Boolean(currentCategory.value));
const currentPublishedVersion = computed(() =>
  (promptDetail.value?.versions || []).find((item) => item.status === 1) || null
);

const promptStats = computed(() => {
  const records = promptPage.value.records || [];
  return {
    categoryCount: flattenTree(categories.value).length,
    promptCount: promptPage.value.total || records.length,
    enabledCount: records.filter((item) => item.status === 1).length,
    publishedCount: records.filter((item) => item.publishedVersionId).length
  };
});

const categoryRules = {
  categoryCode: [{ required: true, message: "请输入分类编码", trigger: "blur" }],
  categoryName: [{ required: true, message: "请输入分类名称", trigger: "blur" }]
};

const promptRules = {
  categoryId: [{ required: true, message: "请选择分类", trigger: "change" }],
  promptCode: [{ required: true, message: "请输入提示词编码", trigger: "blur" }],
  name: [{ required: true, message: "请输入提示词名称", trigger: "blur" }],
  bizScene: [{ required: true, message: "请输入业务场景", trigger: "blur" }]
};

const versionRules = {
  content: [{ required: true, message: "请输入版本内容", trigger: "blur" }]
};

function statusTagType(status) {
  return status === 1 ? "success" : "info";
}

function promptStatusText(status) {
  return status === 1 ? "启用中" : "已停用";
}

function versionStatusText(status) {
  if (status === 1) return "已发布";
  if (status === 2) return "历史版本";
  return "草稿";
}

function versionDialogTitle() {
  return versionDialogMode.value === "derive" ? "基于当前版本创建新版本" : "新建版本";
}

function formatModelSummary(modelConfig) {
  if (!modelConfig) {
    return "未配置";
  }
  const segments = [
    `model: ${modelConfig.model || "--"}`,
    `temperature: ${modelConfig.temperature ?? "--"}`,
    `topP: ${modelConfig.topP ?? "--"}`,
    `topK: ${modelConfig.topK ?? "--"}`,
    `maxTokens: ${modelConfig.maxTokens ?? "--"}`
  ];
  return segments.join("  |  ");
}

function formatJsonText(text) {
  if (!text || !text.trim()) {
    return "--";
  }
  try {
    return JSON.stringify(JSON.parse(text), null, 2);
  } catch {
    return text;
  }
}

async function loadCategories() {
  categoryLoading.value = true;
  try {
    categories.value = normalizeCategoryNodes(await listPromptCategories());
  } catch (error) {
    categories.value = [];
    ElMessage.error(error?.message || "分类加载失败");
  } finally {
    categoryLoading.value = false;
  }
}

async function loadPrompts(reset = false) {
  loading.value = true;
  if (reset) {
    promptPage.value.current = 1;
  }
  try {
    const pageData = await pagePrompts({
      current: promptPage.value.current,
      size: promptPage.value.size,
      keyword: filters.keyword,
      categoryId: selectedCategoryId.value
    });
    promptPage.value = {
      records: (pageData.records || []).map(normalizePromptRow),
      total: pageData.total || 0,
      current: pageData.current || promptPage.value.current,
      size: pageData.size || promptPage.value.size
    };
    if (selectedPromptId.value) {
      const stillExists = promptPage.value.records.some((item) => matchId(item.id, selectedPromptId.value));
      if (!stillExists) {
        promptDetail.value = null;
        selectedPromptId.value = null;
      }
    }
  } catch (error) {
    promptPage.value = {
      records: [],
      total: 0,
      current: promptPage.value.current,
      size: promptPage.value.size
    };
    ElMessage.error(error?.message || "提示词列表加载失败");
  } finally {
    loading.value = false;
  }
}

async function loadPromptDetail(promptId) {
  if (!promptId) return;
  detailLoading.value = true;
  selectedPromptId.value = normalizeId(promptId);
  try {
    promptDetail.value = normalizePromptDetail(await getPromptDetail(promptId));
  } catch (error) {
    promptDetail.value = null;
    ElMessage.error(error?.message || "提示词详情加载失败");
  } finally {
    detailLoading.value = false;
  }
}

function handleCategoryNodeClick(node) {
  selectedCategoryId.value = normalizeId(node?.id);
  loadPrompts(true);
}

function resetCategoryFilter() {
  selectedCategoryId.value = null;
  loadPrompts(true);
}

function openCreateCategoryDialog() {
  categoryDialogMode.value = "create";
  Object.assign(categoryForm, {
    id: null,
    parentId: selectedCategoryId.value || 0,
    categoryCode: "",
    categoryName: "",
    sortNo: 0,
    status: 1
  });
  categoryDialogVisible.value = true;
}

function openEditCategoryDialog() {
  const selected = currentCategory.value;
  if (!selected) {
    ElMessage.warning("请先选择分类");
    return;
  }
  categoryDialogMode.value = "edit";
  Object.assign(categoryForm, {
    id: selected.id,
    parentId: selected.parentId,
    categoryCode: selected.categoryCode,
    categoryName: selected.categoryName,
    sortNo: selected.sortNo,
    status: selected.status
  });
  categoryDialogVisible.value = true;
}

async function submitCategory() {
  await categoryFormRef.value.validate();
  categorySaving.value = true;
  try {
    const payload = {
      parentId: categoryForm.parentId,
      categoryCode: categoryForm.categoryCode,
      categoryName: categoryForm.categoryName,
      sortNo: categoryForm.sortNo,
      status: categoryForm.status
    };
    if (categoryDialogMode.value === "create") {
      await createPromptCategory(payload);
      ElMessage.success("分类已创建");
    } else {
      await updatePromptCategory(categoryForm.id, payload);
      ElMessage.success("分类已更新");
    }
    categoryDialogVisible.value = false;
    await loadCategories();
  } finally {
    categorySaving.value = false;
  }
}

async function removeCategory() {
  const selected = currentCategory.value;
  if (!selected) {
    ElMessage.warning("请先选择分类");
    return;
  }
  await ElMessageBox.confirm(`确认删除分类“${selected.categoryName}”吗？`, "删除分类", { type: "warning" });
  await deletePromptCategory(selected.id);
  ElMessage.success("分类已删除");
  selectedCategoryId.value = undefined;
  await loadCategories();
  await loadPrompts(true);
}

function openCreatePromptDialog() {
  promptDialogMode.value = "create";
  Object.assign(promptForm, {
    id: null,
    categoryId: selectedCategoryId.value || null,
    promptCode: "",
    name: "",
    bizScene: "",
    description: "",
    status: 1
  });
  promptDialogVisible.value = true;
}

function openEditPromptDialog(row) {
  const prompt = row ? normalizePromptRow(row) : promptDetail.value;
  if (!prompt) {
    ElMessage.warning("请先选择提示词");
    return;
  }
  promptDialogMode.value = "edit";
  Object.assign(promptForm, {
    id: prompt.id,
    categoryId: prompt.categoryId,
    promptCode: prompt.promptCode,
    name: prompt.name,
    bizScene: prompt.bizScene,
    description: prompt.description || "",
    status: prompt.status
  });
  promptDialogVisible.value = true;
}

async function submitPrompt() {
  await promptFormRef.value.validate();
  promptSaving.value = true;
  try {
    const payload = {
      categoryId: promptForm.categoryId,
      promptCode: promptForm.promptCode,
      name: promptForm.name,
      bizScene: promptForm.bizScene,
      description: promptForm.description,
      status: promptForm.status
    };
    if (promptDialogMode.value === "create") {
      const created = normalizePromptRow(await createPrompt(payload));
      ElMessage.success("提示词已创建");
      promptDialogVisible.value = false;
      await loadPrompts(true);
      await loadPromptDetail(created.id);
    } else {
      await updatePrompt(promptForm.id, payload);
      ElMessage.success("提示词已更新");
      promptDialogVisible.value = false;
      await loadPrompts();
      await loadPromptDetail(promptForm.id);
    }
  } finally {
    promptSaving.value = false;
  }
}

async function removePrompt(row) {
  const prompt = row ? normalizePromptRow(row) : promptDetail.value;
  if (!prompt) {
    ElMessage.warning("请先选择提示词");
    return;
  }
  await ElMessageBox.confirm(`确认删除提示词“${prompt.name}”吗？`, "删除提示词", { type: "warning" });
  await deletePrompt(prompt.id);
  ElMessage.success("提示词已删除");
  promptDetail.value = null;
  selectedPromptId.value = null;
  await loadPrompts();
}

function buildDefaultModelConfig(modelConfig = {}) {
  return {
    model: modelConfig.model || "",
    temperature: modelConfig.temperature ?? 0.2,
    topP: modelConfig.topP ?? 0.8,
    topK: modelConfig.topK ?? 40,
    maxTokens: modelConfig.maxTokens ?? 2000,
    presencePenalty: modelConfig.presencePenalty ?? 0,
    frequencyPenalty: modelConfig.frequencyPenalty ?? 0
  };
}

function openCreateVersionDialog() {
  if (!promptDetail.value?.id) {
    ElMessage.warning("请先选择提示词");
    return;
  }
  versionDialogMode.value = "create";
  versionSourceVersion.value = null;
  Object.assign(versionForm, {
    content: "",
    variablesJson: "{\n  \n}",
    modelConfig: buildDefaultModelConfig()
  });
  versionDialogVisible.value = true;
}

function openDeriveVersionDialog(version) {
  if (!promptDetail.value?.id) {
    ElMessage.warning("请先选择提示词");
    return;
  }
  versionDialogMode.value = "derive";
  versionSourceVersion.value = version;
  Object.assign(versionForm, {
    content: version?.content || "",
    variablesJson: formatJsonText(version?.variablesJson) === "--" ? "{\n  \n}" : formatJsonText(version?.variablesJson),
    modelConfig: buildDefaultModelConfig(version?.modelConfig)
  });
  versionDialogVisible.value = true;
}

function validateJson(text, fieldName) {
  if (!text || !text.trim()) return;
  try {
    JSON.parse(text);
  } catch {
    throw new Error(`${fieldName} 需要是合法 JSON`);
  }
}

async function submitVersion() {
  await versionFormRef.value.validate();
  validateJson(versionForm.variablesJson, "变量定义");
  versionSaving.value = true;
  try {
    await createPromptVersion(promptDetail.value.id, {
      content: versionForm.content,
      variablesJson: versionForm.variablesJson,
      modelConfig: {
        model: versionForm.modelConfig.model || null,
        temperature: versionForm.modelConfig.temperature,
        topP: versionForm.modelConfig.topP,
        topK: versionForm.modelConfig.topK,
        maxTokens: versionForm.modelConfig.maxTokens,
        presencePenalty: versionForm.modelConfig.presencePenalty,
        frequencyPenalty: versionForm.modelConfig.frequencyPenalty
      }
    });
    ElMessage.success("版本已创建");
    versionDialogVisible.value = false;
    await loadPromptDetail(promptDetail.value.id);
    await loadPrompts();
  } finally {
    versionSaving.value = false;
  }
}

async function publishVersion(version) {
  const actionText = version.status === 0 ? "设为当前生效版本" : "重新设为当前生效版本";
  await ElMessageBox.confirm(`确认将 V${version.versionNo} ${actionText}吗？`, "启用版本", { type: "warning" });
  await publishPromptVersion(version.promptId, version.id);
  ElMessage.success(`V${version.versionNo} 已设为当前版本`);
  await loadPromptDetail(version.promptId);
  await loadPrompts();
}

async function removeVersion(version) {
  await ElMessageBox.confirm(`确认删除 V${version.versionNo} 吗？删除后无法恢复。`, "删除版本", { type: "warning" });
  await deletePromptVersion(version.promptId, version.id);
  ElMessage.success(`V${version.versionNo} 已删除`);
  await loadPromptDetail(version.promptId);
  await loadPrompts();
}

function handlePageChange(current) {
  promptPage.value.current = current;
  loadPrompts();
}

function handlePromptRowClick(row) {
  loadPromptDetail(row.id);
}

onMounted(async () => {
  await loadCategories();
  await loadPrompts(true);
});
</script>

<template>
  <section class="ly-page-stack">
    <el-card class="stats-shell" shadow="never">
      <div class="ly-kpi-grid">
        <div class="ly-kpi-item">
          <span>分类总数</span>
          <strong>{{ promptStats.categoryCount }}</strong>
        </div>
        <div class="ly-kpi-item">
          <span>提示词总数</span>
          <strong>{{ promptStats.promptCount }}</strong>
        </div>
        <div class="ly-kpi-item">
          <span>启用中</span>
          <strong>{{ promptStats.enabledCount }}</strong>
        </div>
        <div class="ly-kpi-item">
          <span>已挂发布版本</span>
          <strong>{{ promptStats.publishedCount }}</strong>
        </div>
      </div>
    </el-card>

    <div class="prompt-layout">
      <el-card class="ly-panel-card prompt-sidebar" shadow="never">
        <template #header>
          <div class="ly-page-toolbar">
            <strong>分类树</strong>
            <el-button :icon="RefreshRight" text @click="loadCategories" />
          </div>
        </template>

        <div class="prompt-side-actions">
          <el-button type="primary" plain :icon="FolderAdd" @click="openCreateCategoryDialog">新增分类</el-button>
          <el-button :icon="EditPen" @click="openEditCategoryDialog">编辑</el-button>
          <el-button :icon="Remove" @click="removeCategory">删除</el-button>
        </div>

        <div class="prompt-category-status">
          <span>{{ currentCategory ? `当前分类：${currentCategory.categoryName}` : "当前分类：全部提示词" }}</span>
          <el-button v-if="hasCategoryFilter" text @click="resetCategoryFilter">清空筛选</el-button>
        </div>

        <el-skeleton :loading="categoryLoading" animated>
          <template #template>
            <el-skeleton-item variant="rect" style="height: 280px; border-radius: 18px;" />
          </template>
          <el-scrollbar class="prompt-tree-scroll">
            <el-tree
              :data="categories"
              :current-node-key="selectedCategoryId"
              node-key="id"
              :props="{ label: 'categoryName', children: 'children' }"
              highlight-current
              default-expand-all
              :expand-on-click-node="false"
              @node-click="handleCategoryNodeClick"
            />
          </el-scrollbar>
        </el-skeleton>
      </el-card>

      <div class="prompt-main">
        <el-card class="ly-panel-card" shadow="never">
        <template #header>
          <div class="ly-page-toolbar">
            <strong>{{ currentCategory ? `${currentCategory.categoryName} · 提示词列表` : "提示词列表" }}</strong>
            <el-button type="primary" :icon="Plus" @click="openCreatePromptDialog">新建提示词</el-button>
          </div>
        </template>

          <div class="prompt-filter-bar">
            <el-input
              v-model="filters.keyword"
              placeholder="搜索提示词名称 / 编码 / 场景"
              clearable
              :prefix-icon="Search"
              @keyup.enter="loadPrompts(true)"
            />
            <el-button @click="loadPrompts(true)">查询</el-button>
          </div>

          <div class="prompt-table-shell">
            <el-table
              v-loading="loading"
              :data="promptPage.records"
              stripe
              border
              @row-click="handlePromptRowClick"
            >
              <el-table-column label="提示词" min-width="250">
                <template #default="{ row }">
                  <div class="ly-table-title">
                    <strong>{{ row.name }}</strong>
                    <div class="ly-inline-meta">
                      <span>编码：{{ row.promptCode }}</span>
                      <span>场景：{{ row.bizScene }}</span>
                    </div>
                  </div>
                </template>
              </el-table-column>
              <el-table-column prop="description" label="说明" min-width="220" show-overflow-tooltip />
              <el-table-column label="状态" width="110">
                <template #default="{ row }">
                  <el-tag :type="statusTagType(row.status)">{{ promptStatusText(row.status) }}</el-tag>
                </template>
              </el-table-column>
              <el-table-column label="发布版本" width="110">
                <template #default="{ row }">
                  <span>{{ row.publishedVersionId ? `#${row.publishedVersionId}` : "--" }}</span>
                </template>
              </el-table-column>
              <el-table-column label="创建时间" width="180">
                <template #default="{ row }">{{ formatDateTime(row.createdAt) }}</template>
              </el-table-column>
              <el-table-column label="操作" width="180" fixed="right">
                <template #default="{ row }">
                  <el-button link @click.stop="openEditPromptDialog(row)">编辑</el-button>
                  <el-button link type="danger" @click.stop="removePrompt(row)">删除</el-button>
                </template>
              </el-table-column>
            </el-table>
          </div>

          <div class="prompt-pagination">
            <el-pagination
              layout="total, prev, pager, next"
              :current-page="promptPage.current"
              :page-size="promptPage.size"
              :total="promptPage.total"
              @current-change="handlePageChange"
            />
          </div>
        </el-card>

        <el-card class="ly-panel-card" shadow="never">
        <template #header>
          <div class="ly-page-toolbar">
            <strong>{{ promptDetail ? `提示词详情与版本 · ${promptDetail.name}` : "提示词详情与版本" }}</strong>
            <div style="display: flex; gap: 10px; flex-wrap: wrap;">
              <el-button :disabled="!promptDetail" @click="openEditPromptDialog()">编辑主档</el-button>
              <el-button type="primary" :icon="Plus" :disabled="!promptDetail" @click="openCreateVersionDialog">
                新建空白版本
              </el-button>
              <el-button
                type="primary"
                plain
                :disabled="!currentPublishedVersion"
                @click="openDeriveVersionDialog(currentPublishedVersion)"
              >
                基于当前版本修改
              </el-button>
            </div>
          </div>
          </template>

          <el-skeleton :loading="detailLoading" animated>
            <template #template>
              <el-skeleton-item variant="rect" style="height: 260px; border-radius: 18px;" />
            </template>

            <el-empty v-if="!promptDetail" description="点击列表中的提示词查看版本与参数" />

            <div v-else class="prompt-detail-stack">
              <el-descriptions :column="2" border class="prompt-detail-card">
                <el-descriptions-item label="名称">{{ promptDetail.name }}</el-descriptions-item>
                <el-descriptions-item label="编码">{{ promptDetail.promptCode }}</el-descriptions-item>
                <el-descriptions-item label="业务场景">{{ promptDetail.bizScene }}</el-descriptions-item>
                <el-descriptions-item label="当前状态">
                  <el-tag :type="statusTagType(promptDetail.status)">{{ promptStatusText(promptDetail.status) }}</el-tag>
                </el-descriptions-item>
                <el-descriptions-item label="说明" :span="2">
                  {{ promptDetail.description || "--" }}
                </el-descriptions-item>
              </el-descriptions>

              <div class="prompt-workflow-tip">
                <div>
                  <strong>当前生效版本</strong>
                  <span>{{ currentPublishedVersion ? `V${currentPublishedVersion.versionNo}` : "暂未发布版本" }}</span>
                </div>
                <p>修改提示词正文或模型参数时，建议基于当前版本创建新版本；确认无误后再发布。历史版本也可以随时重新设为当前版本。</p>
              </div>

              <div class="prompt-version-shell">
                <div v-if="!(promptDetail.versions || []).length" class="prompt-empty-state">当前还没有版本内容。</div>
                <div v-else class="prompt-version-list">
                  <article v-for="row in promptDetail.versions" :key="row.id" class="prompt-version-card">
                    <div class="prompt-version-head">
                      <div class="prompt-version-title">
                        <strong>V{{ row.versionNo }}</strong>
                        <el-tag :type="row.status === 1 ? 'success' : 'info'">{{ versionStatusText(row.status) }}</el-tag>
                      </div>
                      <div class="prompt-version-actions">
                        <el-button link @click="openDeriveVersionDialog(row)">基于此版本修改</el-button>
                        <el-button v-if="row.status !== 1" link type="primary" @click="publishVersion(row)">设为当前版本</el-button>
                        <el-button v-if="row.status !== 1" link type="danger" @click="removeVersion(row)">删除版本</el-button>
                      </div>
                    </div>

                    <div class="prompt-version-meta">
                      <span>创建时间：{{ formatDateTime(row.createdAt) }}</span>
                      <span>发布时间：{{ formatDateTime(row.publishedAt) }}</span>
                    </div>

                    <div class="prompt-version-block">
                      <h4>提示词正文</h4>
                      <pre>{{ row.content || "--" }}</pre>
                    </div>

                    <div class="prompt-version-block">
                      <h4>变量定义</h4>
                      <pre>{{ formatJsonText(row.variablesJson) }}</pre>
                    </div>

                    <div class="prompt-version-block">
                      <h4>模型参数</h4>
                      <pre>{{ formatModelSummary(row.modelConfig) }}</pre>
                    </div>
                  </article>
                </div>
              </div>
            </div>
          </el-skeleton>
        </el-card>
      </div>
    </div>

    <el-dialog
      v-model="categoryDialogVisible"
      :title="categoryDialogMode === 'create' ? '新增分类' : '编辑分类'"
      width="560px"
    >
      <el-form ref="categoryFormRef" :model="categoryForm" :rules="categoryRules" label-position="top">
        <el-form-item label="上级分类">
          <el-select v-model="categoryForm.parentId" style="width: 100%;">
            <el-option :value="0" label="顶级分类" />
            <el-option
              v-for="option in categoryOptions"
              :key="option.value"
              :label="option.label"
              :value="option.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="分类编码" prop="categoryCode">
          <el-input v-model="categoryForm.categoryCode" />
        </el-form-item>
        <el-form-item label="分类名称" prop="categoryName">
          <el-input v-model="categoryForm.categoryName" />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="categoryForm.sortNo" :min="0" />
        </el-form-item>
        <el-form-item label="启用状态">
          <el-switch v-model="categoryForm.status" :active-value="1" :inactive-value="0" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="categoryDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="categorySaving" @click="submitCategory">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog
      v-model="promptDialogVisible"
      :title="promptDialogMode === 'create' ? '新建提示词' : '编辑提示词'"
      width="640px"
    >
      <el-form ref="promptFormRef" :model="promptForm" :rules="promptRules" label-position="top">
        <el-form-item label="所属分类" prop="categoryId">
          <el-select v-model="promptForm.categoryId" style="width: 100%;">
            <el-option
              v-for="option in categoryOptions"
              :key="option.value"
              :label="option.label"
              :value="option.value"
            />
          </el-select>
        </el-form-item>
        <div class="prompt-form-grid">
          <el-form-item label="提示词编码" prop="promptCode">
            <el-input v-model="promptForm.promptCode" />
          </el-form-item>
          <el-form-item label="名称" prop="name">
            <el-input v-model="promptForm.name" />
          </el-form-item>
        </div>
        <el-form-item label="业务场景" prop="bizScene">
          <el-input v-model="promptForm.bizScene" />
        </el-form-item>
        <el-form-item label="说明">
          <el-input v-model="promptForm.description" type="textarea" :rows="3" />
        </el-form-item>
        <el-form-item label="启用状态">
          <el-switch v-model="promptForm.status" :active-value="1" :inactive-value="0" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="promptDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="promptSaving" @click="submitPrompt">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="versionDialogVisible" :title="versionDialogTitle()" width="860px">
      <el-form ref="versionFormRef" :model="versionForm" :rules="versionRules" label-position="top">
        <div class="version-editor-tip">
          <strong>{{ versionDialogMode === "derive" ? `源版本：V${versionSourceVersion?.versionNo || "--"}` : "空白新版本" }}</strong>
          <span>{{ versionDialogMode === "derive" ? "会复制源版本正文和模型参数，保存后生成一个新的草稿版本。" : "适合从头起草新的提示词版本。" }}</span>
        </div>
        <el-form-item label="版本内容" prop="content">
          <el-input v-model="versionForm.content" type="textarea" :rows="12" />
        </el-form-item>
        <el-form-item label="变量定义 JSON">
          <el-input v-model="versionForm.variablesJson" type="textarea" :rows="6" />
        </el-form-item>
        <div class="version-option-grid">
          <el-form-item label="模型">
            <el-input v-model="versionForm.modelConfig.model" placeholder="例如 qwen-plus" />
          </el-form-item>
          <el-form-item label="temperature">
            <el-input-number v-model="versionForm.modelConfig.temperature" :min="0" :max="2" :step="0.1" />
          </el-form-item>
          <el-form-item label="topP">
            <el-input-number v-model="versionForm.modelConfig.topP" :min="0" :max="1" :step="0.05" />
          </el-form-item>
          <el-form-item label="topK">
            <el-input-number v-model="versionForm.modelConfig.topK" :min="1" :step="1" />
          </el-form-item>
          <el-form-item label="maxTokens">
            <el-input-number v-model="versionForm.modelConfig.maxTokens" :min="1" :step="100" />
          </el-form-item>
          <el-form-item label="presencePenalty">
            <el-input-number v-model="versionForm.modelConfig.presencePenalty" :min="-2" :max="2" :step="0.1" />
          </el-form-item>
          <el-form-item label="frequencyPenalty">
            <el-input-number v-model="versionForm.modelConfig.frequencyPenalty" :min="-2" :max="2" :step="0.1" />
          </el-form-item>
        </div>
      </el-form>
      <template #footer>
        <el-button @click="versionDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="versionSaving" @click="submitVersion">保存为新版本</el-button>
      </template>
    </el-dialog>
  </section>
</template>

<style scoped>
.stats-shell {
  border: 0;
  border-radius: 28px;
  background: rgba(255, 252, 248, 0.92);
  box-shadow: 0 18px 36px rgba(71, 44, 25, 0.08);
}

:deep(.stats-shell .el-card__body) {
  padding: 18px;
}

.ly-kpi-item {
  min-width: 0;
  padding: 22px 24px;
  border-radius: 24px;
  background: linear-gradient(180deg, rgba(255, 255, 255, 0.94), rgba(255, 248, 240, 0.9));
  border: 1px solid rgba(90, 56, 35, 0.08);
  box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.7);
}

.ly-kpi-item span {
  display: block;
  font-size: 15px;
  font-weight: 600;
  color: rgba(36, 25, 18, 0.62);
  letter-spacing: 0.01em;
}

.ly-kpi-item strong {
  display: block;
  margin-top: 12px;
  font-size: 40px;
  line-height: 1;
  color: #241912;
}

.prompt-layout {
  display: grid;
  grid-template-columns: 1fr;
  gap: 18px;
}

:deep(.prompt-sidebar .el-card__body),
:deep(.prompt-main > .el-card .el-card__body) {
  display: grid;
  gap: 18px;
}

.prompt-side-actions,
.prompt-form-grid,
.version-option-grid {
  display: grid;
  gap: 12px;
}

.prompt-side-actions {
  grid-template-columns: repeat(3, minmax(0, auto));
  align-items: center;
  justify-content: start;
}

.prompt-tree-scroll {
  max-height: 340px;
  padding-right: 8px;
}

.prompt-category-status {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  min-height: 36px;
  color: rgba(36, 25, 18, 0.62);
  font-size: 14px;
}

.prompt-filter-bar {
  display: grid;
  grid-template-columns: minmax(0, 1fr) auto;
  gap: 12px;
}

.prompt-main {
  display: grid;
  gap: 18px;
}

.prompt-table-shell,
.prompt-version-shell {
  overflow: hidden;
  border-radius: 22px;
  border: 1px solid rgba(90, 56, 35, 0.08);
}

.prompt-table-shell :deep(.el-table),
.prompt-version-shell :deep(.el-table) {
  width: 100%;
}

.prompt-detail-stack {
  display: grid;
  gap: 18px;
}

.prompt-workflow-tip,
.version-editor-tip {
  display: grid;
  gap: 8px;
  padding: 16px 18px;
  border-radius: 18px;
  background: linear-gradient(180deg, rgba(255, 250, 244, 0.94), rgba(255, 244, 234, 0.92));
  border: 1px solid rgba(90, 56, 35, 0.08);
}

.prompt-workflow-tip > div,
.version-editor-tip {
  color: #241912;
}

.prompt-workflow-tip > div {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

.prompt-workflow-tip strong,
.version-editor-tip strong {
  font-size: 15px;
}

.prompt-workflow-tip span,
.version-editor-tip span,
.prompt-workflow-tip p {
  margin: 0;
  color: rgba(36, 25, 18, 0.68);
  line-height: 1.7;
  font-size: 13px;
}

.prompt-version-list {
  display: grid;
  gap: 16px;
  padding: 16px;
  background: rgba(255, 251, 246, 0.72);
}

.prompt-version-card {
  display: grid;
  gap: 14px;
  padding: 18px;
  border-radius: 20px;
  background: #fffdfa;
  border: 1px solid rgba(90, 56, 35, 0.08);
}

.prompt-version-head,
.prompt-version-title,
.prompt-version-meta,
.prompt-version-actions {
  display: flex;
  align-items: center;
  gap: 12px;
}

.prompt-version-head {
  justify-content: space-between;
  flex-wrap: wrap;
}

.prompt-version-title strong {
  font-size: 18px;
  color: #241912;
}

.prompt-version-meta {
  flex-wrap: wrap;
  color: rgba(36, 25, 18, 0.58);
  font-size: 13px;
}

.prompt-version-block {
  display: grid;
  gap: 8px;
}

.prompt-version-block h4 {
  margin: 0;
  color: #241912;
  font-size: 14px;
}

.prompt-version-block pre {
  margin: 0;
  padding: 14px 16px;
  border-radius: 16px;
  background: #fff7f0;
  color: #3a2a20;
  white-space: pre-wrap;
  word-break: break-word;
  font: 13px/1.75 "Consolas", "SFMono-Regular", monospace;
}

.prompt-empty-state {
  padding: 28px 20px;
  color: rgba(36, 25, 18, 0.58);
  text-align: center;
}

.prompt-pagination {
  display: flex;
  justify-content: flex-end;
}

.prompt-detail-card {
  margin-bottom: 0;
}

.prompt-form-grid {
  grid-template-columns: repeat(2, minmax(0, 1fr));
}

.version-option-grid {
  grid-template-columns: repeat(2, minmax(0, 1fr));
}

@media (max-width: 860px) {
  :deep(.stats-shell .el-card__body) {
    padding: 14px;
  }

  .ly-kpi-item {
    padding: 18px 20px;
  }

  .ly-kpi-item strong {
    font-size: 32px;
  }

  .prompt-side-actions,
  .prompt-filter-bar,
  .prompt-form-grid,
  .version-option-grid {
    grid-template-columns: 1fr;
  }
}
</style>
