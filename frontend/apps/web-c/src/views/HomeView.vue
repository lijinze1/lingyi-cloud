<script setup>
import { computed, onBeforeUnmount, onMounted, ref } from "vue";
import { RouterLink, useRouter } from "vue-router";
import { consultDiagnosis, getToken, getUser, recommendProducts } from "@shared";
import searchStagePoster from "@shared/assets/search-stage-poster.svg";

const router = useRouter();

const query = ref("");
const diagnosisInput = ref("");
const diagnosisSessionId = ref(null);
const diagnosisMessages = ref([
  createMessage("assistant", [
    createTextPart("你好，我是灵医用药助手。你可以描述症状、咨询药品，也可以上传图片让我一起分析。")
  ])
]);
const selectedImages = ref([]);
const imageInputRef = ref(null);
const aiPending = ref(false);
const aiError = ref("");
const isDragActive = ref(false);

const recommendItems = ref([]);
const productLoading = ref(false);
const productError = ref("");

const isAuthed = computed(() => Boolean(getToken()));
const currentUserId = computed(() => {
  const user = getUser();
  return user?.id ?? user?.userId ?? null;
});
const hasDraft = computed(() => Boolean(diagnosisInput.value.trim()) || selectedImages.value.length > 0);
const productErrorText = computed(() => {
  if (!productError.value) {
    return "";
  }
  if (!isAuthed.value) {
    return "登录后可查看推荐药品";
  }
  return "商品服务暂不可用";
});
const searchPosterStyle = computed(() => ({
  backgroundImage: `linear-gradient(180deg, rgba(241, 250, 244, 0.12), rgba(223, 239, 228, 0.08)), url(${searchStagePoster})`
}));

let dragDepth = 0;

onMounted(async () => {
  productLoading.value = true;
  productError.value = "";
  try {
    const recommendPage = await recommendProducts({ pageNo: 1, pageSize: 8 });
    recommendItems.value = (recommendPage?.records || []).map((item) => ({
      id: item.id,
      name: item.name,
      category: item.categoryName || "常备药品",
      desc: item.subTitle || "适合家庭常备与日常复购。",
      image: item.mainImage,
      price: Number(item.minPrice || 0),
      stockAvailable: item.stockAvailable || 0
    }));
  } catch (error) {
    productError.value = error.message || "商品推荐暂时不可用";
  } finally {
    productLoading.value = false;
  }
});

onBeforeUnmount(() => {
  revokeImages(selectedImages.value);
});

function createMessage(role, parts) {
  return {
    id: `${role}-${Date.now()}-${Math.random().toString(16).slice(2)}`,
    role,
    parts
  };
}

function createTextPart(text) {
  return {
    id: `text-${Date.now()}-${Math.random().toString(16).slice(2)}`,
    type: "text",
    text
  };
}

function createImagePart(image) {
  return {
    id: image.id,
    type: "image",
    url: image.previewUrl,
    name: image.name,
    size: image.size
  };
}

function buildLoginRedirect() {
  return { path: "/login", query: { redirect: "/" } };
}

function ensureAuthed() {
  if (isAuthed.value) {
    return true;
  }
  router.push(buildLoginRedirect());
  return false;
}

function submitSearch() {
  const target = router.resolve({
    path: "/products",
    query: query.value.trim() ? { q: query.value.trim(), page: "1" } : { page: "1" }
  }).fullPath;

  if (!isAuthed.value) {
    router.push({ path: "/login", query: { redirect: target } });
    return;
  }

  router.push(target);
}

function pickImages() {
  if (!ensureAuthed()) {
    return;
  }
  imageInputRef.value?.click();
}

function handleImageChange(event) {
  appendImages(Array.from(event.target?.files || []));
  clearImageInput();
}

function appendImages(files) {
  if (!files.length) {
    return;
  }
  if (!ensureAuthed()) {
    return;
  }

  const nextImages = files
    .filter((file) => file && file.type.startsWith("image/"))
    .map((file, index) => ({
      id: `image-${Date.now()}-${index}-${Math.random().toString(16).slice(2)}`,
      file,
      name: file.name,
      size: file.size,
      previewUrl: URL.createObjectURL(file)
    }));

  if (!nextImages.length) {
    return;
  }

  selectedImages.value = [...selectedImages.value, ...nextImages];
}

function clearImageInput() {
  if (imageInputRef.value) {
    imageInputRef.value.value = "";
  }
}

