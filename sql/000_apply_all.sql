SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

source schema/user/001_ly_user.sql;
source schema/user/002_ly_user_auth.sql;
source schema/user/003_ly_role.sql;
source schema/user/004_ly_permission.sql;
source schema/user/005_ly_user_role.sql;
source schema/user/006_ly_role_permission.sql;

source schema/product/001_ly_category.sql;
source schema/product/002_ly_spu.sql;
source schema/product/003_ly_sku.sql;
source schema/product/004_ly_sku_stock.sql;

source schema/order/001_ly_cart_item.sql;
source schema/order/002_ly_order.sql;
source schema/order/003_ly_order_item.sql;
source schema/order/004_ly_order_log.sql;

source schema/seckill/001_ly_activity.sql;
source schema/seckill/002_ly_activity_sku.sql;
source schema/seckill/003_ly_seckill_record.sql;

source schema/review/001_ly_review.sql;
source schema/review/002_ly_review_media.sql;

source schema/ai/001_ly_chat_session.sql;
source schema/ai/002_ly_chat_message.sql;
source schema/ai/003_ly_prompt_template.sql;
source schema/ai/004_ly_kb.sql;
source schema/ai/005_ly_kb_file.sql;
source schema/ai/006_ly_kb_chunk.sql;

source schema/infra/001_ly_outbox_event.sql;
source schema/infra/002_ly_idempotent_record.sql;
source schema/infra/003_undo_log.sql;

source seed/001_init_admin_rbac.sql;

SET FOREIGN_KEY_CHECKS = 1;
