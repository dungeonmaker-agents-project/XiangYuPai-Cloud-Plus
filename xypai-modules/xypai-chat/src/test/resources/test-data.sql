-- Test Data for xypai-chat Module
-- This script creates test data for running integration tests

-- ============================================================
-- Clear existing test data
-- ============================================================
DELETE FROM message WHERE sender_id IN (1, 2, 3) OR receiver_id IN (1, 2, 3);
DELETE FROM conversation WHERE user_id IN (1, 2, 3);

-- ============================================================
-- Test Users (assumed to exist in user service)
-- ============================================================
-- User 1: ID = 1
-- User 2: ID = 2
-- User 3: ID = 3

-- ============================================================
-- Test Conversations
-- ============================================================

-- Conversation 1: User 1 <-> User 2 (bidirectional)
INSERT INTO conversation (conversation_id, user_id, other_user_id, last_message, last_message_time, unread_count, is_deleted, create_by, create_time, update_by, update_time, version)
VALUES
    (1001, 1, 2, 'Test message from setup', NOW(), 0, false, 1, NOW(), 1, NOW(), 0),
    (1002, 2, 1, 'Test message from setup', NOW(), 0, false, 2, NOW(), 2, NOW(), 0);

-- Conversation 2: User 1 <-> User 3 (bidirectional)
INSERT INTO conversation (conversation_id, user_id, other_user_id, last_message, last_message_time, unread_count, is_deleted, create_by, create_time, update_by, update_time, version)
VALUES
    (1003, 1, 3, 'Another test conversation', NOW(), 0, false, 1, NOW(), 1, NOW(), 0),
    (1004, 3, 1, 'Another test conversation', NOW(), 0, false, 3, NOW(), 3, NOW(), 0);

-- ============================================================
-- Test Messages
-- ============================================================

-- Messages for Conversation 1 (User 1 <-> User 2)
INSERT INTO message (message_id, conversation_id, sender_id, receiver_id, message_type, content, media_url, thumbnail_url, duration, status, is_recalled, is_deleted, create_by, create_time, update_by, update_time, version)
VALUES
    (2001, 1001, 1, 2, 'text', 'Hello User 2, this is a test message', NULL, NULL, NULL, 1, false, false, 1, NOW(), 1, NOW(), 0),
    (2002, 1001, 2, 1, 'text', 'Hi User 1, thanks for the message!', NULL, NULL, NULL, 2, false, false, 2, NOW(), 2, NOW(), 0),
    (2003, 1001, 1, 2, 'image', NULL, 'https://oss.example.com/images/test1.jpg', NULL, NULL, 1, false, false, 1, NOW(), 1, NOW(), 0),
    (2004, 1001, 2, 1, 'voice', NULL, 'https://oss.example.com/voice/test1.mp3', NULL, 30, 1, false, false, 2, NOW(), 2, NOW(), 0),
    (2005, 1001, 1, 2, 'video', NULL, 'https://oss.example.com/video/test1.mp4', 'https://oss.example.com/video/test1_thumb.jpg', 45, 1, false, false, 1, NOW(), 1, NOW(), 0);

-- Messages for Conversation 2 (User 1 <-> User 3)
INSERT INTO message (message_id, conversation_id, sender_id, receiver_id, message_type, content, media_url, thumbnail_url, duration, status, is_recalled, is_deleted, create_by, create_time, update_by, update_time, version)
VALUES
    (2006, 1003, 1, 3, 'text', 'Hey User 3!', NULL, NULL, NULL, 1, false, false, 1, NOW(), 1, NOW(), 0),
    (2007, 1003, 3, 1, 'text', 'Hello User 1!', NULL, NULL, NULL, 1, false, false, 3, NOW(), 3, NOW(), 0);

-- ============================================================
-- Test Data for Specific Scenarios
-- ============================================================

-- Recalled message (for testing recall functionality)
INSERT INTO message (message_id, conversation_id, sender_id, receiver_id, message_type, content, media_url, thumbnail_url, duration, status, is_recalled, is_deleted, create_by, create_time, update_by, update_time, version)
VALUES
    (2008, 1001, 1, 2, 'text', 'This message will be recalled', NULL, NULL, NULL, 1, true, false, 1, NOW(), 1, NOW(), 0);

-- Old message (>2 minutes for testing recall timeout)
INSERT INTO message (message_id, conversation_id, sender_id, receiver_id, message_type, content, media_url, thumbnail_url, duration, status, is_recalled, is_deleted, create_by, create_time, update_by, update_time, version)
VALUES
    (2009, 1001, 1, 2, 'text', 'Old message from 5 minutes ago', NULL, NULL, NULL, 1, false, false, 1, DATE_SUB(NOW(), INTERVAL 5 MINUTE), 1, NOW(), 0);

-- Unread messages (status = 1)
INSERT INTO message (message_id, conversation_id, sender_id, receiver_id, message_type, content, media_url, thumbnail_url, duration, status, is_recalled, is_deleted, create_by, create_time, update_by, update_time, version)
VALUES
    (2010, 1002, 2, 1, 'text', 'Unread message 1', NULL, NULL, NULL, 1, false, false, 2, NOW(), 2, NOW(), 0),
    (2011, 1002, 2, 1, 'text', 'Unread message 2', NULL, NULL, NULL, 1, false, false, 2, NOW(), 2, NOW(), 0),
    (2012, 1002, 2, 1, 'text', 'Unread message 3', NULL, NULL, NULL, 1, false, false, 2, NOW(), 2, NOW(), 0);

-- ============================================================
-- Verification Queries
-- ============================================================

-- Verify conversations created
-- SELECT * FROM conversation WHERE user_id IN (1, 2, 3);

-- Verify messages created
-- SELECT * FROM message WHERE sender_id IN (1, 2, 3) ORDER BY create_time DESC;

-- Check unread counts
-- SELECT
--     c.user_id,
--     c.other_user_id,
--     c.unread_count,
--     COUNT(m.message_id) as actual_unread
-- FROM conversation c
-- LEFT JOIN message m ON c.conversation_id = m.conversation_id
--     AND m.receiver_id = c.user_id
--     AND m.status = 1
-- WHERE c.user_id IN (1, 2, 3)
-- GROUP BY c.user_id, c.other_user_id, c.unread_count;

-- ============================================================
-- Notes
-- ============================================================
-- 1. This data is for testing purposes only
-- 2. IDs start from 1001 to avoid conflicts with production data
-- 3. All timestamps are set to NOW() for current time
-- 4. Use @Transactional in tests to auto-rollback changes
-- 5. For isolated tests, let tests create their own data instead of relying on this
