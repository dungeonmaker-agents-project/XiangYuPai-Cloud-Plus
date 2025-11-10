package com.xypai.chat.utils;

import java.util.List;

/**
 * 会话工具类 (v7.1)
 * 
 * @author xypai
 * @date 2025-01-14
 */
public class ConversationUtils {

    /**
     * 生成私聊会话标题
     * 
     * @param user1Nickname 用户1昵称
     * @param user2Nickname 用户2昵称
     * @return 会话标题
     */
    public static String generatePrivateConversationTitle(String user1Nickname, String user2Nickname) {
        return user1Nickname + " 和 " + user2Nickname + " 的对话";
    }

    /**
     * 生成群聊会话默认标题
     * 
     * @param creatorNickname 创建者昵称
     * @param memberCount 成员数量
     * @return 会话标题
     */
    public static String generateGroupConversationTitle(String creatorNickname, int memberCount) {
        return creatorNickname + " 的群聊(" + memberCount + "人)";
    }

    /**
     * 生成订单会话标题
     * 
     * @param orderId 订单ID
     * @return 会话标题
     */
    public static String generateOrderConversationTitle(Long orderId) {
        return "订单会话 #" + orderId;
    }

    /**
     * 验证会话标题长度
     * 
     * @param title 标题
     * @return 是否合法
     */
    public static boolean validateTitleLength(String title) {
        return title != null && title.length() >= 1 && title.length() <= 100;
    }

    /**
     * 验证会话描述长度
     * 
     * @param description 描述
     * @return 是否合法
     */
    public static boolean validateDescriptionLength(String description) {
        if (description == null) {
            return true;
        }
        return description.length() <= 500;
    }

    /**
     * 验证群聊最大成员数
     * 
     * @param maxMembers 最大成员数
     * @return 是否合法
     */
    public static boolean validateMaxMembers(Integer maxMembers) {
        if (maxMembers == null) {
            return true;
        }
        return maxMembers >= 2 && maxMembers <= 500; // 群聊最少2人，最多500人
    }

    /**
     * 计算会话活跃度分数
     * 
     * @param messageCount 消息数量
     * @param memberCount 成员数量
     * @param daysSinceLastMessage 距离最后消息的天数
     * @return 活跃度分数（0-100）
     */
    public static int calculateActivityScore(int messageCount, int memberCount, int daysSinceLastMessage) {
        // 基础分数
        int score = 50;

        // 消息数量加分（最多+30分）
        if (messageCount > 1000) {
            score += 30;
        } else if (messageCount > 500) {
            score += 20;
        } else if (messageCount > 100) {
            score += 10;
        }

        // 成员数量加分（最多+10分）
        if (memberCount > 50) {
            score += 10;
        } else if (memberCount > 10) {
            score += 5;
        }

        // 最后消息时间扣分
        if (daysSinceLastMessage > 30) {
            score -= 50;
        } else if (daysSinceLastMessage > 7) {
            score -= 20;
        } else if (daysSinceLastMessage > 1) {
            score -= 5;
        }

        return Math.max(0, Math.min(100, score));
    }

    /**
     * 生成会话唯一标识（用于私聊去重）
     * 
     * @param user1Id 用户1 ID
     * @param user2Id 用户2 ID
     * @return 唯一标识
     */
    public static String generatePrivateConversationKey(Long user1Id, Long user2Id) {
        // 确保顺序一致（小ID在前）
        Long minId = Math.min(user1Id, user2Id);
        Long maxId = Math.max(user1Id, user2Id);
        return "private:" + minId + ":" + maxId;
    }

    /**
     * 解析@提及的用户ID列表
     * 
     * @param content 消息内容
     * @return 被提及的用户ID列表
     */
    public static List<Long> parseMentionedUsers(String content) {
        // TODO: 实现@用户解析逻辑
        // 格式：@[用户名](userId)
        return List.of();
    }

    /**
     * 检查是否包含敏感词
     * 
     * @param content 内容
     * @return 是否包含敏感词
     */
    public static boolean containsSensitiveWords(String content) {
        // TODO: 集成敏感词检测服务
        return false;
    }

    /**
     * 脱敏处理消息内容（用于日志）
     * 
     * @param content 原内容
     * @return 脱敏后的内容
     */
    public static String maskContent(String content) {
        if (content == null || content.length() <= 10) {
            return "***";
        }
        
        return content.substring(0, 5) + "..." + content.substring(content.length() - 5);
    }
}

