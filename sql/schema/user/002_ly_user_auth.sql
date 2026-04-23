CREATE TABLE IF NOT EXISTS `ly_user_auth` (
  `id` BIGINT NOT NULL COMMENT '认证记录ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `auth_type` VARCHAR(32) NOT NULL COMMENT '认证类型:PASSWORD/PHONE/EMAIL/OAUTH',
  `auth_key` VARCHAR(128) NOT NULL COMMENT '认证标识',
  `auth_secret` VARCHAR(255) DEFAULT NULL COMMENT '认证密文',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态:1启用,0禁用',
  `created_by` BIGINT DEFAULT NULL COMMENT '创建人ID',
  `updated_by` BIGINT DEFAULT NULL COMMENT '更新人ID',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除:0否,1是',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_ly_user_auth_type_key` (`auth_type`,`auth_key`),
  KEY `idx_ly_user_auth_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户认证信息表';
