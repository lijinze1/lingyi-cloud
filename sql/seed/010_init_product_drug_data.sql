INSERT INTO `ly_category` (`id`,`parent_id`,`name`,`sort_no`,`status`) VALUES
(2001,0,'感冒退热',10,1),
(2002,0,'止咳化痰',20,1),
(2003,0,'肠胃用药',30,1),
(2004,0,'皮肤护理',40,1),
(2005,0,'维矿补益',50,1)
ON DUPLICATE KEY UPDATE `name`=VALUES(`name`),`sort_no`=VALUES(`sort_no`),`status`=VALUES(`status`);

INSERT INTO `ly_spu` (`id`,`category_id`,`name`,`sub_title`,`main_image`,`detail`,`status`) VALUES
(3001,2001,'复方感冒灵颗粒','常见感冒家庭常备','https://minio.local/lingyi/drug-cold-01.png','用于感冒引起的发热、头痛、鼻塞。',1),
(3002,2001,'布洛芬缓释胶囊','退热止痛常备','https://minio.local/lingyi/drug-fever-02.png','适用于普通感冒或流感引起的发热。',1),
(3003,2002,'川贝枇杷糖浆','干咳痰多适用','https://minio.local/lingyi/drug-cough-03.png','用于咳嗽痰多、咽喉不适。',1),
(3004,2002,'氨溴索口服溶液','化痰护理常备','https://minio.local/lingyi/drug-phlegm-04.png','用于痰液黏稠不易咳出。',1),
(3005,2003,'蒙脱石散','肠胃不适应急','https://minio.local/lingyi/drug-stomach-05.png','用于成人及儿童急慢性腹泻。',1),
(3006,2003,'健胃消食片','饭后积食常备','https://minio.local/lingyi/drug-digest-06.png','用于消化不良、食欲不振。',1),
(3007,2004,'炉甘石洗剂','皮肤瘙痒护理','https://minio.local/lingyi/drug-skin-07.png','用于急性瘙痒性皮肤病。',1),
(3008,2004,'莫匹罗星软膏','创面护理常备','https://minio.local/lingyi/drug-wound-08.png','用于革兰阳性球菌引起的皮肤感染。',1),
(3009,2005,'维生素C咀嚼片','日常补充维C','https://minio.local/lingyi/drug-vitc-09.png','用于补充维生素C。',1),
(3010,2005,'钙维D咀嚼片','骨骼营养补充','https://minio.local/lingyi/drug-calcium-10.png','用于钙与维生素D补充。',1)
ON DUPLICATE KEY UPDATE `name`=VALUES(`name`),`sub_title`=VALUES(`sub_title`),`main_image`=VALUES(`main_image`),`detail`=VALUES(`detail`),`status`=VALUES(`status`);

INSERT INTO `ly_sku` (`id`,`spu_id`,`sku_code`,`title`,`attrs_json`,`price`,`origin_price`,`status`) VALUES
(4001,3001,'COLD-001','复方感冒灵颗粒 10袋/盒','{"规格":"10袋/盒"}',19.90,23.90,1),
(4002,3001,'COLD-002','复方感冒灵颗粒 20袋/盒','{"规格":"20袋/盒"}',35.80,39.80,1),
(4003,3002,'FEVER-001','布洛芬缓释胶囊 12粒','{"规格":"12粒"}',18.50,21.00,1),
(4004,3002,'FEVER-002','布洛芬缓释胶囊 24粒','{"规格":"24粒"}',32.00,36.00,1),
(4005,3003,'COUGH-001','川贝枇杷糖浆 150ml','{"规格":"150ml"}',22.80,25.80,1),
(4006,3003,'COUGH-002','川贝枇杷糖浆 300ml','{"规格":"300ml"}',39.90,45.00,1),
(4007,3004,'PHLEGM-001','氨溴索口服溶液 100ml','{"规格":"100ml"}',26.50,29.90,1),
(4008,3004,'PHLEGM-002','氨溴索口服溶液 200ml','{"规格":"200ml"}',46.80,52.00,1),
(4009,3005,'STOMACH-001','蒙脱石散 12袋','{"规格":"12袋"}',16.90,19.90,1),
(4010,3005,'STOMACH-002','蒙脱石散 24袋','{"规格":"24袋"}',29.90,33.90,1),
(4011,3006,'DIGEST-001','健胃消食片 32片','{"规格":"32片"}',15.80,18.80,1),
(4012,3006,'DIGEST-002','健胃消食片 64片','{"规格":"64片"}',28.60,32.60,1),
(4013,3007,'SKIN-001','炉甘石洗剂 100ml','{"规格":"100ml"}',12.50,15.00,1),
(4014,3007,'SKIN-002','炉甘石洗剂 200ml','{"规格":"200ml"}',19.80,23.00,1),
(4015,3008,'WOUND-001','莫匹罗星软膏 5g','{"规格":"5g"}',24.00,28.00,1),
(4016,3008,'WOUND-002','莫匹罗星软膏 10g','{"规格":"10g"}',43.90,48.00,1),
(4017,3009,'VITC-001','维生素C咀嚼片 60片','{"规格":"60片"}',18.80,22.00,1),
(4018,3009,'VITC-002','维生素C咀嚼片 120片','{"规格":"120片"}',33.90,38.00,1),
(4019,3010,'CALCIUM-001','钙维D咀嚼片 60片','{"规格":"60片"}',29.80,35.00,1),
(4020,3010,'CALCIUM-002','钙维D咀嚼片 120片','{"规格":"120片"}',54.90,60.00,1)
ON DUPLICATE KEY UPDATE `title`=VALUES(`title`),`attrs_json`=VALUES(`attrs_json`),`price`=VALUES(`price`),`origin_price`=VALUES(`origin_price`),`status`=VALUES(`status`);

INSERT INTO `ly_sku_stock` (`id`,`sku_id`,`stock_total`,`stock_available`,`stock_locked`,`version`) VALUES
(5001,4001,300,300,0,0),(5002,4002,240,240,0,0),(5003,4003,320,320,0,0),(5004,4004,180,180,0,0),
(5005,4005,260,260,0,0),(5006,4006,150,150,0,0),(5007,4007,180,180,0,0),(5008,4008,120,120,0,0),
(5009,4009,280,280,0,0),(5010,4010,160,160,0,0),(5011,4011,260,260,0,0),(5012,4012,180,180,0,0),
(5013,4013,220,220,0,0),(5014,4014,160,160,0,0),(5015,4015,140,140,0,0),(5016,4016,110,110,0,0),
(5017,4017,260,260,0,0),(5018,4018,180,180,0,0),(5019,4019,200,200,0,0),(5020,4020,150,150,0,0)
ON DUPLICATE KEY UPDATE `stock_total`=VALUES(`stock_total`),`stock_available`=VALUES(`stock_available`),`stock_locked`=VALUES(`stock_locked`),`version`=VALUES(`version`);