function removeSelectedImage(imageId) {
  const target = selectedImages.value.find((image) => image.id === imageId);
  if (target) {
    URL.revokeObjectURL(target.previewUrl);
  }
  selectedImages.value = selectedImages.value.filter((image) => image.id !== imageId);
}

function revokeImages(images) {
  images.forEach((image) => {
    if (image?.previewUrl) {
      URL.revokeObjectURL(image.previewUrl);
    }
  });
}

function resetDraft() {
  revokeImages(selectedImages.value);
  selectedImages.value = [];
  diagnosisInput.value = "";
  clearImageInput();
  isDragActive.value = false;
  dragDepth = 0;
}

function formatFileSize(size) {
  if (!size) {
    return "0 KB";
  }
  if (size < 1024 * 1024) {
    return `${Math.max(1, Math.round(size / 1024))} KB`;
  }
  return `${(size / (1024 * 1024)).toFixed(1)} MB`;
}

function handleComposerKeydown(event) {
  if (event.key === "Enter" && !event.shiftKey && !event.isComposing) {
    event.preventDefault();
    sendDiagnosis();
  }
}

function handleComposerDragEnter() {
  if (!ensureAuthed()) {
    return;
  }
  dragDepth += 1;
  isDragActive.value = true;
}

function handleComposerDragOver() {
  if (!ensureAuthed()) {
    return;
  }
  isDragActive.value = true;
}

function handleComposerDragLeave() {
  dragDepth = Math.max(0, dragDepth - 1);
  if (dragDepth === 0) {
    isDragActive.value = false;
  }
}

function handleComposerDrop(event) {
  if (!ensureAuthed()) {
    return;
  }
  dragDepth = 0;
  isDragActive.value = false;
  appendImages(Array.from(event.dataTransfer?.files || []));
}

async function sendDiagnosis() {
  if (!ensureAuthed()) {
    return;
  }

  if (!currentUserId.value) {
    router.push(buildLoginRedirect());
    return;
  }

  const typedContent = diagnosisInput.value.trim();
  if (!typedContent && !selectedImages.value.length) {
    return;
  }

  const requestQuestion = typedContent || "请结合图片分析当前症状并给出用药建议";
  const displayQuestion = typedContent || "请结合图片帮我分析当前情况";
  const draftImages = selectedImages.value.slice();

  diagnosisMessages.value.push(
    createMessage("user", [
      createTextPart(displayQuestion),
      ...draftImages.map((image) => createImagePart(image))
    ])
  );

  aiPending.value = true;
  aiError.value = "";

  try {
    const reply = await consultDiagnosis({
      sessionId: diagnosisSessionId.value,
      userId: currentUserId.value,
      question: requestQuestion,
      images: draftImages.map((image) => image.file)
    });

    diagnosisSessionId.value = reply?.sessionId || diagnosisSessionId.value;
    diagnosisMessages.value.push(
      createMessage("assistant", [
        createTextPart(reply?.answer || "这次没有拿到有效回复，请稍后再试。")
      ])
    );
    resetDraft();
  } catch (error) {
    aiError.value = error.message || "问诊请求失败，请稍后重试";
    diagnosisMessages.value.push(
      createMessage("assistant", [
        createTextPart(`这次问诊没有成功完成，${aiError.value}`)
      ])
    );
  } finally {
    aiPending.value = false;
  }
}
</script>

