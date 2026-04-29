import { request } from "./http";

export function listPromptCategories() {
  return request("/api/ai/admin/prompt-categories");
}

export function getPromptCategory(id) {
  return request(`/api/ai/admin/prompt-categories/${id}`);
}

export function createPromptCategory(payload) {
  return request("/api/ai/admin/prompt-categories", {
    method: "POST",
    body: payload
  });
}

export function updatePromptCategory(id, payload) {
  return request(`/api/ai/admin/prompt-categories/${id}`, {
    method: "PUT",
    body: payload
  });
}

export function deletePromptCategory(id) {
  return request(`/api/ai/admin/prompt-categories/${id}`, {
    method: "DELETE"
  });
}

export function pagePrompts(params) {
  return request("/api/ai/admin/prompts", { params });
}

export function getPromptDetail(id) {
  return request(`/api/ai/admin/prompts/${id}`);
}

export function createPrompt(payload) {
  return request("/api/ai/admin/prompts", {
    method: "POST",
    body: payload
  });
}

export function updatePrompt(id, payload) {
  return request(`/api/ai/admin/prompts/${id}`, {
    method: "PUT",
    body: payload
  });
}

export function deletePrompt(id) {
  return request(`/api/ai/admin/prompts/${id}`, {
    method: "DELETE"
  });
}

export function listPromptVersions(id) {
  return request(`/api/ai/admin/prompts/${id}/versions`);
}

export function createPromptVersion(id, payload) {
  return request(`/api/ai/admin/prompts/${id}/versions`, {
    method: "POST",
    body: payload
  });
}

export function deletePromptVersion(promptId, versionId) {
  return request(`/api/ai/admin/prompts/${promptId}/versions/${versionId}`, {
    method: "DELETE"
  });
}

export function publishPromptVersion(promptId, versionId) {
  return request(`/api/ai/admin/prompts/${promptId}/versions/${versionId}/publish`, {
    method: "POST"
  });
}

export function rollbackPromptVersion(promptId, versionId) {
  return request(`/api/ai/admin/prompts/${promptId}/versions/${versionId}/rollback`, {
    method: "POST"
  });
}

export function pageKnowledgeBases(params) {
  return request("/api/ai/admin/kbs", { params });
}

export function getKnowledgeBase(id) {
  return request(`/api/ai/admin/kbs/${id}`);
}

export function createKnowledgeBase(payload) {
  return request("/api/ai/admin/kbs", {
    method: "POST",
    body: payload
  });
}

export function updateKnowledgeBase(id, payload) {
  return request(`/api/ai/admin/kbs/${id}`, {
    method: "PUT",
    body: payload
  });
}

export function deleteKnowledgeBase(id) {
  return request(`/api/ai/admin/kbs/${id}`, {
    method: "DELETE"
  });
}

export function listKnowledgeFiles(kbId) {
  return request(`/api/ai/admin/kbs/${kbId}/files`);
}

export function getKnowledgeFile(kbId, fileId) {
  return request(`/api/ai/admin/kbs/${kbId}/files/${fileId}`);
}

export function uploadKnowledgeFile(kbId, file) {
  const formData = new FormData();
  formData.append("file", file);
  return request(`/api/ai/admin/kbs/${kbId}/files`, {
    method: "POST",
    body: formData
  });
}

export function deleteKnowledgeFile(kbId, fileId) {
  return request(`/api/ai/admin/kbs/${kbId}/files/${fileId}`, {
    method: "DELETE"
  });
}

export function parseKnowledgeFile(kbId, fileId) {
  return request(`/api/ai/admin/kbs/${kbId}/files/${fileId}/parse`, {
    method: "POST"
  });
}

export function indexKnowledgeFile(kbId, fileId) {
  return request(`/api/ai/admin/kbs/${kbId}/files/${fileId}/index`, {
    method: "POST"
  });
}

export function listKnowledgeChunks(kbId, fileId) {
  return request(`/api/ai/admin/kbs/${kbId}/files/${fileId}/chunks`);
}
