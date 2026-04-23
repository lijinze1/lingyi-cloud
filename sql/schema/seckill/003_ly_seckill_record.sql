CREATE TABLE IF NOT EXISTS `ly_seckill_record` (
  `id` BIGINT NOT NULL COMMENT '秒杀记录ID',
  `activity_id` BIGINT NOT NULL COMMENT '活动ID',
  `sku_id` BIGINT NOT NULL COMMENT 'SKU ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `order_id` BIGINT DEFAULT NULL COMMENT '关联订单ID',
  `status` TINYINT NOT NULL DEFAULT 0 COMMENT '状态:0占位,1成功,2失败',
  `created_by` BIGINT DEFAULT NULL COMMENT '创建人ID',
  `updated_by` BIGINT DEFAULT NULL COMMENT '更新人ID',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除:0否,1是',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_ly_seckill_once` (`activity_id`,`sku_id`,`user_id`),
  KEY `idx_ly_seckill_user_ct` (`user_id`,`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='秒杀下单记录表';
