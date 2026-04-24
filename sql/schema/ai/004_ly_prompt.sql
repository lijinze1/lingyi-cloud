CREATE TABLE IF NOT EXISTS `ly_prompt` (
  `id` BIGINT NOT NULL COMMENT '提示词ID',
  `category_id` BIGINT NOT NULL COMMENT '分类ID',
  `prompt_code` VARCHAR(64) NOT NULL COMMENT '提示词编码',
  `name` VARCHAR(128) NOT NULL COMMENT '提示词名称',
  `biz_scene` VARCHAR(64) NOT NULL COMMENT '业务场景',
  `description` VARCHAR(255) DEFAULT NULL COMMENT '描述',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态:1启用,0禁用',
  `published_version_id` BIGINT DEFAULT NULL COMMENT '当前已发布版本ID',
  `created_by` BIGINT DEFAULT NULL COMMENT '创建人ID',
  `updated_by` BIGINT DEFAULT NULL COMMENT '更新人ID',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除:0否,1是',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_ly_prompt_code` (`prompt_code`),
  KEY `idx_ly_prompt_category` (`category_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='提示词主表';
