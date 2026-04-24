CREATE TABLE IF NOT EXISTS `ly_chat_message_ref` (
  `id` BIGINT NOT NULL COMMENT '引用ID',
  `message_id` BIGINT NOT NULL COMMENT '消息ID',
  `ref_type` VARCHAR(32) NOT NULL COMMENT '引用类型:prompt/product/rag/attachment',
  `ref_code` VARCHAR(64) DEFAULT NULL COMMENT '引用编码',
  `ref_id` BIGINT DEFAULT NULL COMMENT '引用ID',
  `ref_meta_json` TEXT DEFAULT NULL COMMENT '引用元数据JSON',
  `created_by` BIGINT DEFAULT NULL COMMENT '创建人ID',
  `updated_by` BIGINT DEFAULT NULL COMMENT '更新人ID',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除:0否,1是',
  PRIMARY KEY (`id`),
  KEY `idx_ly_chat_message_ref_message` (`message_id`,`ref_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI消息引用表';
