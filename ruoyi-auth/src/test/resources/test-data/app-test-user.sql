-- ==========================================
-- SimpleSaTokenTest 测试用户数据
-- ==========================================
-- 用途: 为 SimpleSaTokenTest.java 提供测试用户
-- 手机号: 13900000001
-- 密码: 123456
-- ==========================================

USE `ry-cloud`;

-- 清理旧数据
DELETE FROM sys_user_role WHERE user_id = 1001;
DELETE FROM sys_user WHERE user_id = 1001;

-- 创建APP测试用户
INSERT INTO sys_user (
    user_id,
    tenant_id,
    dept_id,
    user_name,
    nick_name,
    user_type,
    email,
    phonenumber,
    sex,
    avatar,
    password,
    status,
    del_flag,
    create_by,
    create_time,
    update_by,
    update_time,
    remark
) VALUES (
    1001,                                                                       -- user_id
    '000000',                                                                   -- tenant_id (默认租户)
    103,                                                                        -- dept_id (研发部门)
    'appuser001',                                                               -- user_name
    'APP测试用户',                                                               -- nick_name
    'sys_user',                                                                 -- user_type
    'appuser001@xypai.com',                                                    -- email
    '13900000001',                                                             -- phonenumber ✅
    '0',                                                                        -- sex (男)
    '',                                                                         -- avatar
    '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE/sLt6Cdq9.Ju',          -- password: 123456 ✅
    '0',                                                                        -- status (正常)
    '0',                                                                        -- del_flag (未删除)
    1,                                                                          -- create_by
    NOW(),                                                                      -- create_time
    1,                                                                          -- update_by
    NOW(),                                                                      -- update_time
    'SimpleSaTokenTest测试用户 - 用于APP登录测试'                               -- remark
);

-- 创建APP用户角色（如果不存在）
INSERT INTO sys_role (
    role_id,
    tenant_id,
    role_name,
    role_key,
    role_sort,
    data_scope,
    status,
    del_flag,
    create_by,
    create_time,
    update_by,
    update_time,
    remark
) VALUES (
    100,
    '000000',
    'APP普通用户',
    'app_user',
    100,
    '5',                                    -- 仅本人数据权限
    '0',
    '0',
    1,
    NOW(),
    1,
    NOW(),
    'APP端普通用户角色'
) ON DUPLICATE KEY UPDATE role_name = 'APP普通用户';

-- 分配角色给测试用户
INSERT INTO sys_user_role (user_id, role_id) VALUES (1001, 100);

-- 创建APP菜单权限（如果不存在）
INSERT INTO sys_menu (
    menu_id,
    menu_name,
    parent_id,
    order_num,
    path,
    component,
    query_param,
    is_frame,
    is_cache,
    menu_type,
    visible,
    status,
    perms,
    icon,
    create_by,
    create_time,
    update_by,
    update_time,
    remark
) VALUES
-- APP功能菜单目录
(11700, 'APP功能', 0, 1, 'app', NULL, '', 1, 0, 'M', '1', '0', NULL, 'phone', 1, NOW(), 1, NOW(), 'APP端功能菜单'),
-- APP首页
(11701, 'APP首页', 11700, 1, 'home', NULL, '', 1, 0, 'C', '1', '0', 'app:home:view', 'home', 1, NOW(), 1, NOW(), 'APP首页查看'),
-- APP个人中心
(11702, 'APP个人中心', 11700, 2, 'profile', NULL, '', 1, 0, 'C', '1', '0', 'app:profile:view', 'user', 1, NOW(), 1, NOW(), 'APP个人中心'),
-- APP内容浏览
(11703, 'APP内容浏览', 11700, 3, 'content', NULL, '', 1, 0, 'C', '1', '0', 'app:content:view', 'list', 1, NOW(), 1, NOW(), 'APP内容浏览')
ON DUPLICATE KEY UPDATE menu_name = VALUES(menu_name);

-- 分配菜单权限给APP用户角色
INSERT INTO sys_role_menu (role_id, menu_id) VALUES
(100, 11700),  -- APP功能
(100, 11701),  -- APP首页
(100, 11702),  -- APP个人中心
(100, 11703)   -- APP内容浏览
ON DUPLICATE KEY UPDATE role_id = VALUES(role_id);

-- 验证数据
SELECT '✅ APP测试用户创建完成' AS status;
SELECT 
    user_id,
    user_name,
    nick_name,
    phonenumber,
    email,
    status
FROM sys_user 
WHERE user_id = 1001;

SELECT 
    u.user_id,
    u.user_name,
    u.nick_name,
    u.phonenumber,
    r.role_name,
    r.role_key
FROM sys_user u
LEFT JOIN sys_user_role ur ON u.user_id = ur.user_id
LEFT JOIN sys_role r ON ur.role_id = r.role_id
WHERE u.user_id = 1001;

SELECT '📋 测试信息:' AS '';
SELECT '   手机号: 13900000001' AS info;
SELECT '   密码: 123456' AS info;
SELECT '   用户名: appuser001' AS info;
SELECT '   角色: APP普通用户' AS info;

-- ==========================================
-- 使用说明
-- ==========================================
-- 1. 执行此SQL创建测试用户
-- 2. 运行 SimpleSaTokenTest.java
-- 3. 测试会使用手机号 13900000001 登录
-- 4. Token生成后可以访问所有微服务
-- ==========================================

