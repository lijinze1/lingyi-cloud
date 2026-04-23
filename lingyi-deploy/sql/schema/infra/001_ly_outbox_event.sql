CREATE TABLE IF NOT EXISTS `ly_outbox_event` (
  `id` BIGINT NOT NULL COMMENT '事件ID',
  `event_key` VARCHAR(128) NOT NULL COMMENT '事件幂等键',
  `topic` VARCHAR(128) NOT NULL COMMENT '消息主题',
  `tag` VARCHAR(64) DEFAULT NULL COMMENT '消息标签',
  `payload` JSON NOT NULL COMMENT '事件负载JSON',
  `status` TINYINT NOT NULL DEFAULT 0 COMMENT '状态:0待发送,1已发送,2失败',
  `retry_count` INT NOT NULL DEFAULT 0 COMMENT '重试次数',
  `next_retry_at` DATETIME DEFAULT NULL COMMENT '下次重试时间',
  `created_by` BIGINT DEFAULT NULL COMMENT '创建人ID',
  `updated_by` BIGINT DEFAULT NULL COMMENT '更新人ID',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除:0否,1是',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_ly_outbox_event_key` (`event_key`),
  KEY `idx_ly_outbox_status_retry` (`status`,`next_retry_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='事务外盒事件表';
