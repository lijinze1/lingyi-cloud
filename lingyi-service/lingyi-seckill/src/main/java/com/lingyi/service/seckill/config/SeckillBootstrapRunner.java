package com.lingyi.service.seckill.config;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lingyi.service.seckill.entity.LyActivity;
import com.lingyi.service.seckill.entity.LyActivitySku;
import com.lingyi.service.seckill.mapper.ActivityMapper;
import com.lingyi.service.seckill.mapper.ActivitySkuMapper;
import com.lingyi.service.seckill.service.SeckillService;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class SeckillBootstrapRunner implements ApplicationRunner {

    private final ActivityMapper activityMapper;
    private final ActivitySkuMapper activitySkuMapper;
    private final SeckillService seckillService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void run(ApplicationArguments args) {
        Long count = activityMapper.selectCount(new LambdaQueryWrapper<>());
        if (count == null || count == 0) {
            seedActivity();
        }
        seckillService.preloadActiveActivities();
    }

    private void seedActivity() {
        LocalDateTime now = LocalDateTime.now();
        LyActivity activity = new LyActivity();
        activity.setId(6001L);
        activity.setName("家庭常备药限时秒杀");
        activity.setStartTime(now.minusHours(1));
        activity.setEndTime(now.plusDays(15));
        activity.setStatus(1);
        activityMapper.insert(activity);

        seedActivitySku(7001L, activity.getId(), 4001L, new BigDecimal("19.90"), 30, 1);
        seedActivitySku(7002L, activity.getId(), 4002L, new BigDecimal("24.90"), 24, 1);
        seedActivitySku(7003L, activity.getId(), 4003L, new BigDecimal("16.80"), 36, 1);
        seedActivitySku(7004L, activity.getId(), 4005L, new BigDecimal("12.90"), 40, 1);
    }

    private void seedActivitySku(Long id,
                                 Long activityId,
                                 Long skuId,
                                 BigDecimal seckillPrice,
                                 Integer stock,
                                 Integer limitPerUser) {
        LyActivitySku item = new LyActivitySku();
        item.setId(id);
        item.setActivityId(activityId);
        item.setSkuId(skuId);
        item.setSeckillPrice(seckillPrice);
        item.setStockTotal(stock);
        item.setStockAvailable(stock);
        item.setStockLocked(0);
        item.setLimitPerUser(limitPerUser);
        item.setVersion(0);
        item.setStatus(1);
        activitySkuMapper.insert(item);
    }
}