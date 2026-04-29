package com.lingyi.service.seckill.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lingyi.common.web.domain.Result;
import com.lingyi.common.web.exception.BizException;
import com.lingyi.service.seckill.client.OrderClient;
import com.lingyi.service.seckill.client.ProductClient;
import com.lingyi.service.seckill.client.ProductSkuVO;
import com.lingyi.service.seckill.dto.InternalCreateSeckillOrderRequest;
import com.lingyi.service.seckill.entity.LyActivity;
import com.lingyi.service.seckill.entity.LyActivitySku;
import com.lingyi.service.seckill.entity.LySeckillRecord;
import com.lingyi.service.seckill.mapper.ActivityMapper;
import com.lingyi.service.seckill.mapper.ActivitySkuMapper;
import com.lingyi.service.seckill.mapper.SeckillRecordMapper;
import com.lingyi.service.seckill.mq.SeckillOrderMessage;
import com.lingyi.service.seckill.service.SeckillService;
import com.lingyi.service.seckill.vo.SeckillActivityVO;
import com.lingyi.service.seckill.vo.SeckillAttemptVO;
import com.lingyi.service.seckill.vo.SeckillDetailVO;
import com.lingyi.service.seckill.vo.LinkedOrderVO;
import com.lingyi.service.seckill.vo.SeckillOrderCreateVO;
import com.lingyi.service.seckill.vo.SeckillRecordVO;
import com.lingyi.service.seckill.vo.SeckillSkuVO;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Executor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.util.StringUtils;

@Slf4j
@Service
@RequiredArgsConstructor
public class SeckillServiceImpl implements SeckillService {

    private static final String SUCCESS_CODE = "00000";
    private static final int ACTIVITY_ONLINE = 1;
    private static final int SKU_ONLINE = 1;
    private static final int RECORD_PENDING = 0;
    private static final int RECORD_WAIT_PAY = 1;
    private static final int RECORD_FAILED = 2;
    private static final int RECORD_PAID = 3;
    private static final int RECORD_CANCELLED = 4;
    private static final int ORDER_WAIT_PAY = 10;
    private static final int ORDER_PAID = 20;
    private static final int ORDER_CANCELLED = 50;
    private static final int BUY_QUANTITY = 1;

    private final ActivityMapper activityMapper;
    private final ActivitySkuMapper activitySkuMapper;
    private final SeckillRecordMapper seckillRecordMapper;
    private final ProductClient productClient;
    private final OrderClient orderClient;
    private final StringRedisTemplate stringRedisTemplate;
    @Qualifier("seckillTaskExecutor")
    private final Executor seckillTaskExecutor;
    private final ObjectProvider<RocketMQTemplate> rocketMQTemplateProvider;

    @Value("${lingyi.seckill.async-mode:local}")
    private String asyncMode;

    @Value("${lingyi.seckill.topic:lingyi-seckill-create-order}")
    private String topic;

    @Value("${lingyi.seckill.pending-timeout-seconds:15}")
    private long pendingTimeoutSeconds;

    private final DefaultRedisScript<Long> attemptScript = buildAttemptScript();

    @Override
    public List<SeckillActivityVO> listCurrentActivities() {
        LocalDateTime now = LocalDateTime.now();
        List<LyActivity> activities = activityMapper.selectList(new LambdaQueryWrapper<LyActivity>()
                .eq(LyActivity::getStatus, ACTIVITY_ONLINE)
                .le(LyActivity::getStartTime, now)
                .ge(LyActivity::getEndTime, now)
                .orderByAsc(LyActivity::getStartTime));

        List<SeckillActivityVO> result = new ArrayList<>();
        for (LyActivity activity : activities) {
            SeckillActivityVO activityVO = new SeckillActivityVO();
            activityVO.setId(activity.getId());
            activityVO.setName(activity.getName());
            activityVO.setStartTime(activity.getStartTime());
            activityVO.setEndTime(activity.getEndTime());
            activityVO.setStatus(activity.getStatus());

            List<LyActivitySku> skuList = activitySkuMapper.selectList(new LambdaQueryWrapper<LyActivitySku>()
                    .eq(LyActivitySku::getActivityId, activity.getId())
                    .eq(LyActivitySku::getStatus, SKU_ONLINE)
                    .orderByAsc(LyActivitySku::getId));
            for (LyActivitySku activitySku : skuList) {
                ProductSkuVO sku = loadProductSku(activitySku.getSkuId());
                if (sku == null) {
                    continue;
                }
                activityVO.getSkus().add(toSkuVO(activity, activitySku, sku));
            }

            if (!activityVO.getSkus().isEmpty()) {
                result.add(activityVO);
            }
        }
        return result;
    }

