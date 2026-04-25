-- 修复 parking_fee_rule 表缺少的字段（兼容MySQL 5.x版本）
-- 先检查字段是否存在，不存在则添加

-- 添加 fixed_price 字段
SET @dbname = DATABASE();
SET @tablename = 'parking_fee_rule';
SET @columnname = 'fixed_price';
SET @preparedStatement = (SELECT IF(
  (SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = @dbname AND TABLE_NAME = @tablename AND COLUMN_NAME = @columnname) = 0,
  CONCAT('ALTER TABLE ', @tablename, ' ADD COLUMN `fixed_price` decimal(10,2) DEFAULT NULL COMMENT ''按次计费的固定价格'' AFTER `tiered_pricing`;'),
  'SELECT 1;'
));
PREPARE stmt FROM @preparedStatement;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 添加 max_charge_hours 字段
SET @columnname = 'max_charge_hours';
SET @preparedStatement = (SELECT IF(
  (SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = @dbname AND TABLE_NAME = @tablename AND COLUMN_NAME = @columnname) = 0,
  CONCAT('ALTER TABLE ', @tablename, ' ADD COLUMN `max_charge_hours` int DEFAULT NULL COMMENT ''最大计费小时数'' AFTER `daily_cap`;'),
  'SELECT 1;'
));
PREPARE stmt FROM @preparedStatement;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 添加 time_periods 字段
SET @columnname = 'time_periods';
SET @preparedStatement = (SELECT IF(
  (SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = @dbname AND TABLE_NAME = @tablename AND COLUMN_NAME = @columnname) = 0,
  CONCAT('ALTER TABLE ', @tablename, ' ADD COLUMN `time_periods` text DEFAULT NULL COMMENT ''时段定价（JSON格式）'' AFTER `max_charge_hours`;'),
  'SELECT 1;'
));
PREPARE stmt FROM @preparedStatement;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 添加 tiered_pricing 字段
SET @columnname = 'tiered_pricing';
SET @preparedStatement = (SELECT IF(
  (SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = @dbname AND TABLE_NAME = @tablename AND COLUMN_NAME = @columnname) = 0,
  CONCAT('ALTER TABLE ', @tablename, ' ADD COLUMN `tiered_pricing` text DEFAULT NULL COMMENT ''阶梯定价（JSON格式）'' AFTER `time_periods`;'),
  'SELECT 1;'
));
PREPARE stmt FROM @preparedStatement;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 添加 create_by 字段
SET @columnname = 'create_by';
SET @preparedStatement = (SELECT IF(
  (SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = @dbname AND TABLE_NAME = @tablename AND COLUMN_NAME = @columnname) = 0,
  CONCAT('ALTER TABLE ', @tablename, ' ADD COLUMN `create_by` bigint DEFAULT NULL COMMENT ''创建人'' AFTER `update_time`;'),
  'SELECT 1;'
));
PREPARE stmt FROM @preparedStatement;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 添加 update_by 字段
SET @columnname = 'update_by';
SET @preparedStatement = (SELECT IF(
  (SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = @dbname AND TABLE_NAME = @tablename AND COLUMN_NAME = @columnname) = 0,
  CONCAT('ALTER TABLE ', @tablename, ' ADD COLUMN `update_by` bigint DEFAULT NULL COMMENT ''更新人'' AFTER `create_by`;'),
  'SELECT 1;'
));
PREPARE stmt FROM @preparedStatement;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 插入默认全局计费规则（如果不存在）
INSERT IGNORE INTO `parking_fee_rule` (`rule_name`, `fee_mode`, `free_minutes`, `unit_duration`, `unit_price`, `daily_cap`, `status`, `sort_order`, `remark`) 
VALUES ('标准计费', 1, 0, 20, 4.00, 50.00, 1, 0, '默认计费规则：每20分钟4元，每日封顶50元');
