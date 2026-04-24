CREATE TABLE IF NOT EXISTS `ly_prompt_category` (
  `id` BIGINT NOT NULL COMMENT '分类ID',
  `parent_id` BIGINT NOT NULL DEFAULT 0 COMMENT '父分类ID',
  `category_code` VARCHAR(64) NOT NULL COMMENT '分类编码',
  `category_name` VARCHAR(128) NOT NULL COMMENT '分类名称',
  `sort_no` INT NOT NULL DEFAULT 0 COMMENT '排序号',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态:1启用,0禁用',
  `created_by` BIGINT DEFAULT NULL COMMENT '创建人ID',
  `updated_by` BIGINT DEFAULT NULL COMMENT '更新人ID',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除:0否,1是',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_ly_prompt_category_code` (`category_code`),
  KEY `idx_ly_prompt_category_parent` (`parent_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='提示词分类表';