<template>
  <section class="home-search-stage" :style="searchPosterStyle">
    <div class="home-search-bar">
      <input
        v-model="query"
        type="text"
        placeholder="搜索药品、症状或分类，例如感冒药 / 退热药 / 肠胃药"
        @keyup.enter="submitSearch"
      />
      <button type="button" @click="submitSearch">搜索</button>
    </div>
  </section>

  <section class="home-ai-panel">
    <div class="home-section-head">
      <div>
        <span>AI 用药助手</span>
      </div>
      <strong>{{ isAuthed ? "多模态问诊中" : "登录后可使用" }}</strong>
    </div>

    <div class="home-ai-thread">
      <article
        v-for="message in diagnosisMessages"
        :key="message.id"
        class="home-chat-row"
        :class="message.role"
      >
        <div v-if="message.role === 'assistant'" class="home-chat-avatar">AI</div>
        <div class="home-chat-bubble" :class="message.role">
          <div class="home-chat-parts">
            <template v-for="part in message.parts" :key="part.id">
              <p v-if="part.type === 'text'" class="home-chat-text">{{ part.text }}</p>
              <figure v-else class="home-chat-image-card">
                <img :src="part.url" :alt="part.name || '诊断图片'" class="home-chat-image" />
                <figcaption>{{ part.name }}</figcaption>
              </figure>
            </template>
          </div>
        </div>
      </article>
    </div>

    <p v-if="aiError" class="ly-error home-ai-error">{{ aiError }}</p>

    <div
      class="home-ai-composer"
      :class="{ 'is-drag-active': isDragActive, 'is-disabled': aiPending }"
      @dragenter.prevent="handleComposerDragEnter"
      @dragover.prevent="handleComposerDragOver"
      @dragleave.prevent="handleComposerDragLeave"
      @drop.prevent="handleComposerDrop"
    >
      <input
        ref="imageInputRef"
        class="home-ai-file-input"
        type="file"
        accept="image/*"
        multiple
        @change="handleImageChange"
      />

      <div v-if="selectedImages.length" class="home-ai-preview-strip">
        <article v-for="image in selectedImages" :key="image.id" class="home-ai-preview-card">
          <img :src="image.previewUrl" :alt="image.name" />
          <div>
            <strong>{{ image.name }}</strong>
            <span>{{ formatFileSize(image.size) }}</span>
          </div>
          <button type="button" aria-label="移除图片" @click="removeSelectedImage(image.id)">
            ×
          </button>
        </article>
      </div>

      <label v-if="isDragActive" class="home-ai-drop-mask">
        松手即可添加图片
      </label>

      <div class="home-ai-input-shell" @click="ensureAuthed()">
        <button
          type="button"
          class="home-ai-icon-btn home-ai-plus-btn"
          :disabled="aiPending"
          aria-label="上传图片"
          @click.stop="pickImages"
        >
          <svg viewBox="0 0 24 24" aria-hidden="true">
            <path d="M12 5v14M5 12h14" />
          </svg>
        </button>

        <textarea
          v-model="diagnosisInput"
          class="home-ai-textarea"
          :placeholder="
            isAuthed
              ? '有问题，尽管问。可拖拽图片，Shift + Enter 换行。'
              : '登录后可使用 AI 多模态问诊'
          "
          :disabled="aiPending"
          rows="1"
          @keydown="handleComposerKeydown"
        />

        <button
          type="button"
          class="home-ai-send-btn"
          :disabled="aiPending || !hasDraft"
          aria-label="发送消息"
          @click.stop="sendDiagnosis"
        >
          <svg viewBox="0 0 24 24" aria-hidden="true">
            <path d="M5 12h12" />
            <path d="M13 6l6 6-6 6" />
          </svg>
        </button>
      </div>

    </div>
  </section>

  <section class="home-products-panel">
    <div class="home-section-head home-section-head-compact">
      <div>
        <span>推荐药品</span>
        <h2>家庭常备清单</h2>
      </div>
      <RouterLink to="/products">查看全部</RouterLink>
    </div>

    <div v-if="recommendItems.length" class="home-product-grid">
      <article v-for="item in recommendItems" :key="item.id" class="home-product-card">
        <div class="home-product-image-shell">
          <img :src="item.image" :alt="item.name" class="home-product-image" />
        </div>
        <div class="home-product-meta-top">
          <span>{{ item.category }}</span>
          <span>{{ item.stockAvailable > 0 ? "现货" : "缺货" }}</span>
        </div>
        <h3>{{ item.name }}</h3>
        <p>{{ item.desc }}</p>
        <div class="home-product-meta-bottom">
          <strong>¥{{ item.price.toFixed(2) }}</strong>
          <RouterLink :to="`/products/${item.id}`">详情</RouterLink>
        </div>
      </article>
    </div>
    <p v-else-if="productLoading" class="ly-muted">推荐药品加载中...</p>
    <p v-else-if="productError" class="ly-muted">{{ productErrorText }}</p>
    <p v-else class="ly-muted">当前还没有推荐药品。</p>
  </section>
</template>

<style scoped>
.home-search-stage,
.home-ai-panel,
.home-products-panel,
.home-product-card {
  border-radius: 32px;
  background: rgba(255, 252, 248, 0.95);
  box-shadow: 0 20px 44px rgba(71, 44, 25, 0.08);
}

.home-search-stage {
  min-height: 168px;
  padding: 34px;
  background-position: center;
  background-repeat: no-repeat;
  background-size: cover;
  display: flex;
  align-items: center;
}

