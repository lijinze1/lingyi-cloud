CREATE TABLE IF NOT EXISTS `undo_log` (
  `branch_id` BIGINT NOT NULL COMMENT '分支事务ID',
  `xid` VARCHAR(128) NOT NULL COMMENT '全局事务ID',
  `context` VARCHAR(128) NOT NULL COMMENT '上下文信息',
  `rollback_info` LONGBLOB NOT NULL COMMENT '回滚信息',
  `log_status` INT NOT NULL COMMENT '日志状态:0正常,1防悬挂',
  `log_created` DATETIME NOT NULL COMMENT '创建时间',
  `log_modified` DATETIME NOT NULL COMMENT '修改时间',
  `ext` VARCHAR(100) DEFAULT NULL COMMENT '扩展字段',
  PRIMARY KEY (`branch_id`,`xid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Seata AT事务回滚日志表';
