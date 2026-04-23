-- 管理员与基础RBAC初始化数据
INSERT INTO `ly_user` (`id`,`username`,`nickname`,`status`,`created_at`,`updated_at`,`is_deleted`) VALUES
(1000000000001,'admin','系统管理员',1,NOW(),NOW(),0)
ON DUPLICATE KEY UPDATE `updated_at`=NOW();

INSERT INTO `ly_user_auth` (`id`,`user_id`,`auth_type`,`auth_key`,`auth_secret`,`status`,`created_at`,`updated_at`,`is_deleted`) VALUES
(1000000000101,1000000000001,'PASSWORD','admin','$2a$10$7EqJtq98hPqEX7fNZaFWoO5fJ6fVwBEm90LkAMPxQqvOB0fOkH1aS',1,NOW(),NOW(),0)
ON DUPLICATE KEY UPDATE `updated_at`=NOW();

INSERT INTO `ly_role` (`id`,`role_code`,`role_name`,`status`,`created_at`,`updated_at`,`is_deleted`) VALUES
(1000000000201,'ROLE_ADMIN','管理员',1,NOW(),NOW(),0),
(1000000000202,'ROLE_USER','普通用户',1,NOW(),NOW(),0)
ON DUPLICATE KEY UPDATE `updated_at`=NOW();

INSERT INTO `ly_permission` (`id`,`perm_code`,`perm_name`,`perm_type`,`status`,`created_at`,`updated_at`,`is_deleted`) VALUES
(1000000000301,'user:manage','用户管理','API',1,NOW(),NOW(),0),
(1000000000302,'product:manage','商品管理','API',1,NOW(),NOW(),0),
(1000000000303,'prompt:manage','提示词管理','API',1,NOW(),NOW(),0),
(1000000000304,'kb:manage','知识库管理','API',1,NOW(),NOW(),0),
(1000000000305,'order:read','订单查看','API',1,NOW(),NOW(),0),
(1000000000306,'seckill:manage','秒杀管理','API',1,NOW(),NOW(),0)
ON DUPLICATE KEY UPDATE `updated_at`=NOW();

INSERT INTO `ly_user_role` (`id`,`user_id`,`role_id`,`created_at`,`updated_at`,`is_deleted`) VALUES
(1000000000401,1000000000001,1000000000201,NOW(),NOW(),0)
ON DUPLICATE KEY UPDATE `updated_at`=NOW();

INSERT INTO `ly_role_permission` (`id`,`role_id`,`permission_id`,`created_at`,`updated_at`,`is_deleted`) VALUES
(1000000000501,1000000000201,1000000000301,NOW(),NOW(),0),
(1000000000502,1000000000201,1000000000302,NOW(),NOW(),0),
(1000000000503,1000000000201,1000000000303,NOW(),NOW(),0),
(1000000000504,1000000000201,1000000000304,NOW(),NOW(),0),
(1000000000505,1000000000201,1000000000305,NOW(),NOW(),0),
(1000000000506,1000000000201,1000000000306,NOW(),NOW(),0)
ON DUPLICATE KEY UPDATE `updated_at`=NOW();
