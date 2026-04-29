DELETE FROM `ly_prompt_category`
WHERE `category_code` = 'rag-system';

INSERT INTO `ly_prompt_category`
(`id`, `parent_id`, `category_code`, `category_name`, `sort_no`, `status`, `created_at`, `updated_at`, `is_deleted`)
VALUES
(910000000000000001, 0, 'customer-service', '商品客服', 10, 1, NOW(), NOW(), 0),
(910000000000000002, 0, 'medical-consultation', '医疗问诊', 20, 1, NOW(), NOW(), 0)
ON DUPLICATE KEY UPDATE `updated_at` = VALUES(`updated_at`);

INSERT INTO `ly_prompt`
(`id`, `category_id`, `prompt_code`, `name`, `biz_scene`, `description`, `status`, `published_version_id`, `created_at`, `updated_at`, `is_deleted`)
VALUES
(910000000000000101, 910000000000000001, 'chatbot-default-v1', '商品客服默认提示词', 'product-chat', 'C端商品客服默认系统提示词', 1, 910000000000000201, NOW(), NOW(), 0),
(910000000000000102, 910000000000000002, 'diagnosis-default-v1', '医疗问诊默认提示词', 'medical-diagnosis', 'C端医疗问诊默认系统提示词', 1, 910000000000000202, NOW(), NOW(), 0)
ON DUPLICATE KEY UPDATE
`category_id` = VALUES(`category_id`),
`name` = VALUES(`name`),
`biz_scene` = VALUES(`biz_scene`),
`description` = VALUES(`description`),
`status` = VALUES(`status`),
`published_version_id` = VALUES(`published_version_id`),
`updated_at` = VALUES(`updated_at`);

INSERT INTO `ly_prompt_version`
(`id`, `prompt_id`, `version_no`, `content`, `variables_json`, `model_config_json`, `status`, `published_at`, `created_at`, `updated_at`, `is_deleted`)
VALUES
(
  910000000000000201,
  910000000000000101,
  1,
  '你是灵医商品 AI 客服助手。请严格依据已有上下文、商品资料和用户问题回答。未知信息要明确说明，不要编造商品信息。若上下文中存在商品信息摘要，请优先基于摘要回答；若问题超出商品资料范围，提示用户稍后咨询人工客服。',
  '[{"name":"productContext","required":false},{"name":"conversationSummary","required":false}]',
  '{"model":"qwen-plus","temperature":0.2,"topP":0.8}',
  1,
  NOW(),
  NOW(),
  NOW(),
  0
),
(
  910000000000000202,
  910000000000000102,
  1,
  '你是灵医医疗问诊 AI 助手。请根据用户文字描述和上传图片做风险识别、可能原因分析、护理建议与就医建议。不要给出绝对诊断，不要虚构检查结果；当症状存在明显风险时，要明确提示尽快线下就医。',
  '[{"name":"conversationSummary","required":false}]',
  '{"model":"qwen-vl-max","temperature":0.2,"topP":0.8}',
  1,
  NOW(),
  NOW(),
  NOW(),
  0
)
ON DUPLICATE KEY UPDATE
`content` = VALUES(`content`),
`variables_json` = VALUES(`variables_json`),
`model_config_json` = VALUES(`model_config_json`),
`status` = VALUES(`status`),
`published_at` = VALUES(`published_at`),
`updated_at` = VALUES(`updated_at`);
