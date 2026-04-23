CREATE TABLE IF NOT EXISTS `ly_sku` (
  `id` BIGINT NOT NULL COMMENT 'SKU ID',
  `spu_id` BIGINT NOT NULL COMMENT 'SPU ID',
  `sku_code` VARCHAR(64) NOT NULL COMMENT 'SKU编码',
  `title` VARCHAR(128) NOT NULL COMMENT 'SKU标题',
  `attrs_json` JSON DEFAULT NULL COMMENT '销售属性JSON',
  `price` DECIMAL(10,2) NOT NULL COMMENT '销售价',
  `origin_price` DECIMAL(10,2) DEFAULT NULL COMMENT '原价',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态:1启用,0禁用',
  `created_by` BIGINT DEFAULT NULL COMMENT '创建人ID',
  `updated_by` BIGINT DEFAULT NULL COMMENT '更新人ID',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除:0否,1是',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_ly_sku_code` (`sku_code`),
  KEY `idx_ly_sku_spu` (`spu_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='SKU明细表';
