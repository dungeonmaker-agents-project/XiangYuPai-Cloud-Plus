-- ========================================
-- APP客户端配置脚本
-- React Native Expo App Authentication
-- ========================================
-- 用途: 配置前端APP的认证客户端信息
-- 执行时机: 首次部署后端时执行一次
-- 数据库: ruoyi-cloud-plus
-- ========================================

USE `ry-cloud`;

-- ========================================
-- 1. 检查sys_client表是否存在
-- ========================================
SELECT 'Checking sys_client table...' as status;

-- ========================================
-- 2. 删除旧配置（如果存在）
-- ========================================
DELETE FROM `sys_client` WHERE client_id = 'app-client';

-- ========================================
-- 3. 插入APP客户端配置
-- ========================================
INSERT INTO `sys_client` (
    `client_id`,          -- 客户端ID（前端必须匹配）
    `client_key`,         -- 客户端标识
    `client_secret`,      -- 客户端密钥
    `grant_type`,         -- 支持的认证类型
    `device_type`,        -- 设备类型
    `active_timeout`,     -- 活跃超时时间（秒）
    `timeout`,            -- 总超时时间（秒）
    `status`,             -- 状态（0=正常，1=停用）
    `del_flag`,           -- 删除标志（0=存在，2=删除）
    `create_time`,        -- 创建时间
    `update_time`         -- 更新时间
) VALUES (
    'app-client',                    -- ⭐ 客户端ID (前端必须匹配)
    'app-client',                    -- 客户端标识
    'app_secret_key_2024_xy',        -- 密钥（生产环境需修改）
    'password,sms',                  -- ⭐ 支持密码登录和短信登录
    'app',                           -- ⭐ 设备类型（app端）
    1800,                            -- 活跃超时: 30分钟
    7200,                            -- 总超时: 2小时
    '0',                             -- 状态: 正常
    '0',                             -- 未删除
    NOW(),                           -- 创建时间
    NOW()                            -- 更新时间
);

-- ========================================
-- 4. 验证配置
-- ========================================
SELECT 
    client_id,
    client_key,
    grant_type,
    device_type,
    timeout,
    active_timeout,
    status,
    create_time
FROM `sys_client` 
WHERE client_id = 'app-client';

-- ========================================
-- 5. 测试用户账号（可选）
-- ========================================
-- 如果需要创建测试账号，取消下面注释

/*
-- 检查测试用户是否存在
SELECT * FROM `sys_user` WHERE user_name = 'testuser';

-- 插入测试用户（如果不存在）
INSERT INTO `sys_user` (
    `tenant_id`,
    `dept_id`,
    `user_name`,
    `nick_name`,
    `user_type`,
    `email`,
    `phonenumber`,
    `sex`,
    `avatar`,
    `password`,           -- 密码: test123456 (BCrypt加密)
    `status`,
    `del_flag`,
    `create_by`,
    `create_time`,
    `update_by`,
    `update_time`
) VALUES (
    '000000',                                -- 默认租户
    103,                                     -- 研发部门
    'testuser',                              -- 用户名
    '测试用户',                               -- 昵称
    '00',                                    -- 系统用户
    'test@xiangyupai.com',                   -- 邮箱
    '13800138000',                           -- ⭐ 手机号（用于登录）
    '0',                                     -- 男
    '',                                      -- 头像
    '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE/sW9ppyKh0Ru',  -- test123456
    '0',                                     -- 状态: 正常
    '0',                                     -- 未删除
    'admin',                                 -- 创建者
    NOW(),                                   -- 创建时间
    'admin',                                 -- 更新者
    NOW()                                    -- 更新时间
);

-- 分配用户角色（普通用户）
INSERT INTO `sys_user_role` (`user_id`, `role_id`) 
SELECT user_id, 2 FROM `sys_user` WHERE user_name = 'testuser';
*/

-- ========================================
-- 6. 短信配置（可选）
-- ========================================
-- 如果使用阿里云短信，需要配置以下参数

/*
INSERT INTO `sys_config` VALUES 
(NULL, '000000', '短信服务AccessKeyId', 'sms.accessKeyId', 'your_access_key_id', 'Y', NOW(), NULL, NULL, '阿里云短信AccessKeyId'),
(NULL, '000000', '短信服务AccessKeySecret', 'sms.accessKeySecret', 'your_access_key_secret', 'Y', NOW(), NULL, NULL, '阿里云短信AccessKeySecret'),
(NULL, '000000', '短信签名', 'sms.signName', '相遇派', 'Y', NOW(), NULL, NULL, '短信签名名称'),
(NULL, '000000', '登录验证码模板', 'sms.template.login', 'SMS_123456789', 'Y', NOW(), NULL, NULL, '登录验证码模板CODE'),
(NULL, '000000', '注册验证码模板', 'sms.template.register', 'SMS_123456790', 'Y', NOW(), NULL, NULL, '注册验证码模板CODE'),
(NULL, '000000', '重置密码验证码模板', 'sms.template.reset', 'SMS_123456791', 'Y', NOW(), NULL, NULL, '重置密码验证码模板CODE');
*/

-- ========================================
-- 7. 输出说明
-- ========================================
SELECT '✅ APP客户端配置完成！' as message;
SELECT '🔑 客户端ID: app-client' as info;
SELECT '📱 支持认证类型: password, sms' as info;
SELECT '⏰ Token有效期: 2小时' as info;
SELECT '🔄 活跃超时: 30分钟' as info;
SELECT '' as separator;
SELECT '📝 下一步操作:' as next_steps;
SELECT '1. 启动 ruoyi-auth 服务 (端口 8081)' as step;
SELECT '2. 启动 ruoyi-gateway 服务 (端口 8080)' as step;
SELECT '3. 启动前端APP，使用以下测试账号:' as step;
SELECT '   手机号: 13800138000' as step;
SELECT '   密码: test123456' as step;
SELECT '4. 如需使用短信登录，请配置短信服务参数' as step;

-- ========================================
-- End of Script
-- ========================================

