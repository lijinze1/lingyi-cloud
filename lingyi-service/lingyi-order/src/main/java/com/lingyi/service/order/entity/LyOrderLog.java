package com.lingyi.service.order.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("ly_order_log")
public class LyOrderLog {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private Long orderId;
    private String orderNo;
    private String action;
    private Long operatorId;
    private String operatorName;
    private String content;
    private Long createdBy;
    private Long updatedBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    @TableLogic(value = "0", delval = "1")
    private Integer isDeleted;
}
