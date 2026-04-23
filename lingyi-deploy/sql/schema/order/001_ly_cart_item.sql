CREATE TABLE IF NOT EXISTS `ly_cart_item` (
  `id` BIGINT NOT NULL COMMENT '购物车项ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `sku_id` BIGINT NOT NULL COMMENT 'SKU ID',
  `quantity` INT NOT NULL COMMENT '商品数量',
  `checked` TINYINT NOT NULL DEFAULT 1 COMMENT '是否勾选:1是,0否',
  `price_snapshot` DECIMAL(10,2) NOT NULL COMMENT '价格快照',
  `created_by` BIGINT DEFAULT NULL COMMENT '创建人ID',
  `updated_by` BIGINT DEFAULT NULL COMMENT '更新人ID',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除:0否,1是',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_ly_cart_user_sku` (`user_id`,`sku_id`),
  KEY `idx_ly_cart_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='购物车明细表';
