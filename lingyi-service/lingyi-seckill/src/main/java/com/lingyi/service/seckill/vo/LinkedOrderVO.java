package com.lingyi.service.seckill.vo;

import lombok.Data;

@Data
public class LinkedOrderVO {
    private Long id;
    private String orderNo;
    private Integer status;
    private Integer payStatus;
}
