package com.lingyi.service.seckill.controller;

import com.lingyi.common.web.domain.Result;
import com.lingyi.service.seckill.service.SeckillService;
import com.lingyi.service.seckill.vo.SeckillActivityVO;
import com.lingyi.service.seckill.vo.SeckillAttemptVO;
import com.lingyi.service.seckill.vo.SeckillRecordVO;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/seckill")
@RequiredArgsConstructor
public class SeckillController {

    private final SeckillService seckillService;

    @GetMapping("/ping")
    public Result<Map<String, Object>> ping() {
        return Result.success(Map.of("service", "lingyi-seckill", "status", "UP"));
    }

    @GetMapping("/activities/current")
    public Result<List<SeckillActivityVO>> currentActivities() {
        return Result.success(seckillService.listCurrentActivities());
    }

    @PostMapping("/activities/{activityId}/skus/{activitySkuId}/attempt")
    public Result<SeckillAttemptVO> attempt(@RequestHeader("X-User-Id") Long userId,
                                            @PathVariable Long activityId,
                                            @PathVariable Long activitySkuId) {
        return Result.success(seckillService.attempt(userId, activityId, activitySkuId));
    }

    @GetMapping("/records/{recordId}")
    public Result<SeckillRecordVO> record(@RequestHeader("X-User-Id") Long userId,
                                          @PathVariable Long recordId) {
        return Result.success(seckillService.getRecord(userId, recordId));
    }
}