    @Override
    public SeckillDetailVO getDetail(Long activityId, Long activitySkuId) {
        LyActivity activity = requireActivity(activityId);
        LyActivitySku activitySku = requireActivitySkuByActivitySkuId(activityId, activitySkuId);
        ProductSkuVO sku = loadProductSku(activitySku.getSkuId());
        if (sku == null) {
            throw new BizException("S0404", "秒杀商品不存在或已下架");
        }
        return toDetailVO(activity, activitySku, sku);
    }

    @Override
    public SeckillRecordVO getCurrentRecord(Long userId, Long activityId, Long activitySkuId) {
        LyActivity activity = requireActivity(activityId);
        LyActivitySku activitySku = requireActivitySkuByActivitySkuId(activityId, activitySkuId);
        LySeckillRecord record = findRecord(activity.getId(), activitySku.getSkuId(), userId);
        if (record == null) {
            return null;
        }
        record = reconcileLinkedOrderState(record, activitySku);
        refreshTimedOutPendingRecord(record);
        if (shouldRecoverPendingRecord(record)) {
            if (isAsyncMode()) {
                dispatchOrderCreation(record.getId());
            } else {
                processOrderMessage(buildOrderMessage(record.getId()));
            }
            record = seckillRecordMapper.selectById(record.getId());
        }
        return record == null ? null : toRecordVO(record);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SeckillAttemptVO attempt(Long userId, Long activityId, Long activitySkuId) {
        LyActivity activity = requireActivity(activityId);
        LyActivitySku activitySku = requireActivitySkuByActivitySkuId(activityId, activitySkuId);
        validateActivityWindow(activity);

        LySeckillRecord existing = findRecord(activityId, activitySku.getSkuId(), userId);
        if (existing != null) {
            existing = reconcileLinkedOrderState(existing, activitySku);
            refreshTimedOutPendingRecord(existing);
            if (shouldRecoverPendingRecord(existing)) {
                processOrderMessage(buildOrderMessage(existing.getId()));
                existing = seckillRecordMapper.selectById(existing.getId());
            }
            if (!canRetry(existing.getStatus())) {
                return buildAttempt(existing.getId(), existing.getStatus(), buildStatusText(existing.getStatus()));
            }
            clearUserAttemptMark(activitySkuId, userId);
        }

        Long luaResult = executeAttemptScript(activitySkuId, userId, activity.getEndTime());

        if (luaResult != null && luaResult == 2L) {
            LySeckillRecord latestRecord = findRecord(activityId, activitySku.getSkuId(), userId);
            if (latestRecord == null || canRetry(latestRecord.getStatus())) {
                clearUserAttemptMark(activitySkuId, userId);
                luaResult = executeAttemptScript(activitySkuId, userId, activity.getEndTime());
            }
        }

        if (luaResult == null) {
            throw new BizException("S0500", "秒杀系统繁忙，请稍后重试");
        }
        if (luaResult == 1L) {
            return buildAttempt(null, RECORD_FAILED, "今日秒杀已抢完");
        }
        if (luaResult == 2L) {
            return buildAttempt(null, RECORD_FAILED, "同一商品只能参与一次秒杀");
        }

        LySeckillRecord record = existing;
        if (record == null) {
            LySeckillRecord deletedRecord = seckillRecordMapper.selectAnyByActivitySkuUser(activityId, activitySku.getSkuId(), userId);
            if (deletedRecord != null && Objects.equals(deletedRecord.getIsDeleted(), 1)) {
                seckillRecordMapper.restoreDeletedRecord(deletedRecord.getId(), userId, RECORD_PENDING);
                record = seckillRecordMapper.selectById(deletedRecord.getId());
            } else {
                record = new LySeckillRecord();
                record.setActivityId(activityId);
                record.setSkuId(activitySku.getSkuId());
                record.setUserId(userId);
                record.setStatus(RECORD_PENDING);
                record.setCreatedBy(userId);
                record.setUpdatedBy(userId);
            }
        } else {
            record.setOrderId(null);
            record.setStatus(RECORD_PENDING);
            record.setUpdatedBy(userId);
        }
        try {
            if (record.getId() == null) {
                seckillRecordMapper.insert(record);
            } else if (!Objects.equals(record.getIsDeleted(), 1)) {
                seckillRecordMapper.updateById(record);
            }
        } catch (DuplicateKeyException ex) {
            compensateRedis(activitySkuId, userId);
            LySeckillRecord duplicate = findRecord(activityId, activitySku.getSkuId(), userId);
            if (duplicate != null) {
                return buildAttempt(duplicate.getId(), duplicate.getStatus(), buildStatusText(duplicate.getStatus()));
            }
            throw new BizException("S0409", "当前商品已经提交过秒杀请求");
        }

        if (isAsyncMode()) {
            dispatchOrderCreation(record.getId());
            return buildAttempt(record.getId(), RECORD_PENDING, "秒杀请求已提交，请稍后查看订单状态");
        }

        processOrderMessage(buildOrderMessage(record.getId()));
        LySeckillRecord latest = seckillRecordMapper.selectById(record.getId());
        if (latest == null) {
            throw new BizException("S0500", "秒杀记录处理失败，请稍后重试");
        }
        return buildAttempt(latest.getId(), latest.getStatus(), buildStatusText(latest.getStatus()));
    }

    @Override
    public SeckillRecordVO getRecord(Long userId, Long recordId) {
        LySeckillRecord record = seckillRecordMapper.selectOne(new LambdaQueryWrapper<LySeckillRecord>()
                .eq(LySeckillRecord::getId, recordId)
                .eq(LySeckillRecord::getUserId, userId)
                .last("LIMIT 1"));
        if (record == null) {
            throw new BizException("S0404", "秒杀记录不存在");
        }
        LyActivitySku activitySku = requireActivitySkuBySkuId(record.getActivityId(), record.getSkuId());
        record = reconcileLinkedOrderState(record, activitySku);
        refreshTimedOutPendingRecord(record);
        if (shouldRecoverPendingRecord(record)) {
            if (isAsyncMode()) {
                dispatchOrderCreation(record.getId());
            } else {
                processOrderMessage(buildOrderMessage(record.getId()));
            }
            record = seckillRecordMapper.selectById(record.getId());
        }
        return toRecordVO(record);
    }

    @Override
    public void preloadActiveActivities() {
        LocalDateTime now = LocalDateTime.now();
        List<LyActivity> activities = activityMapper.selectList(new LambdaQueryWrapper<LyActivity>()
                .eq(LyActivity::getStatus, ACTIVITY_ONLINE)
                .ge(LyActivity::getEndTime, now));
        for (LyActivity activity : activities) {
            List<LyActivitySku> skuList = activitySkuMapper.selectList(new LambdaQueryWrapper<LyActivitySku>()
                    .eq(LyActivitySku::getActivityId, activity.getId())
                    .eq(LyActivitySku::getStatus, SKU_ONLINE));
            for (LyActivitySku sku : skuList) {
                stringRedisTemplate.opsForValue().set(stockKey(sku.getId()), String.valueOf(Math.max(sku.getStockAvailable(), 0)));
                stringRedisTemplate.expire(stockKey(sku.getId()), Duration.ofSeconds(expireSeconds(activity.getEndTime())));
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void processOrderMessage(SeckillOrderMessage message) {
        if (message == null || message.getRecordId() == null) {
            return;
        }
        LySeckillRecord record = seckillRecordMapper.selectById(message.getRecordId());
        if (record == null || !Objects.equals(record.getStatus(), RECORD_PENDING)) {
            return;
        }

        LyActivity activity = requireActivity(record.getActivityId());
        LyActivitySku activitySku = requireActivitySkuBySkuId(record.getActivityId(), record.getSkuId());
        boolean dbLocked = false;
        try {
            validateActivityWindow(activity);
            if (activitySkuMapper.lockForOrder(activitySku.getId(), BUY_QUANTITY) <= 0) {
                failAndCompensate(record, activitySku, false, "库存不足");
                return;
            }
            dbLocked = true;

            ProductSkuVO sku = loadProductSku(record.getSkuId());
            if (sku == null) {
                failAndCompensate(record, activitySku, true, "商品信息不存在");
                return;
            }

            InternalCreateSeckillOrderRequest request = new InternalCreateSeckillOrderRequest();
            request.setUserId(record.getUserId());
            request.setActivityId(record.getActivityId());
            request.setActivitySkuId(activitySku.getId());
            request.setSeckillRecordId(record.getId());
            request.setSpuId(sku.getSpuId());
            request.setSkuId(sku.getId());
            request.setSkuTitle(StringUtils.hasText(sku.getTitle()) ? sku.getTitle() : sku.getSpuName());
            request.setSkuAttrsJson(sku.getAttrsJson());
            request.setPrice(activitySku.getSeckillPrice());
            request.setQuantity(BUY_QUANTITY);

            Result<SeckillOrderCreateVO> result = orderClient.createSeckillOrder(request);
            SeckillOrderCreateVO order = result == null ? null : result.getData();
            if (result == null || !SUCCESS_CODE.equals(result.getCode()) || order == null || order.getId() == null) {
                failAndCompensate(record, activitySku, true, resolveMessage(result, "秒杀订单创建失败"));
                return;
            }

            record.setOrderId(order.getId());
            record.setStatus(RECORD_WAIT_PAY);
            record.setUpdatedBy(record.getUserId());
            seckillRecordMapper.updateById(record);
        } catch (BizException ex) {
            failAndCompensate(record, activitySku, dbLocked, ex.getMessage());
        } catch (Exception ex) {
            log.error("秒杀订单创建失败, recordId={}", record.getId(), ex);
            failAndCompensate(record, activitySku, dbLocked, "秒杀订单创建失败");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void confirmByOrderId(Long orderId) {
        LySeckillRecord record = requireRecordByOrderId(orderId);
        if (Objects.equals(record.getStatus(), RECORD_PAID)) {
            return;
        }
        if (!Objects.equals(record.getStatus(), RECORD_WAIT_PAY)) {
            return;
        }
        LyActivitySku activitySku = requireActivitySkuBySkuId(record.getActivityId(), record.getSkuId());
        activitySkuMapper.confirmPaid(activitySku.getId(), BUY_QUANTITY);
        record.setStatus(RECORD_PAID);
        record.setUpdatedBy(record.getUserId());
        seckillRecordMapper.updateById(record);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void releaseByOrderId(Long orderId) {
        LySeckillRecord record = requireRecordByOrderId(orderId);
        if (!Objects.equals(record.getStatus(), RECORD_WAIT_PAY)) {
            return;
        }
        LyActivitySku activitySku = requireActivitySkuBySkuId(record.getActivityId(), record.getSkuId());
        activitySkuMapper.releaseLocked(activitySku.getId(), BUY_QUANTITY);
        compensateRedis(activitySku.getId(), record.getUserId());
        record.setStatus(RECORD_CANCELLED);
        record.setUpdatedBy(record.getUserId());
        seckillRecordMapper.updateById(record);
    }

    private void dispatchOrderCreation(Long recordId) {
        if (recordId == null) {
            return;
        }
        if (TransactionSynchronizationManager.isActualTransactionActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    submitOrderCreation(recordId);
                }
            });
            return;
        }
        submitOrderCreation(recordId);
    }

    private void submitOrderCreation(Long recordId) {
        SeckillOrderMessage message = buildOrderMessage(recordId);
        if ("rocketmq".equalsIgnoreCase(asyncMode)) {
            RocketMQTemplate rocketMQTemplate = rocketMQTemplateProvider.getIfAvailable();
            if (rocketMQTemplate != null) {
                rocketMQTemplate.convertAndSend(topic, message);
                return;
            }
            log.warn("秒杀异步模式配置为 rocketmq，但未找到 RocketMQTemplate，自动回退为本地异步");
        }
        seckillTaskExecutor.execute(() -> processOrderMessage(message));
    }

    private void failAndCompensate(LySeckillRecord record, LyActivitySku activitySku, boolean dbLocked, String reason) {
        if (dbLocked) {
            activitySkuMapper.releaseLocked(activitySku.getId(), BUY_QUANTITY);
        }
        compensateRedis(activitySku.getId(), record.getUserId());
        record.setStatus(RECORD_FAILED);
        record.setUpdatedBy(record.getUserId());
        seckillRecordMapper.updateById(record);
        log.warn("秒杀下单失败, recordId={}, reason={}", record.getId(), reason);
    }

    private LySeckillRecord reconcileLinkedOrderState(LySeckillRecord record, LyActivitySku activitySku) {
        if (record == null || record.getOrderId() == null) {
            return record;
        }
        LinkedOrderVO linkedOrder = loadLinkedOrder(record.getOrderId());
        if (linkedOrder == null) {
            return record;
        }
        if (Objects.equals(linkedOrder.getStatus(), ORDER_CANCELLED)
                && Objects.equals(record.getStatus(), RECORD_WAIT_PAY)) {
            activitySkuMapper.releaseLocked(activitySku.getId(), BUY_QUANTITY);
            compensateRedis(activitySku.getId(), record.getUserId());
            record.setOrderId(null);
            record.setStatus(RECORD_CANCELLED);
            record.setUpdatedBy(record.getUserId());
            seckillRecordMapper.updateById(record);
            return seckillRecordMapper.selectById(record.getId());
        }
        if (Objects.equals(linkedOrder.getStatus(), ORDER_PAID)
                && !Objects.equals(record.getStatus(), RECORD_PAID)) {
            record.setStatus(RECORD_PAID);
            record.setUpdatedBy(record.getUserId());
            seckillRecordMapper.updateById(record);
            return seckillRecordMapper.selectById(record.getId());
        }
        return record;
    }

    private LinkedOrderVO loadLinkedOrder(Long orderId) {
        try {
            Result<LinkedOrderVO> result = orderClient.getOrderById(orderId);
            if (result == null || !SUCCESS_CODE.equals(result.getCode())) {
                return null;
            }
            return result.getData();
        } catch (Exception ex) {
            log.warn("查询关联订单失败, orderId={}", orderId, ex);
            return null;
        }
    }

    private ProductSkuVO loadProductSku(Long skuId) {
        try {
            Result<ProductSkuVO> result = productClient.getSku(skuId);
            if (result == null || !SUCCESS_CODE.equals(result.getCode())) {
                return null;
            }
            return result.getData();
        } catch (Exception ex) {
            log.warn("查询商品 SKU 失败, skuId={}", skuId, ex);
            return null;
        }
    }

    private LyActivity requireActivity(Long activityId) {
        LyActivity activity = activityMapper.selectById(activityId);
        if (activity == null || !Objects.equals(activity.getStatus(), ACTIVITY_ONLINE)) {
            throw new BizException("S0404", "秒杀活动不存在");
        }
        return activity;
    }

    private LyActivitySku requireActivitySkuByActivitySkuId(Long activityId, Long activitySkuId) {
        LyActivitySku activitySku = activitySkuMapper.selectOne(new LambdaQueryWrapper<LyActivitySku>()
                .eq(LyActivitySku::getId, activitySkuId)
                .eq(LyActivitySku::getActivityId, activityId)
                .eq(LyActivitySku::getStatus, SKU_ONLINE)
                .last("LIMIT 1"));
        if (activitySku == null) {
            throw new BizException("S0404", "秒杀商品不存在");
        }
        return activitySku;
    }

    private LyActivitySku requireActivitySkuBySkuId(Long activityId, Long skuId) {
        LyActivitySku activitySku = activitySkuMapper.selectOne(new LambdaQueryWrapper<LyActivitySku>()
                .eq(LyActivitySku::getActivityId, activityId)
                .eq(LyActivitySku::getSkuId, skuId)
                .eq(LyActivitySku::getStatus, SKU_ONLINE)
                .last("LIMIT 1"));
        if (activitySku == null) {
            throw new BizException("S0404", "秒杀商品不存在");
        }
        return activitySku;
    }

    private LySeckillRecord findRecord(Long activityId, Long skuId, Long userId) {
        return seckillRecordMapper.selectOne(new LambdaQueryWrapper<LySeckillRecord>()
                .eq(LySeckillRecord::getActivityId, activityId)
                .eq(LySeckillRecord::getSkuId, skuId)
                .eq(LySeckillRecord::getUserId, userId)
                .last("LIMIT 1"));
    }

    private LySeckillRecord requireRecordByOrderId(Long orderId) {
        LySeckillRecord record = seckillRecordMapper.selectOne(new LambdaQueryWrapper<LySeckillRecord>()
                .eq(LySeckillRecord::getOrderId, orderId)
                .last("LIMIT 1"));
        if (record == null) {
            throw new BizException("S0404", "秒杀记录不存在");
        }
        return record;
    }

    private void refreshTimedOutPendingRecord(LySeckillRecord record) {
        if (!isPendingTimedOut(record)) {
            return;
        }
        LyActivitySku activitySku = activitySkuMapper.selectOne(new LambdaQueryWrapper<LyActivitySku>()
                .eq(LyActivitySku::getActivityId, record.getActivityId())
                .eq(LyActivitySku::getSkuId, record.getSkuId())
                .last("LIMIT 1"));
        if (activitySku != null) {
            releasePendingReservation(activitySku.getId(), record.getUserId());
        }
        record.setStatus(RECORD_FAILED);
        record.setUpdatedBy(record.getUserId());
        seckillRecordMapper.updateById(record);
    }

    private boolean shouldRecoverPendingRecord(LySeckillRecord record) {
        return record != null
                && record.getId() != null
                && record.getOrderId() == null
                && Objects.equals(record.getStatus(), RECORD_PENDING);
    }

    private boolean isPendingTimedOut(LySeckillRecord record) {
        LocalDateTime baseTime = record == null
                ? null
                : (record.getUpdatedAt() != null ? record.getUpdatedAt() : record.getCreatedAt());
        return record != null
                && Objects.equals(record.getStatus(), RECORD_PENDING)
                && baseTime != null
                && Duration.between(baseTime, LocalDateTime.now()).getSeconds() >= pendingTimeoutSeconds;
    }

    private void releasePendingReservation(Long activitySkuId, Long userId) {
        Boolean existed = stringRedisTemplate.hasKey(userKey(activitySkuId, userId));
        if (Boolean.TRUE.equals(existed)) {
            stringRedisTemplate.delete(userKey(activitySkuId, userId));
            stringRedisTemplate.opsForValue().increment(stockKey(activitySkuId));
        }
    }

    private void clearUserAttemptMark(Long activitySkuId, Long userId) {
        stringRedisTemplate.delete(userKey(activitySkuId, userId));
    }

    private void validateActivityWindow(LyActivity activity) {
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(activity.getStartTime())) {
            throw new BizException("S0409", "秒杀活动尚未开始");
        }
        if (now.isAfter(activity.getEndTime())) {
            throw new BizException("S0410", "秒杀活动已经结束");
        }
    }

    private SeckillRecordVO toRecordVO(LySeckillRecord record) {
        SeckillRecordVO vo = new SeckillRecordVO();
        vo.setRecordId(record.getId() == null ? null : String.valueOf(record.getId()));
        vo.setActivityId(record.getActivityId() == null ? null : String.valueOf(record.getActivityId()));
        vo.setSkuId(record.getSkuId() == null ? null : String.valueOf(record.getSkuId()));
        vo.setOrderId(record.getOrderId() == null ? null : String.valueOf(record.getOrderId()));
        vo.setStatus(record.getStatus());
        vo.setStatusText(buildStatusText(record.getStatus()));
        vo.setCreatedAt(record.getCreatedAt());
        return vo;
    }

    private SeckillSkuVO toSkuVO(LyActivity activity, LyActivitySku activitySku, ProductSkuVO sku) {
        SeckillSkuVO skuVO = new SeckillSkuVO();
        skuVO.setActivitySkuId(activitySku.getId());
        skuVO.setActivityId(activity.getId());
        skuVO.setSpuId(sku.getSpuId());
        skuVO.setSkuId(activitySku.getSkuId());
        skuVO.setSpuName(sku.getSpuName());
        skuVO.setSkuTitle(StringUtils.hasText(sku.getTitle()) ? sku.getTitle() : sku.getSpuName());
        skuVO.setMainImage(sku.getMainImage());
        skuVO.setSeckillPrice(activitySku.getSeckillPrice());
        skuVO.setOriginPrice(sku.getPrice());
        skuVO.setStockAvailable(resolveCurrentStock(activitySku));
        skuVO.setLimitPerUser(activitySku.getLimitPerUser());
        return skuVO;
    }

    private SeckillDetailVO toDetailVO(LyActivity activity, LyActivitySku activitySku, ProductSkuVO sku) {
        SeckillSkuVO skuVO = toSkuVO(activity, activitySku, sku);
        SeckillDetailVO detailVO = new SeckillDetailVO();
        detailVO.setActivityId(skuVO.getActivityId());
        detailVO.setActivitySkuId(skuVO.getActivitySkuId());
        detailVO.setSpuId(skuVO.getSpuId());
        detailVO.setSkuId(skuVO.getSkuId());
        detailVO.setName(StringUtils.hasText(skuVO.getSpuName()) ? skuVO.getSpuName() : activity.getName());
        detailVO.setTitle(StringUtils.hasText(skuVO.getSkuTitle()) ? skuVO.getSkuTitle() : detailVO.getName());
        detailVO.setImage(skuVO.getMainImage());
        detailVO.setSeckillPrice(skuVO.getSeckillPrice());
        detailVO.setOriginPrice(skuVO.getOriginPrice());
        detailVO.setStockAvailable(skuVO.getStockAvailable());
        detailVO.setLimitPerUser(skuVO.getLimitPerUser());
        detailVO.setStartTime(activity.getStartTime());
        detailVO.setEndTime(activity.getEndTime());
        detailVO.setActivityStatus(activity.getStatus());
        return detailVO;
    }

    private SeckillAttemptVO buildAttempt(Long recordId, Integer status, String message) {
        SeckillAttemptVO vo = new SeckillAttemptVO();
        vo.setRecordId(recordId == null ? null : String.valueOf(recordId));
        vo.setStatus(status);
        vo.setMessage(message);
        return vo;
    }

    private Integer resolveCurrentStock(LyActivitySku activitySku) {
        String stock = stringRedisTemplate.opsForValue().get(stockKey(activitySku.getId()));
        if (!StringUtils.hasText(stock)) {
            return Math.max(activitySku.getStockAvailable(), 0);
        }
        try {
            return Integer.parseInt(stock);
        } catch (NumberFormatException ex) {
            return Math.max(activitySku.getStockAvailable(), 0);
        }
    }

    private String buildStatusText(Integer status) {
        return switch (status == null ? -1 : status) {
            case RECORD_PENDING -> "排队处理中";
            case RECORD_WAIT_PAY -> "待支付";
            case RECORD_PAID -> "已支付";
            case RECORD_CANCELLED -> "已取消";
            case RECORD_FAILED -> "已失败";
            default -> "未知状态";
        };
    }

    private boolean canRetry(Integer status) {
        return Objects.equals(status, RECORD_FAILED) || Objects.equals(status, RECORD_CANCELLED);
    }

    private long expireSeconds(LocalDateTime endTime) {
        long seconds = Duration.between(LocalDateTime.now(), endTime).getSeconds();
        return Math.max(seconds, 60L);
    }

    private void compensateRedis(Long activitySkuId, Long userId) {
        stringRedisTemplate.opsForValue().increment(stockKey(activitySkuId));
        stringRedisTemplate.delete(userKey(activitySkuId, userId));
    }

    private Long executeAttemptScript(Long activitySkuId, Long userId, LocalDateTime endTime) {
        return stringRedisTemplate.execute(
                attemptScript,
                List.of(stockKey(activitySkuId), userKey(activitySkuId, userId)),
                String.valueOf(expireSeconds(endTime))
        );
    }

    private String stockKey(Long activitySkuId) {
        return "lingyi:seckill:stock:" + activitySkuId;
    }

    private String userKey(Long activitySkuId, Long userId) {
        return "lingyi:seckill:user:" + activitySkuId + ':' + userId;
    }

    private SeckillOrderMessage buildOrderMessage(Long recordId) {
        SeckillOrderMessage message = new SeckillOrderMessage();
        message.setRecordId(recordId);
        return message;
    }

    private boolean isAsyncMode() {
        return "rocketmq".equalsIgnoreCase(asyncMode);
    }

    private String resolveMessage(Result<?> result, String fallback) {
        if (result == null) {
            return fallback;
        }
        String message = result.getMessage();
        return message == null || message.isBlank() ? fallback : message;
    }

    private DefaultRedisScript<Long> buildAttemptScript() {
        DefaultRedisScript<Long> script = new DefaultRedisScript<>();
        script.setResultType(Long.class);
        script.setScriptText("""
                local stockKey = KEYS[1]
                local userKey = KEYS[2]
                local expireSeconds = tonumber(ARGV[1])
                local currentStock = tonumber(redis.call('GET', stockKey) or '-1')
                if currentStock <= 0 then
                    return 1
                end
                if redis.call('EXISTS', userKey) == 1 then
                    return 2
                end
                redis.call('DECR', stockKey)
                redis.call('SET', userKey, '1', 'EX', expireSeconds)
                return 0
                """);
        return script;
    }
}
