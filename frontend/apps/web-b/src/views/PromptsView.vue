<script setup>
import { computed, onMounted, reactive, ref } from "vue";
import {
  createPrompt,
  createPromptCategory,
  createPromptVersion,
  deletePrompt,
  deletePromptCategory,
  flattenTree,
  formatDateTime,
  getPromptDetail,
  listPromptCategories,
  pagePrompts,
  prettyJson,
  publishPromptVersion,
  rollbackPromptVersion,
  updatePrompt,
  updatePromptCategory
} from "@shared";
import { ElMessage, ElMessageBox } from "element-plus";
import {
  ChatDotRound,
  CircleCheck,
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

const categoryOptions = computed(() => flattenTree(categories.value).map((item) => ({
  label: `${"　".repeat(item.level)}${item.categoryName}`,
  value: item.id
})));

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
  return status === 1 ? "已发布" : "草稿";
}

async function loadCategories() {
  categoryLoading.value = true;
  try {
    categories.value = await listPromptCategories();
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
      records: pageData.records || [],
      total: pageData.total || 0,
      current: pageData.current || promptPage.value.current,
      size: pageData.size || promptPage.value.size
    };
    if (selectedPromptId.value) {
      const stillExists = promptPage.value.records.some((item) => item.id === selectedPromptId.value);
      if (!stillExists) {
        promptDetail.value = null;
        selectedPromptId.value = null;
      }
    }
  } finally {
    loading.value = false;
  }
}

async function loadPromptDetail(promptId) {
  if (!promptId) return;
  detailLoading.value = true;
  selectedPromptId.value = promptId;
  try {
    promptDetail.value = await getPromptDetail(promptId);
  } finally {
    detailLoading.value = false;
  }
}

function handleCategoryNodeClick(node) {
  selectedCategoryId.value = node?.id;
  loadPrompts(true);
}

