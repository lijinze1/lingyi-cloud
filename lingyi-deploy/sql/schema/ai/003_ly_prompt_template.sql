CREATE TABLE IF NOT EXISTS `ly_prompt_template` (
  `id` BIGINT NOT NULL COMMENT '模板ID',
  `biz_scene` VARCHAR(64) NOT NULL COMMENT '业务场景',
  `name` VARCHAR(128) NOT NULL COMMENT '模板名称',
  `content` MEDIUMTEXT NOT NULL COMMENT '模板内容',
  `version_no` INT NOT NULL DEFAULT 1 COMMENT '版本号',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态:1启用,0禁用',
  `created_by` BIGINT DEFAULT NULL COMMENT '创建人ID',
  `updated_by` BIGINT DEFAULT NULL COMMENT '更新人ID',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除:0否,1是',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_ly_prompt_scene_name_ver` (`biz_scene`,`name`,`version_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='提示词模板表';
