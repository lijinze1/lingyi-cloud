CREATE TABLE IF NOT EXISTS `ly_chat_message` (
  `id` BIGINT NOT NULL COMMENT '消息ID',
  `session_id` BIGINT NOT NULL COMMENT '会话ID',
  `role` VARCHAR(16) NOT NULL COMMENT '角色:user/assistant/system',
  `content` MEDIUMTEXT NOT NULL COMMENT '消息内容',
  `tokens_in` INT DEFAULT NULL COMMENT '输入Token数',
  `tokens_out` INT DEFAULT NULL COMMENT '输出Token数',
  `model` VARCHAR(64) DEFAULT NULL COMMENT '模型名称',
  `created_by` BIGINT DEFAULT NULL COMMENT '创建人ID',
  `updated_by` BIGINT DEFAULT NULL COMMENT '更新人ID',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除:0否,1是',
  PRIMARY KEY (`id`),
  KEY `idx_ly_chat_message_session_ct` (`session_id`,`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI会话消息表';
