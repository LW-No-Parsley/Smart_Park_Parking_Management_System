-- 修复所有表中create_time和update_time为NULL的记录
USE Smart_Park;

-- 设置一个合理的默认时间（2026年2月1日）
SET @default_time = '2026-02-01 00:00:00';

-- 1. 修复park_area表
UPDATE park_area 
SET create_time = @default_time 
WHERE create_time IS NULL;

UPDATE park_area 
SET update_time = COALESCE(update_time, create_time) 
WHERE update_time IS NULL;

-- 2. 修复parking_zone表
UPDATE parking_zone 
SET create_time = @default_time 
WHERE create_time IS NULL;

UPDATE parking_zone 
SET update_time = COALESCE(update_time, create_time) 
WHERE update_time IS NULL;

-- 3. 修复parking_space表
UPDATE parking_space 
SET create_time = @default_time 
WHERE create_time IS NULL;

UPDATE parking_space 
SET update_time = COALESCE(update_time, create_time) 
WHERE update_time IS NULL;

-- 4. 修复park_user表
UPDATE park_user 
SET create_time = @default_time 
WHERE create_time IS NULL;

UPDATE park_user 
SET update_time = COALESCE(update_time, create_time) 
WHERE update_time IS NULL;

-- 5. 修复sys_user表
UPDATE sys_user 
SET create_time = @default_time 
WHERE create_time IS NULL;

UPDATE sys_user 
SET update_time = COALESCE(update_time, create_time) 
WHERE update_time IS NULL;

-- 6. 修复sys_role表
UPDATE sys_role 
SET create_time = @default_time 
WHERE create_time IS NULL;

UPDATE sys_role 
SET update_time = COALESCE(update_time, create_time) 
WHERE update_time IS NULL;

-- 7. 修复sys_permission表
UPDATE sys_permission 
SET create_time = @default_time 
WHERE create_time IS NULL;

UPDATE sys_permission 
SET update_time = COALESCE(update_time, create_time) 
WHERE update_time IS NULL;

-- 8. 修复sys_user_role表
UPDATE sys_user_role 
SET create_time = @default_time 
WHERE create_time IS NULL;

UPDATE sys_user_role 
SET update_time = COALESCE(update_time, create_time) 
WHERE update_time IS NULL;

-- 9. 修复sys_role_permission表
UPDATE sys_role_permission 
SET create_time = @default_time 
WHERE create_time IS NULL;

UPDATE sys_role_permission 
SET update_time = COALESCE(update_time, create_time) 
WHERE update_time IS NULL;

-- 10. 修复vehicle表
UPDATE vehicle 
SET create_time = @default_time 
WHERE create_time IS NULL;

UPDATE vehicle 
SET update_time = COALESCE(update_time, create_time) 
WHERE update_time IS NULL;

-- 11. 修复reservation表
UPDATE reservation 
SET create_time = @default_time 
WHERE create_time IS NULL;

UPDATE reservation 
SET update_time = COALESCE(update_time, create_time) 
WHERE update_time IS NULL;

-- 12. 修复payment_record表
UPDATE payment_record 
SET create_time = @default_time 
WHERE create_time IS NULL;

UPDATE payment_record 
SET update_time = COALESCE(update_time, create_time) 
WHERE update_time IS NULL;

-- 13. 修复refund_record表
UPDATE refund_record 
SET create_time = @default_time 
WHERE create_time IS NULL;

UPDATE refund_record 
SET update_time = COALESCE(update_time, create_time) 
WHERE update_time IS NULL;

-- 14. 修复gate_device表
UPDATE gate_device 
SET create_time = @default_time 
WHERE create_time IS NULL;

UPDATE gate_device 
SET update_time = COALESCE(update_time, create_time) 
WHERE update_time IS NULL;

-- 15. 修复blacklist表
UPDATE blacklist 
SET create_time = @default_time 
WHERE create_time IS NULL;

UPDATE blacklist 
SET update_time = COALESCE(update_time, create_time) 
WHERE update_time IS NULL;

-- 16. 修复exception_report表
UPDATE exception_report 
SET create_time = @default_time 
WHERE create_time IS NULL;

UPDATE exception_report 
SET update_time = COALESCE(update_time, create_time) 
WHERE update_time IS NULL;

-- 17. 修复operation_log表
UPDATE operation_log 
SET create_time = @default_time 
WHERE create_time IS NULL;

UPDATE operation_log 
SET update_time = COALESCE(update_time, create_time) 
WHERE update_time IS NULL;

-- 18. 修复access_log表
UPDATE access_log 
SET create_time = @default_time 
WHERE create_time IS NULL;

UPDATE access_log 
SET update_time = COALESCE(update_time, create_time) 
WHERE update_time IS NULL;

-- 19. 修复sys_captcha表
UPDATE sys_captcha 
SET create_time = @default_time 
WHERE create_time IS NULL;

UPDATE sys_captcha 
SET update_time = COALESCE(update_time, create_time) 
WHERE update_time IS NULL;

-- 20. 修复sys_login_log表
UPDATE sys_login_log 
SET create_time = @default_time 
WHERE create_time IS NULL;

UPDATE sys_login_log 
SET update_time = COALESCE(update_time, create_time) 
WHERE update_time IS NULL;

-- 21. 修复sys_token_jti_blacklist表
UPDATE sys_token_jti_blacklist 
SET create_time = @default_time 
WHERE create_time IS NULL;

UPDATE sys_token_jti_blacklist 
SET update_time = COALESCE(update_time, create_time) 
WHERE update_time IS NULL;

-- 22. 修复sys_user_park_area表
UPDATE sys_user_park_area 
SET create_time = @default_time 
WHERE create_time IS NULL;

UPDATE sys_user_park_area 
SET update_time = COALESCE(update_time, create_time) 
WHERE update_time IS NULL;

-- 23. 修复system_config表
UPDATE system_config 
SET create_time = @default_time 
WHERE create_time IS NULL;

UPDATE system_config 
SET update_time = COALESCE(update_time, create_time) 
WHERE update_time IS NULL;

-- 24. 修复space_occupy表
UPDATE space_occupy 
SET create_time = @default_time 
WHERE create_time IS NULL;

-- 显示修复结果
SELECT '修复完成' as status;
