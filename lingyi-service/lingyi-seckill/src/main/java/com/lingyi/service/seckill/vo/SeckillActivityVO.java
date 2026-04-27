package com.lingyi.service.seckill.vo;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;

@Data
public class SeckillActivityVO {
    private Long id;
    private String name;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer status;
    private List<SeckillSkuVO> skus = new ArrayList<>();
}