function resetCategoryFilter() {
  selectedCategoryId.value = undefined;
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
  const selected = flattenTree(categories.value).find((item) => item.id === selectedCategoryId.value);
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
  const selected = flattenTree(categories.value).find((item) => item.id === selectedCategoryId.value);
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
  const prompt = row || promptDetail.value;
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
      const created = await createPrompt(payload);
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
  const prompt = row || promptDetail.value;
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

function openCreateVersionDialog() {
  if (!promptDetail.value?.id) {
    ElMessage.warning("请先选择提示词");
    return;
  }
  Object.assign(versionForm, {
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
  await ElMessageBox.confirm(`确认发布 V${version.versionNo} 吗？`, "发布版本", { type: "warning" });
  await publishPromptVersion(version.promptId, version.id);
  ElMessage.success("版本已发布");
  await loadPromptDetail(version.promptId);
  await loadPrompts();
}

async function rollbackVersion(version) {
  await ElMessageBox.confirm(`确认回滚到 V${version.versionNo} 吗？`, "回滚版本", { type: "warning" });
  await rollbackPromptVersion(version.promptId, version.id);
  ElMessage.success("已完成回滚");
  await loadPromptDetail(version.promptId);
  await loadPrompts();
}

function handlePageChange(current) {
  promptPage.value.current = current;
  loadPrompts();
}

onMounted(async () => {
  await loadCategories();
  await loadPrompts(true);
});
</script>

<template>
  <section class="ly-page-stack">
    <div class="ly-kpi-grid">
      <div class="ly-kpi-item">
        <span>分类总数</span>
        <strong>{{ promptStats.categoryCount }}</strong>
        <p>支持树形分类，用于区分客服、问诊和 RAG 系统提示词。</p>
      </div>
      <div class="ly-kpi-item">
        <span>提示词总数</span>
        <strong>{{ promptStats.promptCount }}</strong>
        <p>当前按筛选条件返回的提示词主档数量。</p>
      </div>
      <div class="ly-kpi-item">
        <span>启用中</span>
        <strong>{{ promptStats.enabledCount }}</strong>
        <p>处于启用状态，可继续进入版本治理与上线流程。</p>
      </div>
      <div class="ly-kpi-item">
        <span>已挂发布版本</span>
        <strong>{{ promptStats.publishedCount }}</strong>
        <p>主档已经绑定已发布版本，可被运行时消费。</p>
      </div>
    </div>

    <div class="prompt-layout">
      <el-card class="ly-panel-card prompt-sidebar" shadow="never">
        <template #header>
          <div class="ly-page-toolbar">
            <div>
              <strong>分类树</strong>
              <p class="ly-page-subtle">按场景维护提示词归属，方便运营分治。</p>
            </div>
            <el-button :icon="RefreshRight" text @click="loadCategories" />
          </div>
        </template>

        <div class="prompt-side-actions">
          <el-button type="primary" plain :icon="FolderAdd" @click="openCreateCategoryDialog">新增分类</el-button>
          <el-button :icon="EditPen" @click="openEditCategoryDialog">编辑</el-button>
          <el-button :icon="Remove" @click="removeCategory">删除</el-button>
        </div>

        <el-button v-if="selectedCategoryId" text @click="resetCategoryFilter">清空分类过滤</el-button>

        <el-skeleton :loading="categoryLoading" animated>
          <template #template>
            <el-skeleton-item variant="rect" style="height: 280px; border-radius: 18px;" />
          </template>
          <el-tree
            :data="categories"
            node-key="id"
            :props="{ label: 'categoryName', children: 'children' }"
            highlight-current
            default-expand-all
            @node-click="handleCategoryNodeClick"
          />
        </el-skeleton>
      </el-card>

      <div class="prompt-main">
        <el-card class="ly-panel-card" shadow="never">
          <template #header>
            <div class="ly-page-toolbar">
              <div>
                <strong>提示词列表</strong>
                <p class="ly-page-subtle">按主档管理编码、业务场景和当前生效版本。</p>
              </div>
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

          <el-table
            v-loading="loading"
            :data="promptPage.records"
            stripe
            border
            @row-click="loadPromptDetail($event.id)"
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
                <el-button link type="primary" @click.stop="loadPromptDetail(row.id)">查看</el-button>
                <el-button link @click.stop="openEditPromptDialog(row)">编辑</el-button>
                <el-button link type="danger" @click.stop="removePrompt(row)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>

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
              <div>
                <strong>版本工作区</strong>
                <p class="ly-page-subtle">选中主档后可查看版本历史、参数配置与发布动作。</p>
              </div>
              <div style="display: flex; gap: 10px; flex-wrap: wrap;">
                <el-button :disabled="!promptDetail" @click="openEditPromptDialog()">编辑主档</el-button>
                <el-button type="primary" :icon="Plus" :disabled="!promptDetail" @click="openCreateVersionDialog">
                  新建版本
                </el-button>
              </div>
            </div>
          </template>

          <el-skeleton :loading="detailLoading" animated>
            <template #template>
              <el-skeleton-item variant="rect" style="height: 260px; border-radius: 18px;" />
            </template>

            <el-empty v-if="!promptDetail" description="点击列表中的提示词查看版本与参数" />

            <template v-else>
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

              <el-table :data="promptDetail.versions || []" border style="margin-top: 18px;">
                <el-table-column label="版本" width="90">
                  <template #default="{ row }">V{{ row.versionNo }}</template>
                </el-table-column>
                <el-table-column label="状态" width="120">
                  <template #default="{ row }">
                    <el-tag :type="row.status === 1 ? 'success' : 'info'">{{ versionStatusText(row.status) }}</el-tag>
                  </template>
                </el-table-column>
                <el-table-column label="模型参数" min-width="260">
                  <template #default="{ row }">
                    <div class="ly-inline-meta">
                      <span>model：{{ row.modelConfig?.model || "--" }}</span>
                      <span>temperature：{{ row.modelConfig?.temperature ?? "--" }}</span>
                      <span>topP：{{ row.modelConfig?.topP ?? "--" }}</span>
                      <span>maxTokens：{{ row.modelConfig?.maxTokens ?? "--" }}</span>
                    </div>
                  </template>
                </el-table-column>
                <el-table-column label="创建时间" width="180">
                  <template #default="{ row }">{{ formatDateTime(row.createdAt) }}</template>
                </el-table-column>
                <el-table-column label="发布时间" width="180">
                  <template #default="{ row }">{{ formatDateTime(row.publishedAt) }}</template>
                </el-table-column>
                <el-table-column label="操作" width="160">
                  <template #default="{ row }">
                    <el-button v-if="row.status !== 1" link type="primary" @click="publishVersion(row)">发布</el-button>
                    <el-button link @click="rollbackVersion(row)">回滚</el-button>
                  </template>
                </el-table-column>
              </el-table>
            </template>
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

    <el-dialog v-model="versionDialogVisible" title="新建版本" width="860px">
      <el-form ref="versionFormRef" :model="versionForm" :rules="versionRules" label-position="top">
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
        <el-button type="primary" :loading="versionSaving" @click="submitVersion">创建版本</el-button>
      </template>
    </el-dialog>
  </section>
</template>

<style scoped>
.prompt-layout {
  display: grid;
  grid-template-columns: 300px minmax(0, 1fr);
  gap: 18px;
}

.prompt-sidebar {
  align-self: start;
}

.prompt-side-actions,
.prompt-filter-bar,
.prompt-form-grid,
.version-option-grid {
  display: grid;
  gap: 12px;
}

.prompt-filter-bar {
  grid-template-columns: minmax(0, 1fr) auto;
  margin-bottom: 18px;
}

.prompt-main {
  display: grid;
  gap: 18px;
}

.prompt-pagination {
  display: flex;
  justify-content: flex-end;
  margin-top: 18px;
}

.prompt-detail-card {
  margin-bottom: 8px;
}

.prompt-form-grid {
  grid-template-columns: repeat(2, minmax(0, 1fr));
}

.version-option-grid {
  grid-template-columns: repeat(2, minmax(0, 1fr));
}

@media (max-width: 1180px) {
  .prompt-layout {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 860px) {
  .prompt-filter-bar,
  .prompt-form-grid,
  .version-option-grid {
    grid-template-columns: 1fr;
  }
}
</style>
