-- =========================================================================================
-- 诊断脚本：检查用户与动态数据匹配情况
-- 执行此脚本来验证 xypai_user 和 xypai_content 数据库的数据是否正确关联
-- =========================================================================================

-- 1. 检查 xypai_user 数据库中的用户
SELECT '===== 1. xypai_user 数据库中的用户 =====' AS info;
SELECT user_id, nickname, avatar, is_online, deleted
FROM xypai_user.users
WHERE deleted = 0
ORDER BY user_id;

-- 2. 检查 xypai_content 数据库中的动态
SELECT '===== 2. xypai_content 数据库中的动态 =====' AS info;
SELECT id, user_id, title, LEFT(content, 30) as content_preview, cover_image,
       like_count, comment_count, status, deleted, visibility
FROM xypai_content.feed
WHERE deleted = 0 AND status = 0 AND visibility = 0
ORDER BY user_id, created_timestamp DESC;

-- 3. 检查每个用户的动态数量
SELECT '===== 3. 每个用户的动态数量 =====' AS info;
SELECT
    u.user_id,
    u.nickname,
    COUNT(f.id) as feed_count
FROM xypai_user.users u
LEFT JOIN xypai_content.feed f ON u.user_id = f.user_id
    AND f.deleted = 0 AND f.status = 0 AND f.visibility = 0
WHERE u.deleted = 0
GROUP BY u.user_id, u.nickname
ORDER BY u.user_id;

-- 4. 检查用户是否有上架技能（首页列表显示条件）
SELECT '===== 4. 用户技能上架情况 =====' AS info;
SELECT
    u.user_id,
    u.nickname,
    COUNT(s.skill_id) as online_skill_count
FROM xypai_user.users u
LEFT JOIN xypai_user.skills s ON u.user_id = s.user_id
    AND s.is_online = 1 AND s.deleted = 0
WHERE u.deleted = 0
GROUP BY u.user_id, u.nickname
ORDER BY u.user_id;

-- 5. 完整检查：用户 + 技能 + 动态
SELECT '===== 5. 完整检查：用户信息 + 技能数 + 动态数 =====' AS info;
SELECT
    u.user_id,
    u.nickname,
    u.is_online as user_online,
    (SELECT COUNT(*) FROM xypai_user.skills s WHERE s.user_id = u.user_id AND s.is_online = 1 AND s.deleted = 0) as online_skills,
    (SELECT COUNT(*) FROM xypai_content.feed f WHERE f.user_id = u.user_id AND f.deleted = 0 AND f.status = 0 AND f.visibility = 0) as feeds,
    CASE
        WHEN EXISTS (SELECT 1 FROM xypai_user.skills s WHERE s.user_id = u.user_id AND s.is_online = 1 AND s.deleted = 0)
        THEN 'YES - 会出现在首页'
        ELSE 'NO - 没有上架技能'
    END as will_appear_in_homepage
FROM xypai_user.users u
WHERE u.deleted = 0
ORDER BY u.user_id;

SELECT '===== 诊断完成 =====' AS info;
