package com.lingyi.service.product.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("ly_sku")
public class LySku {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private Long spuId;
    private String skuCode;
    private String title;
    private String attrsJson;
    private BigDecimal price;
    private BigDecimal originPrice;
    private Integer status;
    private Long createdBy;
    private Long updatedBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    @TableLogic(value = "0", delval = "1")
    private Integer isDeleted;
}
