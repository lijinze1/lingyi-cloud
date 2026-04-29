<script setup>
import { computed, onMounted, reactive, ref } from "vue";
import {
  createKnowledgeBase,
  deleteKnowledgeBase,
  deleteKnowledgeFile,
  formatDateTime,
  formatFileSize,
  getKnowledgeBase,
  indexKnowledgeFile,
  listKnowledgeChunks,
  listKnowledgeFiles,
  pageKnowledgeBases,
  parseKnowledgeFile,
  prettyJson,
  updateKnowledgeBase,
  uploadKnowledgeFile
} from "@shared";
import { ElMessage, ElMessageBox } from "element-plus";
import {
  Plus,
  RefreshRight,
  Search,
  Upload
} from "@element-plus/icons-vue";

const loading = ref(false);
const fileLoading = ref(false);
const chunkLoading = ref(false);

const kbPage = ref({
  records: [],
  total: 0,
  current: 1,
  size: 8
});
const selectedKbId = ref();
const selectedKb = ref(null);

const filters = reactive({
  keyword: ""
});

const files = ref([]);
const chunkDrawerVisible = ref(false);
const selectedFile = ref(null);
const chunks = ref([]);

const kbDialogVisible = ref(false);
const kbDialogMode = ref("create");
const kbFormRef = ref();
const kbSaving = ref(false);
const kbForm = reactive({
  id: null,
  kbCode: "",
  name: "",
  description: "",
  qdrantCollection: "lingyi_ai_kb_chunk_v1",
  embeddingModel: "text-embedding-v4",
  embeddingDimension: null,
  chunkSize: 500,
  chunkOverlap: 50,
  status: 1
});

const kbRules = {
  kbCode: [{ required: true, message: "请输入知识库编码", trigger: "blur" }],
  name: [{ required: true, message: "请输入知识库名称", trigger: "blur" }],
  qdrantCollection: [{ required: true, message: "请输入集合名", trigger: "blur" }],
  embeddingModel: [{ required: true, message: "请输入 embedding 模型", trigger: "blur" }]
};

const kbStats = computed(() => {
  const records = kbPage.value.records || [];
  return {
    kbCount: kbPage.value.total || records.length,
    enabledCount: records.filter((item) => item.status === 1).length,
    fileCount: files.value.length,
    indexedCount: files.value.filter((item) => item.indexStatus === 1).length
  };
});

function kbStatusText(status) {
  return status === 1 ? "启用中" : "已停用";
}

function kbStatusTag(status) {
  return status === 1 ? "success" : "info";
}

function parseStatusTag(status) {
  if (status === 1) return "success";
  if (status === 2) return "danger";
  return "info";
}

function parseStatusText(status) {
  if (status === 1) return "已解析";
  if (status === 2) return "解析失败";
  return "待解析";
}

function indexStatusText(status) {
  if (status === 1) return "已索引";
  if (status === 2) return "索引失败";
  return "待索引";
}

async function loadKnowledgeBases(reset = false) {
  loading.value = true;
  if (reset) {
    kbPage.value.current = 1;
  }
  try {
    const data = await pageKnowledgeBases({
      current: kbPage.value.current,
      size: kbPage.value.size,
      keyword: filters.keyword
    });
    kbPage.value = {
      records: data.records || [],
      total: data.total || 0,
      current: data.current || kbPage.value.current,
      size: data.size || kbPage.value.size
    };

    if (!selectedKbId.value && kbPage.value.records.length) {
      await selectKnowledgeBase(kbPage.value.records[0].id);
    } else if (selectedKbId.value) {
      const existing = kbPage.value.records.find((item) => item.id === selectedKbId.value);
      if (!existing) {
        selectedKbId.value = undefined;
        selectedKb.value = null;
        files.value = [];
      }
    }
  } finally {
    loading.value = false;
  }
}

async function selectKnowledgeBase(id) {
  if (!id) return;
  selectedKbId.value = id;
  selectedKb.value = await getKnowledgeBase(id);
  await loadFiles();
}

async function loadFiles() {
  if (!selectedKbId.value) return;
  fileLoading.value = true;
  try {
    files.value = await listKnowledgeFiles(selectedKbId.value);
  } finally {
    fileLoading.value = false;
  }
}