.home-search-bar {
  width: 100%;
  display: grid;
  grid-template-columns: 1fr auto;
  gap: 10px;
  padding: 10px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.94);
  box-shadow:
    0 18px 36px rgba(44, 89, 70, 0.1),
    inset 0 0 0 1px rgba(69, 116, 95, 0.08);
}

.home-section-head,
.home-product-meta-top,
.home-product-meta-bottom {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.home-section-head span {
  color: #d07232;
  letter-spacing: 0.12em;
  font-size: 13px;
}

.home-section-head h2 {
  margin: 10px 0 0;
  color: #241912;
  letter-spacing: -0.05em;
  font-size: 34px;
}

.home-section-head strong,
.home-product-meta-top span,
.home-product-card p {
  color: rgba(36, 25, 18, 0.62);
}

.home-search-bar input,
.home-search-bar button,
.home-ai-textarea,
.home-ai-icon-btn,
.home-ai-send-btn,
.home-ai-preview-card button {
  border: 0;
  font: inherit;
}

.home-search-bar input {
  background: transparent;
  color: #241912;
  padding: 0 16px;
}

.home-search-bar input:focus,
.home-ai-textarea:focus {
  outline: none;
}

.home-search-bar button {
  min-height: 52px;
  padding: 0 24px;
  border-radius: 999px;
  background: linear-gradient(135deg, #ff6d4d, #ff8d57);
  color: #fff9f4;
  font-weight: 700;
  cursor: pointer;
  box-shadow: 0 14px 24px rgba(255, 109, 77, 0.2);
}

.home-ai-panel,
.home-products-panel {
  margin-top: 20px;
  padding: 28px;
}

.home-ai-panel {
  display: grid;
  gap: 16px;
}

.home-ai-thread {
  display: grid;
  gap: 14px;
  min-height: 220px;
  padding: 8px 0 2px;
}

.home-chat-row {
  display: flex;
  align-items: flex-end;
  gap: 12px;
}

.home-chat-row.user {
  justify-content: flex-end;
}

.home-chat-avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  display: grid;
  place-items: center;
  background: linear-gradient(135deg, #ffeddc, #ffe7d8);
  color: #9b532c;
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 0.08em;
  flex-shrink: 0;
}

.home-chat-bubble {
  max-width: min(760px, 84%);
  padding: 16px 18px;
  border-radius: 24px;
  line-height: 1.8;
}

.home-chat-bubble.assistant {
  background: #fff7f1;
  color: #593623;
  box-shadow: inset 0 0 0 1px rgba(255, 155, 112, 0.12);
}

.home-chat-bubble.user {
  background: linear-gradient(135deg, #ff6d4d, #ff8d57);
  color: #fff9f4;
  box-shadow: 0 18px 34px rgba(255, 109, 77, 0.2);
}

.home-chat-parts {
  display: grid;
  gap: 10px;
  width: 100%;
}

.home-chat-text {
  margin: 0;
  white-space: pre-wrap;
  word-break: break-word;
}

.home-chat-image-card {
  margin: 0;
  display: grid;
  gap: 8px;
}

.home-chat-image-card figcaption {
  font-size: 12px;
  opacity: 0.82;
}

.home-chat-image {
  width: min(220px, 100%);
  border-radius: 18px;
  display: block;
  object-fit: cover;
  box-shadow: 0 10px 20px rgba(25, 18, 12, 0.14);
}

.home-ai-composer {
  position: relative;
  display: grid;
  gap: 12px;
  width: 100%;
  padding: 0;
  background: transparent;
  box-shadow: none;
  transition: transform 180ms ease;
}

.home-ai-composer.is-drag-active {
  transform: translateY(-1px);
}

.home-ai-composer.is-disabled {
  opacity: 0.82;
}

.home-ai-drop-mask {
  position: absolute;
  inset: 0;
  border-radius: 24px;
  border: 1.5px dashed rgba(255, 109, 77, 0.55);
  background: rgba(255, 247, 241, 0.88);
  display: grid;
  place-items: center;
  color: #b35523;
  font-weight: 600;
  letter-spacing: 0.04em;
  pointer-events: none;
  z-index: 2;
}

.home-ai-preview-strip {
  display: flex;
  gap: 10px;
  overflow-x: auto;
  padding: 0 4px 2px;
}

.home-ai-preview-card {
  min-width: 180px;
  display: grid;
  grid-template-columns: 52px minmax(0, 1fr) auto;
  gap: 10px;
  align-items: center;
  padding: 10px 12px;
  border-radius: 18px;
  background: #fff5ee;
  box-shadow: inset 0 0 0 1px rgba(255, 155, 112, 0.12);
}

.home-ai-preview-card img {
  width: 52px;
  height: 52px;
  border-radius: 14px;
  object-fit: cover;
  display: block;
}

.home-ai-preview-card div {
  min-width: 0;
  display: grid;
  gap: 2px;
}

.home-ai-preview-card strong,
.home-ai-preview-card span {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.home-ai-preview-card strong {
  color: #241912;
  font-size: 13px;
}

.home-ai-preview-card span {
  color: rgba(36, 25, 18, 0.56);
  font-size: 12px;
}

.home-ai-preview-card button {
  width: 28px;
  height: 28px;
  border-radius: 50%;
  background: rgba(36, 25, 18, 0.08);
  color: #7b492c;
  cursor: pointer;
}

.home-ai-input-shell {
  display: grid;
  grid-template-columns: auto minmax(0, 1fr) auto;
  align-items: center;
  gap: 12px;
  width: 100%;
  min-height: 64px;
  padding: 4px 4px 4px 8px;
  border-radius: 999px;
  background: #fff;
  box-shadow:
    0 20px 40px rgba(72, 44, 23, 0.08),
    inset 0 0 0 1px rgba(64, 49, 37, 0.08);
}

.home-ai-composer.is-drag-active .home-ai-input-shell {
  box-shadow:
    0 24px 46px rgba(255, 109, 77, 0.14),
    inset 0 0 0 1px rgba(255, 109, 77, 0.32);
}

.home-ai-file-input {
  display: none;
}

.home-ai-icon-btn,
.home-ai-send-btn {
  width: 44px;
  height: 44px;
  border-radius: 50%;
  display: grid;
  place-items: center;
  cursor: pointer;
  transition:
    transform 180ms ease,
    opacity 180ms ease,
    box-shadow 180ms ease;
}

.home-ai-icon-btn svg,
.home-ai-send-btn svg {
  width: 22px;
  height: 22px;
  fill: none;
  stroke-width: 1.9;
  stroke-linecap: round;
  stroke-linejoin: round;
}

.home-ai-plus-btn {
  background: transparent;
  color: #6f5a49;
  box-shadow: none;
}

.home-ai-plus-btn svg {
  stroke: currentColor;
}

.home-ai-send-btn {
  background: #181512;
  color: #fff8f2;
  box-shadow: 0 10px 22px rgba(35, 24, 17, 0.18);
}

.home-ai-send-btn svg {
  stroke: currentColor;
}

.home-ai-send-btn:disabled,
.home-ai-icon-btn:disabled,
.home-search-bar button:disabled {
  cursor: not-allowed;
  opacity: 0.42;
  box-shadow: none;
}

.home-ai-textarea {
  width: 100%;
  min-height: 24px;
  max-height: 120px;
  resize: none;
  background: transparent;
  color: #241912;
  line-height: 1.6;
  padding: 10px 0;
}

.home-ai-error {
  margin: 0;
}

.home-product-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 14px;
}

.home-product-card {
  padding: 18px;
}

.home-product-image-shell {
  height: 210px;
  margin-bottom: 14px;
  border-radius: 20px;
  overflow: hidden;
  background: linear-gradient(180deg, #fff8f0 0%, #f7eadf 100%);
}

.home-product-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}

.home-product-card h3 {
  margin: 8px 0 6px;
  color: #241912;
}

.home-product-card p {
  min-height: 62px;
  line-height: 1.7;
}

.home-product-meta-bottom strong {
  font-size: 28px;
  color: #241912;
}

.home-product-meta-bottom a,
.home-section-head-compact a {
  color: #a4562d;
  text-decoration: none;
}

@media (max-width: 1080px) {
  .home-product-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 760px) {
  .home-product-grid {
    grid-template-columns: 1fr;
  }

  .home-section-head,
  .home-product-meta-top,
  .home-product-meta-bottom {
    display: grid;
  }

  .home-search-bar {
    grid-template-columns: 1fr;
    border-radius: 28px;
  }

  .home-chat-bubble {
    max-width: 100%;
  }

  .home-ai-input-shell {
    grid-template-columns: auto minmax(0, 1fr) auto;
    gap: 10px;
  }

  .home-search-bar button {
    width: 100%;
  }
}
</style>
