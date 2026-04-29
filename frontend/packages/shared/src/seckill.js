import {
  attemptSeckill,
  getCurrentSeckillRecord,
  getSeckillActivitySkuDetail,
  getSeckillRecord,
  listCurrentSeckillActivities
} from "./http";

export const SeckillActivityStatus = Object.freeze({
  UPCOMING: "upcoming",
  ACTIVE: "active",
  SOLD_OUT: "sold_out",
  ENDED: "ended"
});

export const SeckillRequestStatus = Object.freeze({
  IDLE: "idle",
  SUBMITTING: "submitting",
  QUEUEING: "queueing",
  SUCCESS: "success",
  FAILED: "failed"
});

/**
 * @typedef {Object} SeckillListItem
 * @property {string} activityId
 * @property {string} activitySkuId
 * @property {string} spuId
 * @property {string} skuId
 * @property {string} name
 * @property {string} title
 * @property {string} image
 * @property {number} seckillPrice
 * @property {number} originPrice
 * @property {number} stockAvailable
 * @property {number} limitPerUser
 * @property {string} startTime
 * @property {string} endTime
 * @property {string} activityStatus
 */

/**
 * @typedef {SeckillListItem & {
 *   rules: string[]
 * }} SeckillDetailModel
 */

function toNumber(value, fallback = 0) {
  const parsed = Number(value);
  return Number.isFinite(parsed) ? parsed : fallback;
}

function toText(value, fallback = "") {
  return typeof value === "string" && value.trim() ? value.trim() : fallback;
}

function toTimestamp(value) {
  if (!value) {
    return 0;
  }
  const ts = new Date(value).getTime();
  return Number.isFinite(ts) ? ts : 0;
}

export function resolveSeckillActivityStatus(source, now = Date.now()) {
  const startAt = toTimestamp(source?.startTime);
  const endAt = toTimestamp(source?.endTime);
  const stockAvailable = toNumber(source?.stockAvailable);

  if (startAt && now < startAt) {
    return SeckillActivityStatus.UPCOMING;
  }
  if (endAt && now >= endAt) {
    return SeckillActivityStatus.ENDED;
  }
  if (stockAvailable <= 0) {
    return SeckillActivityStatus.SOLD_OUT;
  }
  return SeckillActivityStatus.ACTIVE;
}

export function getSeckillListButtonText(status) {
  switch (status) {
    case SeckillActivityStatus.UPCOMING:
      return "即将开始";
    case SeckillActivityStatus.ENDED:
      return "已结束";
    case SeckillActivityStatus.SOLD_OUT:
      return "已抢光";
    default:
      return "查看秒杀";
  }
}

export function getSeckillStatusText(status) {
  switch (status) {
    case SeckillActivityStatus.UPCOMING:
      return "未开始";
    case SeckillActivityStatus.ENDED:
      return "已结束";
    case SeckillActivityStatus.SOLD_OUT:
      return "已抢光";
    default:
      return "进行中";
  }
}

function normalizeSeckillItem(source) {
  const item = {
    activityId: String(source.activityId || source.id || ""),
    activitySkuId: String(source.activitySkuId || ""),
    spuId: String(source.spuId || ""),
    skuId: String(source.skuId || ""),
    name: toText(source.name || source.spuName, "秒杀商品"),
    title: toText(source.title || source.skuTitle || source.name || source.spuName, "秒杀商品"),
    image: toText(source.image || source.mainImage),
    seckillPrice: toNumber(source.seckillPrice),
    originPrice: toNumber(source.originPrice),
    stockAvailable: toNumber(source.stockAvailable),
    limitPerUser: toNumber(source.limitPerUser, 1),
    startTime: source.startTime || "",
    endTime: source.endTime || "",
    activityStatus: SeckillActivityStatus.ACTIVE
  };
  item.activityStatus = resolveSeckillActivityStatus(item);
  return item;
}

export function normalizeSeckillList(activities = []) {
  return (activities || [])
    .flatMap((activity) =>
      (activity?.skus || []).map((sku) =>
        normalizeSeckillItem({
          activityId: activity.id,
          activitySkuId: sku.activitySkuId,
          spuId: sku.spuId,
          skuId: sku.skuId,
          name: sku.spuName || activity.name,
          title: sku.skuTitle || sku.spuName || activity.name,
          image: sku.mainImage,
          seckillPrice: sku.seckillPrice,
          originPrice: sku.originPrice,
          stockAvailable: sku.stockAvailable,
          limitPerUser: sku.limitPerUser,
          startTime: activity.startTime,
          endTime: activity.endTime
        })
      )
    )
    .sort((left, right) => toTimestamp(left.startTime) - toTimestamp(right.startTime));
}

export function normalizeSeckillDetail(detail) {
  return {
    ...normalizeSeckillItem(detail || {}),
    rules: [
      "秒杀商品仅限活动期间购买，超时后将恢复普通展示。",
      "每个用户按活动限购数量参与，重复请求会被系统拦截。",
      "提交秒杀后将进入排队处理，请耐心等待结果返回。",
      "秒杀成功后请前往个人中心查看订单并完成后续支付。"
    ]
  };
}

export async function fetchSeckillList() {
  const activities = await listCurrentSeckillActivities();
  return normalizeSeckillList(activities);
}

export async function fetchSeckillDetail(activityId, activitySkuId) {
  const detail = await getSeckillActivitySkuDetail(activityId, activitySkuId);
  return normalizeSeckillDetail(detail);
}

export async function fetchCurrentSeckillRequest(activityId, activitySkuId) {
  return getCurrentSeckillRecord(activityId, activitySkuId);
}

export async function submitSeckillRequest(activityId, activitySkuId) {
  return attemptSeckill(activityId, activitySkuId);
}

export async function fetchSeckillRequestRecord(recordId) {
  return getSeckillRecord(recordId);
}

export function resolveSeckillRequestStatus(status) {
  const numericStatus =
    typeof status === "object" && status !== null ? toNumber(status.status, -1) : toNumber(status, -1);

  if (numericStatus === 0) {
    return SeckillRequestStatus.QUEUEING;
  }
  if (numericStatus === 1 || numericStatus === 3) {
    return SeckillRequestStatus.SUCCESS;
  }
  if (numericStatus === 2 || numericStatus === 4) {
    return SeckillRequestStatus.FAILED;
  }
  return SeckillRequestStatus.IDLE;
}

export function isSeckillRequestTerminal(status) {
  const resolved =
    typeof status === "string" && Object.values(SeckillRequestStatus).includes(status)
      ? status
      : resolveSeckillRequestStatus(status);
  return resolved === SeckillRequestStatus.SUCCESS || resolved === SeckillRequestStatus.FAILED;
}
