CREATE TABLE IF NOT EXISTS `ly_prompt_version` (
  `id` BIGINT NOT NULL COMMENT '版本ID',
  `prompt_id` BIGINT NOT NULL COMMENT '提示词ID',
  `version_no` INT NOT NULL COMMENT '版本号',
  `content` MEDIUMTEXT NOT NULL COMMENT '提示词内容',
  `variables_json` TEXT DEFAULT NULL COMMENT '变量定义JSON',
  `model_config_json` TEXT DEFAULT NULL COMMENT '模型参数JSON',
  `status` TINYINT NOT NULL DEFAULT 0 COMMENT '状态:0草稿,1已发布,2已归档',
  `published_at` DATETIME DEFAULT NULL COMMENT '发布时间',
  `created_by` BIGINT DEFAULT NULL COMMENT '创建人ID',
  `updated_by` BIGINT DEFAULT NULL COMMENT '更新人ID',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除:0否,1是',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_ly_prompt_version_no` (`prompt_id`,`version_no`),
  KEY `idx_ly_prompt_version_status` (`prompt_id`,`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='提示词版本表';
