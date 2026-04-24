package com.lingyi.service.cart.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("ly_cart_item")
public class LyCartItem {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private Long userId;
    private Long skuId;
    private Integer quantity;
    private Integer checked;
    private BigDecimal priceSnapshot;
    private Long createdBy;
    private Long updatedBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    @TableLogic(value = "0", delval = "1")
    private Integer isDeleted;
}
