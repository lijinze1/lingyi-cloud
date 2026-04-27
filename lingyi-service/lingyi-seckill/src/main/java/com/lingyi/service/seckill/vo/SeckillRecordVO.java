package com.lingyi.service.seckill.vo;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class SeckillRecordVO {
    private String recordId;
    private String activityId;
    private String skuId;
    private String orderId;
    private Integer status;
    private String statusText;
    private LocalDateTime createdAt;
}
