CREATE TABLE IF NOT EXISTS `ly_spu` (
  `id` BIGINT NOT NULL COMMENT 'SPU ID',
  `category_id` BIGINT NOT NULL COMMENT '分类ID',
  `name` VARCHAR(128) NOT NULL COMMENT '商品名称',
  `sub_title` VARCHAR(255) DEFAULT NULL COMMENT '副标题',
  `main_image` VARCHAR(255) DEFAULT NULL COMMENT '主图地址',
  `detail` MEDIUMTEXT COMMENT '商品详情',
  `status` TINYINT NOT NULL DEFAULT 0 COMMENT '上架状态:0下架,1上架',
  `created_by` BIGINT DEFAULT NULL COMMENT '创建人ID',
  `updated_by` BIGINT DEFAULT NULL COMMENT '更新人ID',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除:0否,1是',
  PRIMARY KEY (`id`),
  KEY `idx_ly_spu_category` (`category_id`),
  KEY `idx_ly_spu_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='SPU商品主表';
