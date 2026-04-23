CREATE TABLE IF NOT EXISTS `ly_kb_chunk` (
  `id` BIGINT NOT NULL COMMENT '切片ID',
  `kb_id` BIGINT NOT NULL COMMENT '知识库ID',
  `file_id` BIGINT NOT NULL COMMENT '文件ID',
  `chunk_index` INT NOT NULL COMMENT '切片序号',
  `content` MEDIUMTEXT NOT NULL COMMENT '切片内容',
  `vector_id` VARCHAR(128) DEFAULT NULL COMMENT '向量ID(Qdrant Point ID)',
  `token_count` INT DEFAULT NULL COMMENT 'Token数量',
  `created_by` BIGINT DEFAULT NULL COMMENT '创建人ID',
  `updated_by` BIGINT DEFAULT NULL COMMENT '更新人ID',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除:0否,1是',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_ly_kb_chunk_file_idx` (`file_id`,`chunk_index`),
  KEY `idx_ly_kb_chunk_kb_file` (`kb_id`,`file_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='知识库切片元数据表';