function openCreateKbDialog() {
  kbDialogMode.value = "create";
  Object.assign(kbForm, {
    id: null,
    kbCode: "",
    name: "",
    description: "",
    qdrantCollection: "lingyi_ai_kb_chunk_v1",
    embeddingModel: "text-embedding-v4",
    embeddingDimension: null,
    chunkSize: 500,
    chunkOverlap: 50,
    status: 1
  });
  kbDialogVisible.value = true;
}

function openEditKbDialog(row) {
  const kb = row || selectedKb.value;
  if (!kb) {
    ElMessage.warning("请先选择知识库");
    return;
  }
  kbDialogMode.value = "edit";
  Object.assign(kbForm, {
    id: kb.id,
    kbCode: kb.kbCode,
    name: kb.name,
    description: kb.description || "",
    qdrantCollection: kb.qdrantCollection,
    embeddingModel: kb.embeddingModel,
    embeddingDimension: kb.embeddingDimension,
    chunkSize: kb.chunkSize,
    chunkOverlap: kb.chunkOverlap,
    status: kb.status
  });
  kbDialogVisible.value = true;
}

async function submitKb() {
  await kbFormRef.value.validate();
  kbSaving.value = true;
  try {
    const payload = {
      kbCode: kbForm.kbCode,
      name: kbForm.name,
      description: kbForm.description,
      qdrantCollection: kbForm.qdrantCollection,
      embeddingModel: kbForm.embeddingModel,
      embeddingDimension: kbForm.embeddingDimension,
      chunkSize: kbForm.chunkSize,
      chunkOverlap: kbForm.chunkOverlap,
      status: kbForm.status
    };
    if (kbDialogMode.value === "create") {
      const created = await createKnowledgeBase(payload);
      ElMessage.success("知识库已创建");
      kbDialogVisible.value = false;
      await loadKnowledgeBases(true);
      await selectKnowledgeBase(created.id);
    } else {
      await updateKnowledgeBase(kbForm.id, payload);
      ElMessage.success("知识库已更新");
      kbDialogVisible.value = false;
      await loadKnowledgeBases();
      await selectKnowledgeBase(kbForm.id);
    }
  } finally {
    kbSaving.value = false;
  }
}

async function removeKb(row) {
  const kb = row || selectedKb.value;
  if (!kb) {
    ElMessage.warning("请先选择知识库");
    return;
  }
  await ElMessageBox.confirm(`确认删除知识库“${kb.name}”吗？`, "删除知识库", { type: "warning" });
  await deleteKnowledgeBase(kb.id);
  ElMessage.success("知识库已删除");
  selectedKbId.value = undefined;
  selectedKb.value = null;
  files.value = [];
  await loadKnowledgeBases(true);
}

async function handleUpload(options) {
  if (!selectedKbId.value) {
    ElMessage.warning("请先选择知识库");
    return;
  }
  try {
    await uploadKnowledgeFile(selectedKbId.value, options.file);
    ElMessage.success("文件已上传");
    await loadFiles();
  } catch (error) {
    ElMessage.error(error.message || "上传失败");
  }
}

async function handleParse(row) {
  await parseKnowledgeFile(selectedKbId.value, row.id);
  ElMessage.success("解析任务已完成");
  await loadFiles();
}

async function handleIndex(row) {
  await indexKnowledgeFile(selectedKbId.value, row.id);
  ElMessage.success("索引任务已完成");
  await loadFiles();
}

async function handleDeleteFile(row) {
  await ElMessageBox.confirm(`确认删除文件“${row.fileName}”吗？`, "删除文件", { type: "warning" });
  await deleteKnowledgeFile(selectedKbId.value, row.id);
  ElMessage.success("文件已删除");
  await loadFiles();
}

async function openChunkDrawer(row) {
  selectedFile.value = row;
  chunkDrawerVisible.value = true;
  chunkLoading.value = true;
  try {
    chunks.value = await listKnowledgeChunks(selectedKbId.value, row.id);
  } finally {
    chunkLoading.value = false;
  }
}

function handleKbPageChange(current) {
  kbPage.value.current = current;
  loadKnowledgeBases();
}

onMounted(async () => {
  await loadKnowledgeBases(true);
});
</script>

