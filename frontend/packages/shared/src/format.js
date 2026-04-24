export function formatDateTime(value) {
  if (!value) return "--";
  const date = typeof value === "string" || typeof value === "number" ? new Date(value) : value;
  if (Number.isNaN(date.getTime?.())) return "--";
  return new Intl.DateTimeFormat("zh-CN", {
    year: "numeric",
    month: "2-digit",
    day: "2-digit",
    hour: "2-digit",
    minute: "2-digit"
  }).format(date).replace(/\//g, "-");
}

export function formatFileSize(size) {
  if (!Number.isFinite(size) || size < 0) return "--";
  if (size < 1024) return `${size} B`;
  if (size < 1024 * 1024) return `${(size / 1024).toFixed(1)} KB`;
  if (size < 1024 * 1024 * 1024) return `${(size / 1024 / 1024).toFixed(1)} MB`;
  return `${(size / 1024 / 1024 / 1024).toFixed(1)} GB`;
}

export function safeJsonParse(value, fallback = null) {
  if (!value) return fallback;
  try {
    return JSON.parse(value);
  } catch {
    return fallback;
  }
}

export function prettyJson(value) {
  if (value == null || value === "") return "";
  if (typeof value === "string") {
    const parsed = safeJsonParse(value);
    return parsed == null ? value : JSON.stringify(parsed, null, 2);
  }
  return JSON.stringify(value, null, 2);
}

export function flattenTree(nodes, childKey = "children", level = 0, result = []) {
  (nodes || []).forEach((node) => {
    result.push({ ...node, level });
    flattenTree(node[childKey] || [], childKey, level + 1, result);
  });
  return result;
}
