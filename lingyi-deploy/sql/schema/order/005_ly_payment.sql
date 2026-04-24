CREATE TABLE IF NOT EXISTS `ly_payment` (
  `id` BIGINT NOT NULL COMMENT '支付ID',
  `payment_no` VARCHAR(64) NOT NULL COMMENT '支付单号',
  `order_no` VARCHAR(64) NOT NULL COMMENT '订单号',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `amount` DECIMAL(12,2) NOT NULL COMMENT '支付金额',
  `pay_channel` TINYINT NOT NULL DEFAULT 1 COMMENT '支付渠道:1模拟支付',
  `pay_status` TINYINT NOT NULL DEFAULT 0 COMMENT '支付状态:0待支付,1已支付,2已关闭',
  `paid_at` DATETIME DEFAULT NULL COMMENT '支付时间',
  `created_by` BIGINT DEFAULT NULL COMMENT '创建人ID',
  `updated_by` BIGINT DEFAULT NULL COMMENT '更新人ID',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除:0否,1是',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_ly_payment_no` (`payment_no`),
  UNIQUE KEY `uk_ly_payment_order_no` (`order_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='支付单表';
