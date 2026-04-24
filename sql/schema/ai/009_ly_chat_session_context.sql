CREATE TABLE IF NOT EXISTS `ly_chat_session_context` (
  `id` BIGINT NOT NULL COMMENT '上下文ID',
  `session_id` BIGINT NOT NULL COMMENT '会话ID',
  `summary` MEDIUMTEXT DEFAULT NULL COMMENT '历史摘要',
  `summary_version` INT NOT NULL DEFAULT 0 COMMENT '摘要版本号',
  `last_context_message_id` BIGINT DEFAULT NULL COMMENT '已摘要的最后消息ID',
  `last_message_id` BIGINT DEFAULT NULL COMMENT '最近消息ID',
  `created_by` BIGINT DEFAULT NULL COMMENT '创建人ID',
  `updated_by` BIGINT DEFAULT NULL COMMENT '更新人ID',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除:0否,1是',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_ly_chat_session_context_session` (`session_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI会话上下文表';
