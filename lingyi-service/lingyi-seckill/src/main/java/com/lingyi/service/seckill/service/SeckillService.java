package com.lingyi.service.seckill.service;

import com.lingyi.service.seckill.mq.SeckillOrderMessage;
import com.lingyi.service.seckill.vo.SeckillActivityVO;
import com.lingyi.service.seckill.vo.SeckillAttemptVO;
import com.lingyi.service.seckill.vo.SeckillRecordVO;
import java.util.List;

public interface SeckillService {
    List<SeckillActivityVO> listCurrentActivities();
    SeckillAttemptVO attempt(Long userId, Long activityId, Long activitySkuId);
    SeckillRecordVO getRecord(Long userId, Long recordId);
    void preloadActiveActivities();
    void processOrderMessage(SeckillOrderMessage message);
    void confirmByOrderId(Long orderId);
    void releaseByOrderId(Long orderId);
}