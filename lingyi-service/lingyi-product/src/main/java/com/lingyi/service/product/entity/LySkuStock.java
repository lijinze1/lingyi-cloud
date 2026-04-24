package com.lingyi.service.product.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("ly_sku_stock")
public class LySkuStock {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private Long skuId;
    private Integer stockTotal;
    private Integer stockAvailable;
    private Integer stockLocked;
    private Integer version;
    private Long createdBy;
    private Long updatedBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    @TableLogic(value = "0", delval = "1")
    private Integer isDeleted;
}