<template>
  <section class="ly-page-stack">
    <el-card class="stats-shell" shadow="never">
      <div class="ly-kpi-grid">
        <div class="ly-kpi-item">
          <span>知识库总数</span>
          <strong>{{ kbStats.kbCount }}</strong>
        </div>
        <div class="ly-kpi-item">
          <span>启用中</span>
          <strong>{{ kbStats.enabledCount }}</strong>
        </div>
        <div class="ly-kpi-item">
          <span>当前文件数</span>
          <strong>{{ kbStats.fileCount }}</strong>
        </div>
        <div class="ly-kpi-item">
          <span>已完成索引</span>
          <strong>{{ kbStats.indexedCount }}</strong>
        </div>
      </div>
    </el-card>

    <div class="knowledge-layout">
      <el-card class="ly-panel-card" shadow="never">
        <template #header>
          <div class="ly-page-toolbar">
            <strong>知识库列表</strong>
            <div style="display: flex; gap: 10px; flex-wrap: wrap;">
              <el-button :icon="RefreshRight" @click="loadKnowledgeBases">刷新</el-button>
              <el-button type="primary" :icon="Plus" @click="openCreateKbDialog">新建知识库</el-button>
            </div>
          </div>
        </template>

        <div class="knowledge-filter-bar">
          <el-input
            v-model="filters.keyword"
            placeholder="搜索知识库名称 / 编码"
            clearable
            :prefix-icon="Search"
            @keyup.enter="loadKnowledgeBases(true)"
          />
          <el-button @click="loadKnowledgeBases(true)">查询</el-button>
        </div>

        <div class="knowledge-table-shell">
          <el-table v-loading="loading" :data="kbPage.records" border stripe @row-click="selectKnowledgeBase($event.id)">
            <el-table-column label="知识库" min-width="260">
              <template #default="{ row }">
                <div class="ly-table-title">
                  <strong>{{ row.name }}</strong>
                  <div class="ly-inline-meta">
                    <span>编码：{{ row.kbCode }}</span>
                    <span>集合：{{ row.qdrantCollection }}</span>
                  </div>
                </div>
              </template>
            </el-table-column>
            <el-table-column prop="embeddingModel" label="Embedding 模型" min-width="180" />
            <el-table-column label="切片参数" width="160">
              <template #default="{ row }">
                <span>{{ row.chunkSize }} / {{ row.chunkOverlap }}</span>
              </template>
            </el-table-column>
            <el-table-column label="状态" width="110">
              <template #default="{ row }">
                <el-tag :type="kbStatusTag(row.status)">{{ kbStatusText(row.status) }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="创建时间" width="180">
              <template #default="{ row }">{{ formatDateTime(row.createdAt) }}</template>
            </el-table-column>
            <el-table-column label="操作" width="170" fixed="right">
              <template #default="{ row }">
                <el-button link @click.stop="openEditKbDialog(row)">编辑</el-button>
                <el-button link type="primary" @click.stop="selectKnowledgeBase(row.id)">进入</el-button>
                <el-button link type="danger" @click.stop="removeKb(row)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
        </div>

        <div class="prompt-pagination">
          <el-pagination
            layout="total, prev, pager, next"
            :current-page="kbPage.current"
            :page-size="kbPage.size"
            :total="kbPage.total"
            @current-change="handleKbPageChange"
          />
        </div>
      </el-card>

      <el-card class="ly-panel-card" shadow="never">
        <template #header>
          <div class="ly-page-toolbar">
            <strong>{{ selectedKb?.name ? `${selectedKb.name} · 文件工作区` : "文件工作区" }}</strong>
            <div style="display: flex; gap: 10px; flex-wrap: wrap;">
              <el-upload
                :show-file-list="false"
                :http-request="handleUpload"
                accept=".txt,.md,.pdf,.docx"
                :disabled="!selectedKb"
              >
                <el-button type="primary" :icon="Upload" :disabled="!selectedKb">上传文件</el-button>
              </el-upload>
              <el-button :disabled="!selectedKb" @click="openEditKbDialog()">编辑知识库</el-button>
              <el-button :disabled="!selectedKb" type="danger" plain @click="removeKb()">删除知识库</el-button>
            </div>
          </div>
        </template>

        <template v-if="selectedKb">
          <div class="knowledge-workspace">
            <el-descriptions :column="3" border>
              <el-descriptions-item label="集合">{{ selectedKb.qdrantCollection }}</el-descriptions-item>
              <el-descriptions-item label="Embedding 模型">{{ selectedKb.embeddingModel }}</el-descriptions-item>
              <el-descriptions-item label="切片策略">{{ selectedKb.chunkSize }} / {{ selectedKb.chunkOverlap }}</el-descriptions-item>
              <el-descriptions-item label="说明" :span="3">{{ selectedKb.description || "--" }}</el-descriptions-item>
            </el-descriptions>

            <div class="knowledge-upload-bar">
              <div class="knowledge-upload-tip">
                <strong>文档上传</strong>
                <span>支持 `txt / md / pdf / docx`。上传后可继续执行“解析”和“索引”。</span>
              </div>
              <el-upload :show-file-list="false" :http-request="handleUpload" accept=".txt,.md,.pdf,.docx">
                <el-button type="primary" :icon="Upload">选择并上传文件</el-button>
              </el-upload>
            </div>

            <div class="knowledge-table-shell">
              <el-table v-loading="fileLoading" :data="files" border>
                <el-table-column label="文件" min-width="250">
                  <template #default="{ row }">
                    <div class="ly-table-title">
                      <strong>{{ row.fileName }}</strong>
                      <div class="ly-inline-meta">
                        <span>{{ row.fileType }}</span>
                        <span>{{ formatFileSize(row.fileSize) }}</span>
                      </div>
                    </div>
                  </template>
                </el-table-column>
                <el-table-column label="解析状态" width="120">
                  <template #default="{ row }">
                    <el-tag :type="parseStatusTag(row.parseStatus)">{{ parseStatusText(row.parseStatus) }}</el-tag>
                  </template>
                </el-table-column>
                <el-table-column label="索引状态" width="120">
                  <template #default="{ row }">
                    <el-tag :type="parseStatusTag(row.indexStatus)">{{ indexStatusText(row.indexStatus) }}</el-tag>
                  </template>
                </el-table-column>
                <el-table-column label="失败原因" min-width="180" show-overflow-tooltip>
                  <template #default="{ row }">{{ row.failureReason || "--" }}</template>
                </el-table-column>
                <el-table-column label="上传时间" width="180">
                  <template #default="{ row }">{{ formatDateTime(row.createdAt) }}</template>
                </el-table-column>
                <el-table-column label="操作" width="240" fixed="right">
                  <template #default="{ row }">
                    <el-button link type="primary" @click="openChunkDrawer(row)">切片</el-button>
                    <el-button link @click="handleParse(row)">解析</el-button>
                    <el-button link @click="handleIndex(row)">索引</el-button>
                    <el-button link type="danger" @click="handleDeleteFile(row)">删除</el-button>
                  </template>
                </el-table-column>
              </el-table>
            </div>
          </div>
        </template>

        <el-empty v-else description="先新建或选中一个知识库，再上传和管理文档" />
      </el-card>
    </div>

    <el-dialog
      v-model="kbDialogVisible"
      :title="kbDialogMode === 'create' ? '新建知识库' : '编辑知识库'"
      width="720px"
    >
      <el-form ref="kbFormRef" :model="kbForm" :rules="kbRules" label-position="top">
        <div class="knowledge-form-grid">
          <el-form-item label="知识库编码" prop="kbCode">
            <el-input v-model="kbForm.kbCode" />
          </el-form-item>
          <el-form-item label="知识库名称" prop="name">
            <el-input v-model="kbForm.name" />
          </el-form-item>
        </div>
        <el-form-item label="说明">
          <el-input v-model="kbForm.description" type="textarea" :rows="3" />
        </el-form-item>
        <div class="knowledge-form-grid">
          <el-form-item label="Qdrant Collection" prop="qdrantCollection">
            <el-input v-model="kbForm.qdrantCollection" />
          </el-form-item>
          <el-form-item label="Embedding 模型" prop="embeddingModel">
            <el-input v-model="kbForm.embeddingModel" />
          </el-form-item>
          <el-form-item label="向量维度">
            <el-input-number v-model="kbForm.embeddingDimension" :min="1" />
          </el-form-item>
          <el-form-item label="Chunk Size">
            <el-input-number v-model="kbForm.chunkSize" :min="100" :step="50" />
          </el-form-item>
          <el-form-item label="Chunk Overlap">
            <el-input-number v-model="kbForm.chunkOverlap" :min="0" :step="10" />
          </el-form-item>
          <el-form-item label="启用状态">
            <el-switch v-model="kbForm.status" :active-value="1" :inactive-value="0" />
          </el-form-item>
        </div>
      </el-form>
      <template #footer>
        <el-button @click="kbDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="kbSaving" @click="submitKb">保存</el-button>
      </template>
    </el-dialog>

    <el-drawer v-model="chunkDrawerVisible" size="62%" :title="selectedFile?.fileName || '切片预览'">
      <div class="chunk-drawer-body">
        <el-descriptions v-if="selectedFile" :column="2" border>
          <el-descriptions-item label="文件类型">{{ selectedFile.fileType }}</el-descriptions-item>
          <el-descriptions-item label="文件大小">{{ formatFileSize(selectedFile.fileSize) }}</el-descriptions-item>
          <el-descriptions-item label="解析状态">{{ parseStatusText(selectedFile.parseStatus) }}</el-descriptions-item>
          <el-descriptions-item label="索引状态">{{ indexStatusText(selectedFile.indexStatus) }}</el-descriptions-item>
        </el-descriptions>

        <el-table v-loading="chunkLoading" :data="chunks" border style="margin-top: 18px;">
          <el-table-column label="#" width="80">
            <template #default="{ row }">{{ row.chunkIndex }}</template>
          </el-table-column>
          <el-table-column label="来源" width="120">
            <template #default="{ row }">{{ row.sourceType || "--" }}</template>
          </el-table-column>
          <el-table-column label="页码" width="90">
            <template #default="{ row }">{{ row.pageNo || "--" }}</template>
          </el-table-column>
          <el-table-column label="Token" width="90">
            <template #default="{ row }">{{ row.tokenCount || "--" }}</template>
          </el-table-column>
          <el-table-column label="Vector ID" min-width="180" show-overflow-tooltip>
            <template #default="{ row }">{{ row.vectorId || "--" }}</template>
          </el-table-column>
          <el-table-column label="内容" min-width="320">
            <template #default="{ row }">
              <div class="chunk-content">{{ row.content }}</div>
            </template>
          </el-table-column>
          <el-table-column label="Metadata" min-width="280">
            <template #default="{ row }">
              <pre class="chunk-meta">{{ prettyJson(row.metadataJson) }}</pre>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </el-drawer>
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

.knowledge-layout,
.knowledge-form-grid {
  display: grid;
  gap: 18px;
}

.knowledge-workspace {
  display: grid;
  gap: 18px;
}

.knowledge-upload-bar {
  display: flex;
  justify-content: space-between;
  gap: 16px;
  align-items: center;
  padding: 18px 0;
  flex-wrap: wrap;
}

.knowledge-upload-tip {
  display: grid;
  gap: 6px;
  color: #241912;
}

.knowledge-upload-tip strong {
  font-size: 15px;
}

.knowledge-upload-tip span {
  color: rgba(36, 25, 18, 0.64);
  font-size: 13px;
  line-height: 1.7;
}

.knowledge-filter-bar {
  display: grid;
  grid-template-columns: minmax(0, 1fr) auto;
  gap: 12px;
}

.knowledge-form-grid {
  grid-template-columns: repeat(2, minmax(0, 1fr));
}

.knowledge-table-shell {
  overflow: hidden;
  border-radius: 22px;
  border: 1px solid rgba(90, 56, 35, 0.08);
}

.knowledge-table-shell :deep(.el-table) {
  width: 100%;
}

.chunk-drawer-body {
  display: grid;
  gap: 18px;
}

.chunk-content,
.chunk-meta {
  margin: 0;
  white-space: pre-wrap;
  word-break: break-word;
  line-height: 1.7;
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

  .knowledge-filter-bar,
  .knowledge-form-grid {
    grid-template-columns: 1fr;
  }
}
</style>
